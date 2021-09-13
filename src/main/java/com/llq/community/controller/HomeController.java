package com.llq.community.controller;

import com.llq.community.dao.UserMapper;
import com.llq.community.entity.DiscussPost;
import com.llq.community.entity.Page;
import com.llq.community.entity.User;
import com.llq.community.service.DiscussPostService;
import com.llq.community.service.LikeService;
import com.llq.community.service.UserService;
import com.llq.community.utils.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author llq
 * @create 2021-08-27  21:18
 */
@Controller
public class HomeController implements CommunityConstant {
    @Autowired
    private UserService userService;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private LikeService likeService;


    @RequestMapping(path = "/index",method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page){
        // 方法调用前, SpringMVC会自动实例化Model和Page, 并将Page注入Model
        // 所以, 在thymeleaf中可以直接访问Page对象中的数据
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");
        List<DiscussPost> list = discussPostService.findDiscussPosts(0,page.getOffset(),page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null){
            for(DiscussPost post : list){
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                discussPosts.add(map);

            }
        }
        model.addAttribute("discussPosts", discussPosts);
        return "/index";
    }
    @RequestMapping(path = "/error",method = RequestMethod.GET)
    public String getEroorPage(){
        return "/error/500";
    }
}
