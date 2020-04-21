import com.atguigu.gulimall.GuliMallProductApplication;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {GuliMallProductApplication.class})
public class GuliMallProductApplicationTest {

    @Autowired
    BrandService brandService;

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