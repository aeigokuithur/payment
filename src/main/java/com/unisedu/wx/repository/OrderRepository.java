package com.unisedu.wx.repository;

import com.unisedu.wx.entity.Order;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrderRepository extends CrudRepository<Order, Long> {

    Order findByOrderNo(String orderNo);

    Order findByUserId(String userId);

    Order findByUserIdAndWebsite(String userId, String website);

    List<Order> findOrdersByUserId(String userId);

}
