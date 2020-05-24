package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.CatalogLv2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    CategoryBrandRelationService brandRelationService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
//        1.查出所有分类
        List<CategoryEntity> categoryEntities = categoryDao.selectList(null);
//        2.组装父子树形结构
//        2.1 先找出所有一级分类
        List<CategoryEntity> levelOneCat = categoryEntities.stream().filter((categoryEntity) -> {
            return categoryEntity.getParentCid() == 0;
        }).map((categoryEntity) -> {
            categoryEntity.setChildren(getChildren(categoryEntity, categoryEntities));
            return categoryEntity;
        }).sorted(Comparator.comparingInt(cat -> (cat.getSort() == null ? 0 : cat.getSort())))
                .collect(Collectors.toList());

        return levelOneCat;
    }

    @Override
    public void removeMenusByIds(List<Long> asList) {
//        TODO: 检查引用情况
        categoryDao.deleteBatchIds(asList);
    }

    //    [2,25,225]
    @Override
    public Long[] findCateLogIdPath(Long catelogId) {
        List<Long> path = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, path);
        return parentPath.toArray(new Long[3]);
    }

    @CacheEvict(value = "category", key = "'findLevel1Categories'")
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())) {
            brandRelationService.updateCat(category.getCatId(), category.getName());
        }
    }

    @Cacheable(value = "{category}",key = "#root.methodName")
    @Override
    public List<CategoryEntity> findLevel1Categories() {
        System.out.println("findLevel1Categories..");
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    @Cacheable(value = "{category}",key = "#root.methodName")
    @Override
    public Map<String, List<CatalogLv2Vo>> getCatalogJson() {
        System.out.println("getCatalogJson..");
        return getCatalogJsonFromDB();
    }

//    @Override
    public Map<String, List<CatalogLv2Vo>> getCatalogJson2() {
        String catalogJSON = (String) redisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            return getCatalogJsonWithRedissonLock();
        }

        return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<CatalogLv2Vo>>>() {
        });
    }

    //解决缓存一致性问题：双写模式或失效模式，设置过期时间+加读写锁
    //分布式锁redisson版
    private Map<String, List<CatalogLv2Vo>> getCatalogJsonWithRedissonLock() {
        RLock lock = redissonClient.getLock("lock:catalogJSON");
        lock.lock();
        Map<String, List<CatalogLv2Vo>> jsonFromDB;
        try {
//                读数据库
            jsonFromDB = getCatalogJsonFromDB();
//                保存结果到redis
            String s = JSON.toJSONString(jsonFromDB);
            redisTemplate.opsForValue().set("catalogJSON", s, 1, TimeUnit.DAYS);
        } finally {
            lock.unlock();
        }

        return jsonFromDB;
    }


    //分布式锁redis版
    private Map<String, List<CatalogLv2Vo>> getCatalogJsonWithRedisLock() {
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 200, TimeUnit.SECONDS);
        if (lock) {
            String catalogJSON = (String) redisTemplate.opsForValue().get("catalogJSON");
            if (!StringUtils.isEmpty(catalogJSON)) {
                return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<CatalogLv2Vo>>>() {
                });
            }

            Map<String, List<CatalogLv2Vo>> jsonFromDB;
            try {
//                读数据库
                jsonFromDB = getCatalogJsonFromDB();
//                保存结果到redis
                String s = JSON.toJSONString(jsonFromDB);
                redisTemplate.opsForValue().set("catalogJSON", s, 1, TimeUnit.DAYS);
            } finally {
                String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                        "then\n" +
                        "    return redis.call(\"del\",KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";
                redisTemplate.execute(new DefaultRedisScript(script), Arrays.asList("lock"), uuid);
            }
            return jsonFromDB;
        } else {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonWithRedisLock();
        }
    }


    public Map<String, List<CatalogLv2Vo>> getCatalogJsonFromDB() {

        List<CategoryEntity> allCats = baseMapper.selectList(null);

        List<CategoryEntity> lv1Cats = getChildCats(allCats, 0L);

        Map<String, List<CatalogLv2Vo>> parent_cid = lv1Cats.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<CategoryEntity> lv2Cats = getChildCats(allCats, v.getCatId());

            List<CatalogLv2Vo> catalogLv2Vos = lv2Cats.stream().map(lv2 -> {
                CatalogLv2Vo catalogLv2Vo = new CatalogLv2Vo();
                catalogLv2Vo.setCatalog1Id(lv2.getParentCid().toString());
                catalogLv2Vo.setId(lv2.getCatId().toString());
                catalogLv2Vo.setName(lv2.getName());

                List<CategoryEntity> lv3Cats = getChildCats(allCats, lv2.getCatId());
                List<CatalogLv2Vo.Cat3log3Vo> cat3log3Vos = lv3Cats.stream().map(lv3 -> {
                    CatalogLv2Vo.Cat3log3Vo cat3log3Vo = new CatalogLv2Vo.Cat3log3Vo();
                    cat3log3Vo.setCatalog2Id(lv3.getParentCid().toString());
                    cat3log3Vo.setId(lv3.getCatId().toString());
                    cat3log3Vo.setName(lv3.getName());
                    return cat3log3Vo;
                }).collect(Collectors.toList());
                catalogLv2Vo.setCatalog3List(cat3log3Vos);

                return catalogLv2Vo;
            }).collect(Collectors.toList());

            return catalogLv2Vos;
        }));

        return parent_cid;
    }

    private List<CategoryEntity> getChildCats(List<CategoryEntity> list, Long parentId) {

        return list.stream().filter(item -> item.getParentCid() == parentId).collect(Collectors.toList());
    }

    private List<Long> findParentPath(Long catelogId, List<Long> path) {

        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), path);
        }
        path.add(catelogId);
        return path;
    }


    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map((categoryEntity -> {
            categoryEntity.setChildren(getChildren(categoryEntity, all));
            return categoryEntity;
        })).sorted(Comparator.comparingInt(cat -> (cat.getSort() == null ? 0 : cat.getSort())))
                .collect(Collectors.toList());

        return children;
    }
}