package com.atguigu.gulimall.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author: Justin
 */
public class Cart {

    private List<CartItem> items;

    private Integer itemsCount;

    private Integer typesCount;

    private BigDecimal totalPrice;

    private BigDecimal reduce = new BigDecimal("0.00");

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getItemsCount() {
        if (items == null || items.size() == 0) {
            return  0;
        }

        int count = 0;
        for (CartItem item : items) {
            count += item.getCount();
        }
        return count;
    }


    public Integer getTypesCount() {

        return items.size();
    }


    public BigDecimal getTotalPrice() {
        BigDecimal total = new BigDecimal("0.00");
        for (CartItem item : items) {
            total = total.add(item.getTotalPrice());
        }
        BigDecimal result = total.subtract(getReduce());

        return result;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
