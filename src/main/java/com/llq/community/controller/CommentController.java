package com.llq.community.controller;

import com.llq.community.Event.EventProducer;
import com.llq.community.entity.Comment;
import com.llq.community.entity.DiscussPost;
import com.llq.community.entity.Event;
import com.llq.community.service.CommentService;
import com.llq.community.service.DiscussPostService;
import com.llq.community.utils.CommunityConstant;
import com.llq.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 * @author llq
 * @create 2021-09-10  9:20
 */
@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {
    @Autowired
    private CommentService commentService;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private HostHolder holder;
    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment){
        comment.setUserId(holder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        //触发评论事件
        Event event = new Event().setTopic(TOPIC_COMMENT)
                .setUserId(holder.getUser().getId()).setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId()).setData("postId", discussPostId); //在添加评论的时候，前端页面是提供了评论的对象类型和id的

        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            //找到评论的帖子，再将帖子的发布者找出来
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            // 触发发帖事件(因为帖子中有个评论数量，当评论帖子的时候，帖子是会发生变化的)
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);
        }



        return "redirect:/discuss/detail/" + discussPostId;
    }
}
