package com.llq.community.controller;

import com.llq.community.entity.DiscussPost;
import com.llq.community.entity.User;
import com.llq.community.service.DiscussPostService;
import com.llq.community.service.UserService;
import com.llq.community.utils.CommunityUtil;
import com.llq.community.utils.HostHolder;
import org.apache.catalina.Host;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * @author llq
 * @create 2021-09-09  9:54
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private HostHolder holder;
    @Autowired
    private UserService userService;

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDisscussPost(String title, String content) {
        User user = holder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "您还没有登录，请先登录后再操作");
        }
        DiscussPost post = new DiscussPost();
        post.setTitle(title);
        post.setContent(content);
        post.setUserId(user.getId());
        post.setCreateTime(new Date());
        discussPostService.insertDiscussPost(post);
        return CommunityUtil.getJSONString(0, "发布成功");
    }
    @RequestMapping(value = "/detail/{postId}", method = RequestMethod.GET)
    public String getDetail(@PathVariable("postId") int postId, Model model){
        //帖子的数据
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        model.addAttribute("post", post);
        //发布该帖子的用户数据
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        return "/site/discuss-detail";

    }

}
