package com.llq.community.controller;

import com.llq.community.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sun.security.krb5.internal.PAData;

import java.awt.event.MouseEvent;
import java.util.Date;

/**
 * @author llq
 * @create 2021-09-18  20:15
 */
@Controller
public class DataController {
    @Autowired
    private DataService dataService;

    //统计页面
    //既可以接收get请求也可以接收别的界面转发过来的post请求
    @RequestMapping(path = "/data", method = {RequestMethod.GET,RequestMethod.POST})
    public String getDataPage() {
        return "/site/admin/data";
    }

    //统计网站的访问量
    @RequestMapping(path = "/data/uv", method = RequestMethod.POST)
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd")Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd")Date end,
                        Model model){
        long uv = dataService.calculateUV(start, end);
        model.addAttribute("uvResult", uv);
        model.addAttribute("uvStartDate",start);
        model.addAttribute("uvEndDate",end);

        return "forward:/data"; //这个请求转发就是post请求（请求转发是不会改变请求方式的）
    }

    // 统计活跃用户
    @RequestMapping(path = "/data/dau", method = RequestMethod.POST)
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                         @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model) {
        long dau = dataService.calculateDAU(start, end);
        model.addAttribute("dauResult", dau);
        model.addAttribute("dauStartDate", start);
        model.addAttribute("dauEndDate", end);
        return "forward:/data";
    }



}
