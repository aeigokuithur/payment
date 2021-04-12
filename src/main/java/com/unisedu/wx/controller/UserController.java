package com.unisedu.wx.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.corba.se.impl.ior.WireObjectKeyTemplate;
import com.unisedu.wx.entity.*;
import com.unisedu.wx.repository.*;
import com.unisedu.wx.service.OrderService;
import com.unisedu.wx.utils.OSSUtils;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StudyUserRepository studyUserRepository;
    @Autowired
    private AcademicRepository academicRepository;
    @Autowired
    private JcxkUserRepository jcxkUserRepository;
    @Autowired
    private OrderService orderService;
    @Autowired
    private JcxkRepository jcxkRepository;

    @Value("${image.path}")
    private String imagePath;


    /**
     * 微信小程序 -- 基础学科夏令营获取用户信息
     * @param jcxkUser
     * @return
     */
    @RequestMapping(path = "jcxk",method = RequestMethod.GET)
    public JcxkUser getStudyUser(JcxkUser jcxkUser){
        JcxkUser temp = jcxkUserRepository.findByOpenId(jcxkUser.getOpenId());
        if(temp==null){
            jcxkUser.setSubject("数学，语文");
            jcxkUserRepository.save(jcxkUser);
            return jcxkUser;
        }else{
            return temp;
        }
    }

    /**
     * 微信小程序 -- 基础学科夏令营报名
     * @param user
     */
    @RequestMapping(path = "jcxk",method = RequestMethod.POST)
    public String addUser(JcxkUser user) throws IOException {
        JSONObject json = new JSONObject();
        jcxkUserRepository.save(user);
        ObjectMapper mapper = new ObjectMapper();
        String userJson = mapper.writeValueAsString(user);
        json.put("success",true);
        json.put("user",new JSONObject(userJson));

        //判断是否有资格报名
        if(jcxkRepository.findByCardNumber(user.getCardNumber())!=null){
            //生成订单
            Order order = orderService.findByOpenIdAndWebsite(user.getOpenId(),"jcxk");
            if(order==null){
                order = new Order();
                order.setWebsite("jcxk");
                order.setPrice("4980");
                order.setUserId(user.getOpenId());
                order.setTitle("基础学科夏令营报名费");
                orderService.generateOrder(order);
            }
            String orderJson = mapper.writeValueAsString(order);
            json.put("order",new JSONObject(orderJson));
        }else{
            json.put("success",false);
            json.put("msg","您未获得参赛资格,请登录官网查询获奖成绩");
        }

        return json.toString();
    }


    /**
     * 微信小程序 -- 学术研究营获取用户信息
     * @param academicUser
     * @return
     */
    @RequestMapping(path = "academic",method = RequestMethod.GET)
    public AcademicUser getStudyUser(AcademicUser academicUser){
        AcademicUser temp = academicRepository.findByOpenId(academicUser.getOpenId());
        if(temp==null){
            academicRepository.save(academicUser);
            return academicUser;
        }else{
            return temp;
        }
    }

    /**
     * 微信小程序 -- 学术研究营报名
     * @param user
     * @return
     * @throws IOException
     */
    @RequestMapping(path = "academic",method = RequestMethod.POST)
    public String addUser(AcademicUser user) throws IOException {
        JSONObject json = new JSONObject();
        academicRepository.save(user);
        ObjectMapper mapper = new ObjectMapper();
        String userJson = mapper.writeValueAsString(user);
        json.put("success",true);
        json.put("user",new JSONObject(userJson));

        //生成订单
        Order order = orderService.findByOpenIdAndWebsite(user.getOpenId(),"academic");
        if(order == null){
            order = new Order();
            order.setWebsite("academic");
            order.setPrice("4980");
            order.setUserId(user.getOpenId());
            order.setTitle("学术作品营报名费");
            orderService.generateOrder(order);
        }
        String orderJson = mapper.writeValueAsString(order);
        json.put("order",new JSONObject(orderJson));
        return json.toString();
    }

    /**
     * 微信小程序 -- 研究性学习营获取用户信息
     * @param studyUser
     * @return
     */
    @RequestMapping(path = "study",method = RequestMethod.GET)
    public StudyUser getStudyUser(StudyUser studyUser){
        StudyUser temp = studyUserRepository.findByOpenId(studyUser.getOpenId());
        if(temp==null){
            studyUser.setType(0);
            studyUserRepository.save(studyUser);
            return studyUser;
        }else{
            return temp;
        }
    }

    /**
     * 微信小程序 -- 研究性学习营报名
     * @param user
     * @return
     * @throws IOException
     */
    @RequestMapping(path = "study",method = RequestMethod.POST)
    public String addUser(StudyUser user) throws IOException {
        JSONObject json = new JSONObject();
        studyUserRepository.save(user);
        ObjectMapper mapper = new ObjectMapper();
        String userJson = mapper.writeValueAsString(user);
        json.put("success",true);
        json.put("user",new JSONObject(userJson));

        //生成订单
        Order order = orderService.findByOpenIdAndWebsite(user.getOpenId(),"study");
        if(order == null){
            order = new Order();
            order.setWebsite("study");
            order.setPrice("9800");
            order.setUserId(user.getOpenId());
            order.setTitle("青少年研究性学习营报名费");
            orderService.generateOrder(order);
        }
        String orderJson = mapper.writeValueAsString(order);
        json.put("order",new JSONObject(orderJson));
        return json.toString();

    }

    @RequestMapping(path = "/list", method = RequestMethod.GET)
    public Iterable<User> getAllUsers(){
        return userRepository.findAll();
    }

    /**
     * 微信小程序，获取openid
     * @param request
     * @param response
     */
    @RequestMapping(path = "openid")
    public String wxLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String code = request.getParameter("code");
        Map<String,String> data = new HashMap<>();
        data.put("appid","wx35ea369e7e926188");
        data.put("secret","0f566068c13a7461409c09b66d4d8628");
        data.put("js_code",code);
        data.put("grant_type","unisedu");

        Connection.Response res = Jsoup.connect(Global.WX_LOGIN_GATEWAY)
                .timeout(20000)
                .data(data)
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .execute();
        JSONObject json = new JSONObject(res.body());
        String openid = json.getString("openid");

        if(openid!=null){
           return openid;


        }else{
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "500";
        }
    }

    /**
     *
     * @return
     */
    @RequestMapping(path = "uploadImage")
    public String uploadImage(MultipartFile file, StandardMultipartHttpServletRequest request) throws Exception {
        JSONObject json = new JSONObject();
        file = request.getFile("photo");
        String fileName = file.getOriginalFilename();
        String openId = request.getParameter("openId");
        if(file.getSize()/1024 < 3000){
            String suffix = fileName.substring(fileName.lastIndexOf("."));
            String filepath = "jcxk/user/"+openId+suffix;
            OSSUtils.putObject(filepath,file.getInputStream());
            json.put("success",true);
            json.put("photo","http://pic.unisedu.com/"+filepath);
        }else{
            json.put("success",false);
        }


        return json.toString();
    }

    @RequestMapping(path = "showPhoto", method = RequestMethod.GET)
    public void showPhoto(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String openId = request.getParameter("openid");
        JcxkUser user = jcxkUserRepository.findByOpenId(openId);
        File file = new File(user.getPhoto());
        if(file.exists()){
            ServletOutputStream out =  response.getOutputStream();
            FileInputStream stream = new FileInputStream(file);
            int len = 0;
            byte[] buffer = new byte[1024];
            while((len = stream.read(buffer))!=-1){
                out.write(buffer,0,len);
                out.flush();
            }
            out.close();
            stream.close();
        }

    }
}
