package com.unisedu.wx.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unisedu.wx.entity.Global;
import com.unisedu.wx.entity.Order;
import com.unisedu.wx.enumration.WebSite;
import com.unisedu.wx.repository.OrderRepository;
import com.unisedu.wx.utils.MD5Utils;
import com.unisedu.wx.utils.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

@Service
public class OrderService {

    Logger logger = Logger.getLogger(OrderService.class);
    private final String WX_GATEWAY = "https://api.mch.weixin.qq.com/pay/unifiedorder";

    @Autowired
    private OrderRepository orderRepository;

    public Order findByOpenId(String openid){
        return orderRepository.findByUserId(openid);
    }

    public Order findByOpenIdAndWebsite(String openid,String website){
        return orderRepository.findByUserIdAndWebsite(openid,website);
    }

    public List<Order> findOrdersByOpenId(String openid){
        return orderRepository.findOrdersByUserId(openid);
    }


    /**
     * 生成订单
     */
    public void generateOrder(Order order){
        WebSite ws = WebSite.getInstance(order.getWebsite());
        String orderNo;
        if(order.getWebsite().equals("study")||order.getWebsite().equals("academic")||order.getWebsite().equals("jcxk")){
            orderNo = getOrderNo(ws.getPrefix());
        }else{
            orderNo = getOrderNo(ws.getPrefix(),order.getUserId());
        }
        order.setOrderNo(orderNo);
        order.setCreateTime(new Date());
        order.setStatus("0");
        order.setWebsite(ws.getPrefix());
        orderRepository.save(order);
        logger.warn("生成订单：{订单编号："+order.getOrderNo()+", 所属网站："+ws.getPrefix()+"}");
    }

    /**
     * 获取支付二维码
     * 如果订单号不存在，则生成订单，如存在则根据订单号获取订单，
     * 之后调用支付宝和微信统一下单接口获取二维码
     * @return
     */
    public JSONObject getPayCode(Order order){
        JSONObject json = new JSONObject();
        System.out.println(order);
        if(order.getOrderNo() == null){
            generateOrder(order);
        }else{
            order = orderRepository.findByOrderNo(order.getOrderNo());
        }
        System.out.println(order);
        String wxCode = getWxCode(order);
        String alipayCode = getAlipayCode(order);

        json.put("wxCode",wxCode);
        json.put("alipayCode",alipayCode);
        ObjectMapper mapper = new ObjectMapper();
        try {
            String orderJson = mapper.writeValueAsString(order);
            json.put("order",new JSONObject(orderJson));
        } catch (JsonProcessingException e) {
            logger.error("获取二维码失败：{订单编号："+order.getOrderNo()+", 所属网站："+order.getWebsite()+"}");
            e.printStackTrace();
        }
        return json;
    }

    public String wxWebkitPay(Order order,String ip){
        SortedMap<String ,String> signParam = new TreeMap<>();
        signParam.put("out_trade_no", order.getOrderNo());
        signParam.put("appid", "wx35ea369e7e926188");
        signParam.put("mch_id", "1405244802");
        signParam.put("total_fee",Global.formatWxOrderPrice(order.getPrice()));
        signParam.put("trade_type", "JSAPI");
        signParam.put("nonce_str", Global.getNonceStr());
        signParam.put("body", order.getTitle());
        signParam.put("openid", order.getUserId());
        signParam.put("notify_url", "http://pay.unisedu.com/order/wxNotify");
        signParam.put("spbill_create_ip",ip);
        signParam.put("sign_type","MD5");
        String sign = StringUtils.createSign(signParam,"eddf1eab810d21fa83ccd4766d8fa1e0");
        signParam.put("sign",sign);
        String xml = StringUtils.httpsRequest(WX_GATEWAY,"POST",StringUtils.getMapToXML(signParam));
        return StringUtils.doXMLParse(xml).get("prepay_id").toString();
    }


    /**
     * 获取微信支付二维码
     * @param order 订单实体，用于构建统一下单参数
     * @return
     */
    private String getWxCode(Order order){
        SortedMap<String ,String> signParam = new TreeMap<>();
        signParam.put("appid", Global.getConfig("wxAppId"+order.getCompany()));
        signParam.put("body", order.getTitle());
        signParam.put("mch_id", Global.getConfig("wxMchId"+order.getCompany()));
        signParam.put("nonce_str", Global.getNonceStr());
        signParam.put("notify_url", Global.getConfig("wxNotifyUrl"+order.getCompany()));
        signParam.put("out_trade_no", order.getOrderNo());
        signParam.put("product_id", String.valueOf(order.getId()));
        signParam.put("total_fee",Global.formatWxOrderPrice(order.getPrice()));
        signParam.put("trade_type", "NATIVE");
        String sign = StringUtils.createSign(signParam,Global.getConfig("wxPayKey"+order.getCompany()));
        signParam.put("sign", sign);
        String xml = StringUtils.httpsRequest(WX_GATEWAY,"POST",StringUtils.getMapToXML(signParam));
        return StringUtils.doXMLParse(xml).get("code_url").toString();
    }



    /**
     * 获取支付宝二维码
     * @param order 订单实体，用于构建统一下单参数
     * @return
     */
    private String getAlipayCode(Order order){
        String out_trade_no = "PAY"+System.currentTimeMillis();	//商户订单号

        Map<String,String> params = new HashMap<String, String>();
        params.put("service","create_direct_pay_by_user");
        params.put("partner",Global.getConfig("alipayParentId"+order.getCompany()));
        params.put("subject",order.getTitle());
        params.put("out_trade_no",out_trade_no);
        params.put("seller_email","zhifubao@unisedu.cn");
        params.put("total_fee",order.getPrice());
        params.put("notify_url",Global.getConfig("alipayNotifyUrl"+order.getCompany()));
        params.put("qr_pay_mode","4");
        params.put("qrcode_width","125");
        params.put("paymethod","directPay");
        params.put("extra_common_param",order.getOrderNo());
        params.put("_input_charset","UTF-8");
        String sign = MD5Utils.MD5Encode(StringUtils.getContent(params, Global.getConfig("alipayKey"+order.getCompany())),"UTF-8");

        String parameter = "https://mapi.alipay.com/gateway.do?";
        List<String> keys = new ArrayList<String>(params.keySet());
        for (int i = 0; i < keys.size(); i++) {
            try {
                parameter = String.valueOf(parameter) + keys.get(i) + "=" + URLEncoder.encode(params.get(keys.get(i)), "UTF-8") + "&";
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        parameter += "sign=" + sign + "&sign_type=MD5";
        return parameter;
    }

    /**
     * 生成订单号
     * @param prefix 订单号前缀，以大赛拼音简写命名
     * @param userId 用户Id
     * @return orderNo
     */
    private String getOrderNo(String prefix,String userId){
        return prefix+"-"+userId+"-"+System.currentTimeMillis();
    }

    private String getOrderNo(String prefix){
        return prefix+"-"+System.currentTimeMillis();
    }

    /**
     * 解析微信支付回调地址
     * @param request
     * @param response
     */
    public void parseWXNofity(HttpServletRequest request, HttpServletResponse response){
        String requestStr = readRequest(request);
        if(requestStr!=null){
            Map<String,Object> res = StringUtils.doXMLParse(requestStr);
            if(res.get("return_code").toString().equals("SUCCESS")){
                Order order = orderRepository.findByOrderNo(res.get("out_trade_no").toString());
                if(order.getStatus().equals("0")){
                    order.setStatus("2");
                    orderRepository.save(order);
                    logger.error("微信支付回调成功：{订单编号："+order.getOrderNo()+", 所属网站："+order.getWebsite()+"}");

                    if(order.getCompany()!=null){
                        Map<String,String> map = new HashMap<>();
                        map.put("return_code","SUCCESS");
                        map.put("order_no",order.getOrderNo());
                        //通知挂载网站
                        try {
                            Jsoup.connect(Global.getConfig("notifyUrl"+order.getCompany()))
                                    .timeout(20000)
                                    .data(map)
                                    .ignoreContentType(true)
                                    .method(Connection.Method.POST)
                                    .execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
                WxSuccess(response);
            }
        }
    }

    /**
     * 微信支付成功，通知微信服务端
     */
    public boolean WxSuccess(HttpServletResponse response){
        //通知微信订单交易成功
        String resXml = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(response.getOutputStream());
            out.write(resXml.getBytes());
            out.flush();
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void parseAlipayNotify(HttpServletRequest request,HttpServletResponse response){
        String orderNo = request.getParameter("extra_common_param");// 扩展信息存用户的id
        Order order = orderRepository.findByOrderNo(orderNo);
        logger.warn("支付宝回调："+orderNo);
        try {
            if (getAlipayCheckInfo(request,order.getCompany())) {

                if (request.getParameter("trade_status").equals("TRADE_FINISHED") || request.getParameter("trade_status").equals("TRADE_SUCCESS")) {

                    if(order!=null){
                        order.setStatus("2");
                        orderRepository.save(order);
                        logger.warn("支付宝支付回调成功：{订单编号："+order.getOrderNo()+", 所属网站："+order.getWebsite()+"}");
                        //通知支付宝支付成功
                        response.setCharacterEncoding("UTF-8");
                        PrintWriter writer = response.getWriter();
                        writer.write("success");
                        writer.flush();
                        writer.close();

                        //通知挂载网站
                        Map<String,String> map = new HashMap<>();
                        map.put("return_code","SUCCESS");
                        map.put("order_no",order.getOrderNo());
                        //通知挂载网站
                        try {
                            Jsoup.connect(Global.getConfig("notifyUrl"+order.getCompany()))
                                    .timeout(20000)
                                    .data(map)
                                    .ignoreContentType(true)
                                    .method(Connection.Method.POST)
                                    .execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("支付宝支付回调失败：{订单编号："+orderNo+"}");
            e.printStackTrace();
        }
    }

    public boolean getAlipayCheckInfo(HttpServletRequest request,Integer company) throws Exception{
        String partner = Global.getConfig("alipayParentId"+company);
        String privateKey = Global.getConfig("alipayKey"+company);
        String alipayNotifyURL = "http://notify.alipay.com/trade/notify_query.do?" + "partner=" + partner + "&notify_id=" + request.getParameter("notify_id");
        String responseTxt = check(alipayNotifyURL);
        if(!responseTxt.equals("true")){

            return false;
        }
        Map<String, Object> params = new HashMap<String, Object>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            //中文商品名称 转码
            if ("subject".equals(name.trim())){
                valueStr= URLDecoder.decode(valueStr,"gb2312");
            }else {
                // 乱码解决，这段代码在出现乱码时使用,但是不一定能解决所有的问题，所以建议写过滤器实现编码控制。
                // 如果mysign和sign不相等也可以使用这段代码转化
                valueStr = new String(valueStr.getBytes("ISO-8859-1"), "UTF-8"); // 乱码解决
            }
            params.put(name, valueStr);
        }
        // 验证加密签名
        String mysign = sign(params, privateKey);
        if(!mysign.equals(request.getParameter("sign"))){
            System.out.println("验证加密签名失败");
            return false;
        }

        return true;
    }

    /**
     * 验证加密签名
     * @param params
     * @param privateKey
     * @return
     */
    public String sign(Map params, String privateKey) {
        Properties properties = new Properties();
        Iterator content = params.keySet().iterator();

        while(content.hasNext()) {
            String name = (String)content.next();
            Object value = params.get(name);
            if(name != null && !name.equalsIgnoreCase("sign") && !name.equalsIgnoreCase("sign_type")) {
                properties.setProperty(name, value.toString());
            }
        }

        String content1 = getSignatureContent(properties);
        return sign(content1, privateKey);
    }

    public String sign(String content, String privateKey) {
        if(privateKey == null) {
            return null;
        } else {
            String signBefore = content + privateKey;
            return MD5Utils.MD5Encode(signBefore,"UTF-8");
        }
    }

    public String getSignatureContent(Properties properties) {
        StringBuffer content = new StringBuffer();
        ArrayList keys = new ArrayList(properties.keySet());
        Collections.sort(keys);

        for(int i = 0; i < keys.size(); ++i) {
            String key = (String)keys.get(i);
            String value = properties.getProperty(key);
            content.append((i != 0?"&":"") + key + "=" + value);
        }

        return content.toString();
    }

    /**
     * 校验支付宝回调链接
     * @param urlvalue
     * @return
     */
    public String check(String urlvalue) {
        String inputLine = "";
        try {
            URL e = new URL(urlvalue);
            HttpURLConnection urlConnection = (HttpURLConnection)e.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            inputLine = in.readLine().toString();
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return inputLine;
    }


    /**
     * 读取Request报文
     * @param request
     * @return
     */
    public String readRequest (HttpServletRequest request){
        try {
            BufferedReader reader = request.getReader();
            String line = "";
            StringBuffer sb = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            request.getReader().close();
            return sb.toString();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据订单号获取订单
     * @param orderNo
     * @return
     */
    public Order findByOrderNo(String orderNo){
        return orderRepository.findByOrderNo(orderNo);
    }
}
