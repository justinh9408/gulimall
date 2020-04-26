package com.atguigu.gulimall.product.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryDao categoryDao;

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
        }).map((categoryEntity)->{
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

    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {

        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map((categoryEntity -> {
            categoryEntity.setChildren(getChildren(categoryEntity,all));
            return categoryEntity;
        })).sorted(Comparator.comparingInt(cat -> (cat.getSort() == null ? 0 : cat.getSort())))
                .collect(Collectors.toList());

        return children;
    }
}