package com.unisedu.wx.controller;

import com.unisedu.wx.entity.Global;
import com.unisedu.wx.entity.Order;
import com.unisedu.wx.service.OrderService;
import com.unisedu.wx.utils.MD5Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(path = "/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @RequestMapping(path = "code",method = RequestMethod.POST)
    public String getPayCode(Order order){
        JSONObject json = orderService.getPayCode(order);
        return json.toString();
    }

    @RequestMapping(path = "wxNotify")
    public void wxNotify(HttpServletRequest request, HttpServletResponse response){
        orderService.parseWXNofity(request,response);
    }

    @RequestMapping(path = "alipayNotify")
    public void alipayNotify(HttpServletRequest request, HttpServletResponse response){
        orderService.parseAlipayNotify(request,response);
    }

    @RequestMapping(path = "wxWebkit")
    public String wxWebkitPay(HttpServletRequest request){
        String orderNo = request.getParameter("orderNo");
        String ip = request.getRemoteHost();
        Order order = orderService.findByOrderNo(orderNo);
        String prepayId = orderService.wxWebkitPay(order,ip);

        JSONObject json = new JSONObject();
        json.put("timestamp",new Date().getTime()+"");
        json.put("nonceStr", Global.getNonceStr());
        json.put("prepayId",prepayId);
        String str = "appId=wx35ea369e7e926188&nonceStr="+json.getString("nonceStr")+"&package=prepay_id="+json.getString("prepayId")+"&signType=MD5&timeStamp="+json.getString("timestamp")+"&key=eddf1eab810d21fa83ccd4766d8fa1e0";
        String paySign = MD5Utils.MD5Encode(str,"UTF-8");
        json.put("paySign",paySign);
        return json.toString();
    }

    @RequestMapping(path = "hasOrder")
    public boolean hasOrder(HttpServletRequest request){
        Order order = orderService.findByOpenIdAndWebsite(request.getParameter("openid"),request.getParameter("website"));
        return order != null;
    }

    @RequestMapping(path = "findOrdersByOpenId")
    public List<Order> findOrdersByOpenId(HttpServletRequest request){
        return orderService.findOrdersByOpenId(request.getParameter("openid"));
    }

}
