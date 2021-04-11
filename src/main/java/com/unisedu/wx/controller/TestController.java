package com.unisedu.wx.controller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(path = "/test")
public class TestController {

    private Logger logger = Logger.getLogger(TestController.class);

    @RequestMapping(path = "index")
    public String index(){
        logger.debug("debug");
        logger.info("info");
        logger.warn("warn");
        logger.error("error");
        logger.fatal("fatal");

        return "index";
    }
}
