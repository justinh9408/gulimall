import com.atguigu.gulimall.GuliMallProductApplication;
import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.bytebuddy.asm.Advice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {GuliMallProductApplication.class})
public class GuliMallProductApplicationTest {

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    AttrAttrgroupRelationDao relationDao;

    @Autowired
    AttrService attrService;

    @Test
    public void testFindParentPath() {
        Long[] cateLogIdPath = categoryService.findCateLogIdPath(225L);
        System.out.println(Arrays.asList(cateLogIdPath));
    }

    @Test
    public void testtestesetse() {
        Long[] longArr = new Long[3];
        for (int i = 0; i < longArr.length; i++) {
            longArr[i] = Long.valueOf(i);
        }
        List<Long> longs =  Arrays.asList(longArr);
        for (Long aLong : longs) {
            System.out.println(aLong);
        }
        List<Long> collect = longs.stream().filter(aLong -> aLong > 0).collect(Collectors.toList());
        for (Long aLong : collect) {
            System.out.println("aLong = " + aLong);
        }

//        ObjectMapper
    }

    @Test
    public void testR() {
        AttrAttrgroupRelationEntity relation =
                relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", 3));
        System.out.println(relation);

    }
    @Test
    public void contestLoad() {
//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setBrandId(1L);
//        brandEntity.setDescript("华为");
//        brandService.updateById(brandEntity);
//        System.out.println("update成功..");

        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("name", "HuaWei"));
        list.forEach((item) ->{
            System.out.println(item);
        });
    }

}