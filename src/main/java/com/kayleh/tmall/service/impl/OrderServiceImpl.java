package com.kayleh.tmall.service.impl;

import com.kayleh.tmall.mapper.OrderMapper;
import com.kayleh.tmall.mapper.UserMapper;
import com.kayleh.tmall.pojo.Order;
import com.kayleh.tmall.pojo.OrderExample;
import com.kayleh.tmall.pojo.OrderItem;
import com.kayleh.tmall.pojo.User;
import com.kayleh.tmall.service.OrderItemService;
import com.kayleh.tmall.service.OrderService;
import com.kayleh.tmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author: Wizard
 * @Date: 2020/5/6 9:27
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderMapper orderMapper;
    @Autowired
    UserService userService;
    @Autowired
    OrderItemService orderItemService;

    @Override
    public void add(Order order) {
        orderMapper.insert(order);
    }

    @Override
    public void delete(int id) {
        orderMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void update(Order order) {
        orderMapper.updateByPrimaryKeySelective(order);
    }

    @Override
    public Order get(int id) {
        return orderMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<Order> list() {
        OrderExample example =new OrderExample();
        example.setOrderByClause("id desc");
        List<Order> orders =orderMapper.selectByExample(example);
        setUser(orders);
        return orders;
    }

    /**
     * 生成订单，添加事务，避免脏数据
     * @param order
     * @param orderItems
     * @return
     */
    @Override
    //事务
    @Transactional(propagation = Propagation.REQUIRED,rollbackForClassName = "Exception")
    public float add(Order order, List<OrderItem> orderItems) {
        float total = 0;
        add(order);
        if (false){
            throw new RuntimeException();
        }
        for (OrderItem orderItem : orderItems) {
            orderItem.setOid(order.getId());
            orderItemService.update(orderItem);
            total += orderItem.getProduct().getPromotePrice()*orderItem.getNumber();
        }
        return total;
    }

    public void setUser(List<Order> orders) {
        for (Order order : orders) {
            setUser(order);
        }
    }

    public void setUser(Order order) {
//        int uid = getUid();
        User user = userService.get(order.getUid());
        order.setUser(user);
    }

    //排除某个状态的订单后的集合
    @Override
    public List list(int uid, String excludedStatus) {
        OrderExample orderExample = new OrderExample();
        //排除
        orderExample.createCriteria().andStatusNotEqualTo(excludedStatus);
        orderExample.setOrderByClause("id desc");
        return orderMapper.selectByExample(orderExample);
    }
}
