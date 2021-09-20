package com.llq.community.controller;

import com.llq.community.Event.EventProducer;
import com.llq.community.entity.*;
import com.llq.community.service.CommentService;
import com.llq.community.service.DiscussPostService;
import com.llq.community.service.LikeService;
import com.llq.community.service.UserService;
import com.llq.community.utils.CommunityConstant;
import com.llq.community.utils.CommunityUtil;
import com.llq.community.utils.HostHolder;
import com.llq.community.utils.RedisKeyUtil;
import org.apache.catalina.Host;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @author llq
 * @create 2021-09-09  9:54
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private HostHolder holder;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private RedisTemplate redisTemplate;

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
        //触发发帖事件，发布帖子的时候，要及时将发布的帖子存到es里面去
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0, "发布成功");
    }

    @RequestMapping(value = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDetail(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        //帖子的数据
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        //发布该帖子的用户数据
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);
        //点赞状态
        int likeStatus = holder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(holder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);

        //评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());
        //评论：给帖子的评论
        //回复：给评论的评论
        //评论列表
        List<Comment> commentList = commentService.findCommentByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        //评论VO(view object)列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                //评论需要展示的包括头像 用户id等等，所以要用map来构建
                Map<String, Object> commentVo = new HashMap<>();
                //评论
                commentVo.put("comment", comment);
                //评论的作者
                commentVo.put("user", userService.findUserById(comment.getUserId()));
                // 点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                // 点赞状态
                likeStatus = holder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(holder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);


                //回复列表
                List<Comment> replyList = commentService.findCommentByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                //回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        //回复
                        replyVo.put("reply", reply);
                        //作者
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        //回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);
                        // 点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);
                        // 点赞状态
                        likeStatus = holder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(holder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);
                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);
                //回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments", commentVoList);
        return "/site/discuss-detail";
    }

    //置顶
    @RequestMapping(path = "top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id) {
        discussPostService.updateDiscussPostType(id,1);
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(holder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0, "置顶成功");

    }
    //加精
    @RequestMapping(path = "wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id) {
        discussPostService.updateDiscussPostStatus(id, 1);
        //加精要触发事件，将帖子存到es中
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(holder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        //加精之后需要重新计算分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,id);
        return CommunityUtil.getJSONString(0, "加精成功");
    }
    //删帖
    @RequestMapping(path = "delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id) {
        discussPostService.updateDiscussPostStatus(id, 2);
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(holder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0, "删帖成功");
    }
}
