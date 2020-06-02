package com.atguigu.gulimall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.atguigu.common.exception.ExceptionCode;
import com.atguigu.common.exception.NotEnoughStockException;
import com.atguigu.common.to.SkuStockTo;
import com.atguigu.gulimall.ware.vo.LockOrderItemsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * 商品库存
 *
 * @author justin
 * @email justinh9408@gmail.com
 * @date 2020-04-21 17:08:09
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    @PostMapping("/lockStock")
    public R lockStock(@RequestBody LockOrderItemsVo vo) {
        try {
            wareSkuService.lockStock(vo);
        } catch (NotEnoughStockException e) {
            return R.error(ExceptionCode.NOT_ENOUGH_STOCK_EXCEPTION.getCode(), ExceptionCode.NOT_ENOUGH_STOCK_EXCEPTION.getMsg());
        }

        return R.ok();
    }

    @PostMapping("/hasstock")
    public List<SkuStockTo> getSkusHaveStock(@RequestBody List<Long> skuIds) {

        List<SkuStockTo> skuStockVo = wareSkuService.getSkusHaveStock(skuIds);

        return skuStockVo;
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:waresku:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
