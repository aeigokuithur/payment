package com.unisedu.wx.entity;

import com.unisedu.wx.utils.MD5Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Global {

    private static Map<String,String> map = new HashMap<>();

    public static final String WX_LOGIN_GATEWAY = "https://api.weixin.qq.com/sns/jscode2session";

    static {
        //清大紫育微信支付参数
        map.put("wxAppId1","wx07c62eb622bd166b");
        map.put("wxMchId1","1405244802");
        map.put("wxPayKey1","eddf1eab810d21fa83ccd4766d8fa1e0");
        map.put("wxNotifyUrl1","http://pay.unisedu.com/order/wxNotify");

        //清大紫育支付宝支付参数
        map.put("alipayParentId1","2088021653426540");
        map.put("alipayKey1","37wk34wjpw9nzgd7vublfzyj4zju2ilb");
        map.put("alipayNotifyUrl1","http://pay.unisedu.com/order/alipayNotify");

        map.put("notifyUrl1","http://www.kepukehuan.com/order/bsttyobs");


    }

    /**
     * 获取配置
     * @param key
     * @return
     */
    public static String getConfig(String key){
        return map.get(key);
    }

    /**
     * 获取随机字符串
     * @return
     */
    public static String getNonceStr(){
        Random random = new Random();
        return MD5Utils.MD5Encode(String.valueOf(random.nextInt(10000)), "UTF-8");
    }

    /**
     * 格式化微信订单价格
     * @param price
     * @return
     */
    public static String formatWxOrderPrice(String price){
        float f = Float.valueOf(price);
        f = f*100;
        price = String.valueOf(f);
        return price.replaceAll("\\..*","");
    }
}
