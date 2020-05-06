package com.kayleh.tmall.service;

import com.kayleh.tmall.pojo.Order;

import java.util.List;

/**
 * @Author: Wizard
 * @Date: 2020/5/6 9:24
 */
public interface OrderService {
    String waitPay = "waitPay";//等待付款
    String waitDelivery = "waitDelivery";//等待发货
    String waitConfirm = "waitConfirm";//等待确认
    String waitReview = "waitReview";//等待评论
    String finish = "finish";
    String delete = "delete";
    void add(Order order);

    void delete(int id);
    void update(Order order);
    Order get(int id);
    List list();
}