package com.llq.community.controller;

import com.llq.community.dao.UserMapper;
import com.llq.community.entity.User;
import com.llq.community.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author llq
 * @create 2021-08-26  14:35
 */
@RequestMapping("/test")
//@RestController 有了这个注解，这个类里面的方法返回的都是json格式,一般情况还是不要用这个。很多方法是要跳转页面的
@Controller
public class TestController {
    @Autowired
    private TestService testService;

    //测试一下spring的Ioc
    @RequestMapping("/hello")
    @ResponseBody
    public String test() {
        return testService.find();
    }

    //获取http请求中的数据
    @RequestMapping("/http")
    public void getHttp(HttpServletRequest request, HttpServletResponse response) throws Exception {
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());

        response.setContentType("text/html;charset=utf-8"); // 设置响应返回的文本形式和编码集
        PrintWriter writer = response.getWriter();
        writer.write("<h1>我是傻逼<h1>");
    }

    //获取get请求的参数
    @RequestMapping(path = "/getStudent", method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@RequestParam(name = "Name", required = false, defaultValue = "llq")
                                     String name,
                             @RequestParam(name = "Age", required = false, defaultValue = "18")
                                     int age) { //name--指定参数名字，required--是否一定要有该参数 defaultValue--默认值

        System.out.println(name);
        System.out.println(age);
        return "some student";
    }
    //获取请求格式(url)中占位符的值（restful风格）
    @RequestMapping(path = "/student/{id}",method = RequestMethod.GET)
    @ResponseBody //假如这个类上的注解是restController所以这个加不加是无所谓的，只有是controller注解的时候需要添加
    public String getStudentId(@PathVariable("id")int id){
        System.out.println(id);
        return "student id";
    }
    //获取post请求（post中的请求参数不会在地址栏中显示）
    @RequestMapping("/postStudent")
    @ResponseBody
    public String postStudent(@RequestParam(name = "Name",required = false) String name, @RequestParam(name = "Age",required = false) int age){
        System.out.println(name);
        System.out.println(age);
        return "success";
    }
    //响应html数据
    @RequestMapping("/teacher")
    public ModelAndView getteacher(){
        ModelAndView mv = new ModelAndView();
        mv.addObject("name","llq");
        mv.addObject("age",28);
        mv.setViewName("/demo/view");
        return mv;
    }
    @RequestMapping("/getSchool")
    public String getSchool(Model model){
        model.addAttribute("name","新疆大学");
        model.addAttribute("age",97);
        return "/demo/view";
    }
    // 响应JSON数据（异步请求）
    // Java对象 -> JSON字符串 -> JS对象
    @RequestMapping("/getmap")
    @ResponseBody
    public Map<String,String> getMap(){
        Map<String,String> map = new HashMap<>();
        map.put("llq","是傻逼");
        map.put("hx","傻逼的儿子");
        return map;
    }
    @RequestMapping("getList")
    @ResponseBody
    public List<Map<String,String>> getList(){
        List<Map<String,String>> list = new ArrayList<>();
        Map<String,String> map = new HashMap<>();
        map.put("name" , "llq");
        map.put("age","23");
        list.add(map);

        map = new HashMap<>();
        map.put("name","hx");
        map.put("age","78");
        list.add(map);
        return list;

    }


}
