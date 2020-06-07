package com.atguigu.gulimall.order.service.impl;

import com.atguigu.common.exception.NotEnoughStockException;
import com.atguigu.common.to.SkuStockTo;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.SecKillOrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.OauthMember;
import com.atguigu.common.vo.OrderWithItemsVo;
import com.atguigu.gulimall.order.constan.OrderConstant;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.enume.OrderStatusEnum;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WareFeignService;
import com.atguigu.gulimall.order.interceptor.OrderWebInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.to.OrderCreateTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


/**
 * 本地事务失效问题：
 * 同一个对象内事务方法互调默认失败，原因：绕过了代理对象，事务采用代理对象来控制
 * 解决：使用代理对象调用事务方法
 * 1) 引入aop-starter，采用aspectJ
 * 2）加入EnableAspectJAutoProxy(exposeProxy=true)
 * 3) 本类互调
 * OrderServiceImpl orderService = (OrderServiceImpl)AopContext.currentProxy()
 * public void a(){
 *     orderService.b()
 *     orderService.c()
 * }
 */
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    ThreadLocal<SubmitOrderVo> submitOrderVoThreadLocal = new ThreadLocal<>();

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        OauthMember user = OrderWebInterceptor.loginUser.get();

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);//防止丢失请求上下文
//        住址信息
            ReceiverAddrVo receiverAddrVo = memberFeignService.memberAddressInfo(user.getId());
            confirmVo.setAddress(Arrays.asList(receiverAddrVo));
        });

        CompletableFuture<Void> itemsFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);//防止丢失请求上下文
//        购物车商品列表
            List<OrderItemVo> orderItemVos = cartFeignService.cartItems();
            confirmVo.setItems(orderItemVos);
        }).thenRunAsync(()->{
            List<Long> skuIds = confirmVo.getItems().stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            List<SkuStockTo> skusHaveStock = wareFeignService.getSkusHaveStock(skuIds);
            Map<Long, Boolean> stockMap = skusHaveStock.stream().collect(Collectors.toMap(SkuStockTo::getSkuId, SkuStockTo::getHasStock));
            confirmVo.setStocks(stockMap);
        });

//        会员信息
        confirmVo.setIntegration(user.getIntegration());

//        令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConstant.ORDER_TOKEN_KEY_PREFIX+user.getId(),token,30, TimeUnit.MINUTES);
        confirmVo.setOrderToken(token);
        CompletableFuture.allOf(itemsFuture, addressFuture).get();
        return confirmVo;
    }

//    @GlobalTransactional
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(SubmitOrderVo vo) {
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        submitOrderVoThreadLocal.set(vo);
        OauthMember user = OrderWebInterceptor.loginUser.get();
        String orderToken = vo.getOrderToken();
//        lua脚本执行原子操作,防止重复提交
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        Long execute = redisTemplate.execute(new DefaultRedisScript<Long>(script,Long.class), Arrays.asList(OrderConstant.ORDER_TOKEN_KEY_PREFIX + user.getId().toString()), orderToken);
        if (execute == 0) {
//          验证失败
            responseVo.setCode(1L);
        }else{
//          验证成功
//          创建订单，验证令牌，锁库存，验证价格
            OrderCreateTo createTo = createOrder();
//          验价
            BigDecimal payPriceFromFront = vo.getPayPrice();
            BigDecimal payPriceFromBack = createTo.getOrder().getPayAmount();
            if(Math.abs(payPriceFromBack.subtract(payPriceFromFront).doubleValue())<0.01){
//              金额对比正确
//              保存订单
                saveOrder(createTo);
//              库存锁定
                LockOrderItemsVo lockOrderItemsVo = new LockOrderItemsVo();
                lockOrderItemsVo.setOrderSn(createTo.getOrder().getOrderSn());
                List<OrderItemVo> collect = createTo.getOrderItems().stream().map(item -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                lockOrderItemsVo.setLockedItems(collect);
//                锁库存
                R r = wareFeignService.lockStock(lockOrderItemsVo);
                if (r.getCode() == 0) {
//                    锁库存成功
                    responseVo.setOrder(createTo.getOrder());
                } else {
                    responseVo.setCode(3L);
                    throw new NotEnoughStockException(null);
                }

            }else {
//               金额对比失败
                responseVo.setCode(2L);
            }
        }
        System.out.println(responseVo);
//        int a = 10/0;
//      延时关单
        try {
            rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", responseVo.getOrder());
        } catch (AmqpException e) {
            e.printStackTrace();
//            TODO: 记录消息到数据库，定时重发消息
        }

        return responseVo;
    }

    @Override
    public OrderEntity getByOrderSn(String orderSn) {
        return this.baseMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn",orderSn));
    }

    @Override
    public void releaseOrder(OrderEntity orderEntity) {
//        最新order信息
        OrderEntity byId = this.getById(orderEntity.getId());
        if (byId != null && byId.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()) {
            OrderEntity update = new OrderEntity();
            update.setId(orderEntity.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(update);
//        TODO 确认释放库存 routing-key:order.release.other.#
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(byId,orderTo);
//            关单后立马发消息给库存解锁
            rabbitTemplate.convertAndSend("order-event-exchange","order.release.other.stock",orderTo);
        }
    }

    @Override
    public List<OrderWithItemsVo> findOrdersWithItems(Map<String, Object> map) {
        OauthMember oauthMember = OrderWebInterceptor.loginUser.get();
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(map),
                new QueryWrapper<OrderEntity>().eq("member_id", oauthMember.getId())
        );
        List<OrderEntity> orderEntities = page.getRecords();
        List<OrderWithItemsVo> withItemsVos = orderEntities.stream().map(orderEntity -> {
            OrderWithItemsVo orderWithItemsVo = new OrderWithItemsVo();
            BeanUtils.copyProperties(orderEntity, orderWithItemsVo);
            String orderSn = orderEntity.getOrderSn();
            List<OrderItemEntity> itemEntities = orderItemService.findItemsBySn(orderSn);
            List<com.atguigu.common.vo.OrderItemVo> orderItemVos = itemEntities.stream().map(item -> {
                com.atguigu.common.vo.OrderItemVo itemVo = new com.atguigu.common.vo.OrderItemVo();
                BeanUtils.copyProperties(item, itemVo);
                return itemVo;
            }).collect(Collectors.toList());
            orderWithItemsVo.setItems(orderItemVos);
            return orderWithItemsVo;
        }).collect(Collectors.toList());

        return withItemsVos;
    }

    @Override
    public void payed(String sn) {
        OrderEntity update = new OrderEntity();
        OrderEntity byOrderSn = this.getByOrderSn(sn);
        update.setId(byOrderSn.getId());
        update.setStatus(OrderStatusEnum.PAYED.getCode());
        this.updateById(update);
    }

    @Override
    public void createSecKillOrder(SecKillOrderTo secKillOrder) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setCreateTime(new Date());
        orderEntity.setMemberId(secKillOrder.getMemberId());
        orderEntity.setOrderSn(secKillOrder.getOrderSn());
        orderEntity.setPayAmount(secKillOrder.getSeckillPrice());
        orderEntity.setTotalAmount(secKillOrder.getSeckillPrice());
        this.save(orderEntity);

        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderSn(secKillOrder.getOrderSn());
        orderItemEntity.setSkuQuantity(secKillOrder.getNum().intValue());
        orderItemEntity.setRealAmount(secKillOrder.getSeckillPrice());
        orderItemService.save(orderItemEntity);

    }

    private void saveOrder(OrderCreateTo createTo) {
        createTo.getOrder().setModifyTime(new Date());
        this.save(createTo.getOrder());
        orderItemService.saveBatch(createTo.getOrderItems());
    }

    private OrderCreateTo createOrder() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        String orderSn = IdWorker.getTimeId();
//      创建OrderEntity
        OrderEntity orderEntity = buildOrderEntity(orderSn);
//      创建orderItemEntiy
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);
//      计算价格
        computePrice(orderEntity,orderItemEntities);
        orderCreateTo.setOrder(orderEntity);
        orderCreateTo.setOrderItems(orderItemEntities);
        orderCreateTo.setPayPrice(orderEntity.getPayAmount());

        return orderCreateTo;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        BigDecimal total = new BigDecimal("0.00");
        for (OrderItemEntity item : orderItemEntities) {
            BigDecimal itemRealAmount = item.getRealAmount();
            total = total.add(itemRealAmount);
        }
        orderEntity.setPayAmount(total);

    }

    private OrderEntity buildOrderEntity(String orderSn) {
        OauthMember oauthMember = OrderWebInterceptor.loginUser.get();

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setMemberId(oauthMember.getId());
        orderEntity.setOrderSn(orderSn);
        SubmitOrderVo submitVo = submitOrderVoThreadLocal.get();
//      TODO: 设置收货信息
//        远程调用
        orderEntity.setReceiverCity(submitVo.getAddrId().toString());
        orderEntity.setReceiverPostCode(submitVo.getAddrId().toString());
        orderEntity.setReceiverRegion(submitVo.getAddrId().toString());

        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setAutoConfirmDay(7);
        orderEntity.setCreateTime(new Date());
        return orderEntity;
    }

    private List<OrderItemEntity> buildOrderItems(String orderSn) {
//        重新从购物车取出当前用户所有商品
        List<OrderItemVo> orderItemVos = cartFeignService.cartItems();
        if (orderItemVos != null && orderItemVos.size() > 0) {
            List<OrderItemEntity> orderItemEntities = orderItemVos.stream().map(orderItemVo ->{
                OrderItemEntity orderItemEntity = buildOrderItem(orderItemVo);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());

            return orderItemEntities;
        }

        return null;
    }

    private OrderItemEntity buildOrderItem(OrderItemVo vo) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();

        Long skuId = vo.getSkuId();
        R r = productFeignService.getSpuBySkuId(skuId);
        SpuInfoVo spuInfo = r.getDataInType("spuInfo", SpuInfoVo.class);
        orderItemEntity.setSpuId(spuInfo.getId());
        orderItemEntity.setSpuName(spuInfo.getSpuName());
        orderItemEntity.setSpuBrand(spuInfo.getBrandId().toString());
        orderItemEntity.setCategoryId(spuInfo.getCatalogId());

//        sku info
        orderItemEntity.setSkuId(vo.getSkuId());
        orderItemEntity.setSkuName(vo.getTitle());
        orderItemEntity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(vo.getSkuAttr(),";"));
        orderItemEntity.setSkuQuantity(vo.getCount());
        orderItemEntity.setSkuPrice(vo.getPrice());
        orderItemEntity.setSkuPic(vo.getImg());

//        积分
        orderItemEntity.setGiftGrowth(vo.getPrice().intValue());
        orderItemEntity.setGiftIntegration(vo.getPrice().intValue());

//        价格
        BigDecimal real = orderItemEntity.getSkuPrice().multiply(new BigDecimal(vo.getCount()));
        orderItemEntity.setRealAmount(real);

        return orderItemEntity;
    }

}