package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.exception.NotEnoughStockException;
import com.atguigu.common.to.SkuStockTo;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.StockDetailLockTo;
import com.atguigu.common.to.mq.StockLockTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.feign.OrderFeignService;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.vo.LockOrderItemsVo;
import com.atguigu.gulimall.ware.vo.OrderItemVo;
import com.atguigu.gulimall.ware.vo.OrderVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@RabbitListener(queues = "stock.release.stock.queue")
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    WareOrderTaskService orderTaskService;

    @Autowired
    WareOrderTaskDetailService orderTaskDetailService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    OrderFeignService orderFeignService;

    @RabbitHandler
    public void releaseLock(StockLockTo stockLockTo, Message message, Channel channel) throws IOException {
        System.out.println("收到解锁库存消息");
        Long orderTaskId = stockLockTo.getId();
        WareOrderTaskEntity byId = orderTaskService.getById(orderTaskId);
        if (byId != null) {
            StockDetailLockTo detail = stockLockTo.getStockLockDetail();
            String orderSn = byId.getOrderSn();
            R r = orderFeignService.oderSn(orderSn);
            if (r.getCode() == 0) {
                OrderVo order = r.getDataInType("order", OrderVo.class);
                if (order == null || order.getStatus() == 4) {
                    WareOrderTaskDetailEntity detailEntity = orderTaskDetailService.getById(detail.getId());
                    if (detailEntity.getLockStatus() == 1){
                        System.out.println("解锁库存啦啦啦！！！！！！");
                        unlockStock(detail.getSkuId(),detail.getWareId(),detail.getSkuNum(),detail.getId());

                    }
                }
//                订单进行顺利，无需解锁库存
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            }else{
//                让别人再消费
                channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
            }
        } else {
//            本事务内已经回滚，无需解锁操作
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }
    }

    @RabbitHandler
    public void orderCloseToReleaseLock(OrderTo orderTo, Message message, Channel channel) throws IOException {
        System.out.println("收到关闭订单消息....");
//      TODO:释放库存
        try {
            unlockStock(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            e.printStackTrace();
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

    @Transactional
    private void unlockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        WareOrderTaskEntity wareOrderTaskEntity = orderTaskService.getByOrderSn(orderSn);
        Long id = wareOrderTaskEntity.getId();
        List<WareOrderTaskDetailEntity> detailEntities = orderTaskDetailService.list(
                new QueryWrapper<WareOrderTaskDetailEntity>().
                        eq("task_id", id).eq("lock_status", 1)
        );

        for (WareOrderTaskDetailEntity detailEntity : detailEntities) {
            unlockStock(detailEntity.getSkuId(),detailEntity.getWareId(),detailEntity.getSkuNum(),detailEntity.getId());
        }

    }


    private void unlockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {
        wareSkuDao.unlockStock(skuId,wareId,num,taskDetailId);
        WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
        wareOrderTaskDetailEntity.setId(taskDetailId);
        wareOrderTaskDetailEntity.setLockStatus(2);
        orderTaskDetailService.updateById(wareOrderTaskDetailEntity);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();

        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq("sku_id", skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1、判断如果还没有这个库存记录新增
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (entities == null || entities.size() == 0) {
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            //TODO 远程查询sku的名字，如果失败，整个事务无需回滚
            //1、自己catch异常
            //TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
                if (info.getCode() == 0) {
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {

            }
            wareSkuDao.insert(skuEntity);
        } else {
            wareSkuDao.addStock(skuId, wareId, skuNum);

        }


    }

    @Override
    public List<SkuStockTo> getSkusHaveStock(List<Long> skuIds) {

        List<SkuStockTo> skuStockVos = skuIds.stream().map(skuid -> {
            SkuStockTo stockTo = new SkuStockTo();
            stockTo.setSkuId(skuid);
            Long stock = baseMapper.getSkuStock(skuid);
            stockTo.setHasStock(stock == null ? false : stock > 0);
            return stockTo;
        }).collect(Collectors.toList());

        return skuStockVos;
    }

    @Transactional
    @Override
    public Boolean lockStock(LockOrderItemsVo vo) {
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        orderTaskService.save(wareOrderTaskEntity);

        List<OrderItemVo> lockedItems = vo.getLockedItems();
        for (OrderItemVo lockedItem : lockedItems) {
            Boolean itemLocked = false;
            List<Long> wareIds = findWareIdsHasStock(lockedItem.getSkuId());
            if (wareIds == null || wareIds.size() == 0) {
                throw new NotEnoughStockException(lockedItem.getSkuId());
            }
            for (Long wareId : wareIds) {
//                锁库存
                Long count = this.wareSkuDao.lockSkuStock(lockedItem.getSkuId(), wareId, lockedItem.getCount());
                if (count == 0) {
//                    没锁上，尝试下一个ware
                    continue;
                }else{
                    itemLocked = true;
//                   保存锁商品库存记录
                    WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity(null,lockedItem.getSkuId(),"",lockedItem.getCount(),wareOrderTaskEntity.getId(),wareId,1);
                    orderTaskDetailService.save(detailEntity);
//                    锁成功,放进延时队列
//                    构建to对象
                    StockLockTo stockLockTo = new StockLockTo();
                    stockLockTo.setId(wareOrderTaskEntity.getId());
                    StockDetailLockTo stockDetailLockTo = new StockDetailLockTo();
                    BeanUtils.copyProperties(detailEntity,stockDetailLockTo);
                    stockLockTo.setStockLockDetail(stockDetailLockTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange","stock.lock",stockLockTo);
                    break;
                }
            }
            if (!itemLocked){
                throw new NotEnoughStockException(lockedItem.getSkuId());
            }
        }
        return true;
    }

    private List<Long> findWareIdsHasStock(Long skuId) {
        return this.wareSkuDao.selectIdsHasSku(skuId);
    }
}