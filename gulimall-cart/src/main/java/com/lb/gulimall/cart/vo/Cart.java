package com.lb.gulimall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车
 */
public class  Cart {
    private List<CartItem> items;
    private Integer countNum;//商品数量
    private Integer countType;//商品类型数量
    private BigDecimal totalAmount;//商品总价
    private BigDecimal reduce=new BigDecimal("0");//优惠
    private BigDecimal checkedTotalAmount;//被选中的商品总价

    public BigDecimal getCheckedTotalAmount() {
        checkedTotalAmount = new BigDecimal("0");
        if (items!=null&&items.size()>0){
            for (CartItem item : items) {
                if (item.getCheck()){
                    BigDecimal totalPrice = item.getTotalPrice();
                    checkedTotalAmount.add(totalPrice);
                }
            }
        }
        return checkedTotalAmount;
    }


    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count=0;
        if (items!=null&&items.size()>0){
            for (CartItem item : items) {
                count+=item.getCount();
            }
            return count;
        }else {
            return 0;
        }
    }


    public Integer getCountType() {
        if (items==null){
            return 0;
        }
        return items.size();
    }


    public BigDecimal getTotalAmount() {
        if (items==null||items.size()==0){
            return new BigDecimal("0");
        }
        BigDecimal bigDecimal = new BigDecimal("0");
        for (CartItem item : items) {
            BigDecimal totalPrice = item.getTotalPrice();
            bigDecimal=bigDecimal.add(totalPrice);
        }
        return bigDecimal.subtract(getReduce());
    }


    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
