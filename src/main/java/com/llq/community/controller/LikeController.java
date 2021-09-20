package com.llq.community.controller;

import com.llq.community.Event.EventProducer;
import com.llq.community.entity.Event;
import com.llq.community.entity.User;
import com.llq.community.service.LikeService;
import com.llq.community.utils.CommunityConstant;
import com.llq.community.utils.CommunityUtil;
import com.llq.community.utils.HostHolder;
import com.llq.community.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author llq
 * @create 2021-09-13  9:14
 */
@Controller
public class LikeController implements CommunityConstant {
    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        User user = hostHolder.getUser();
        //点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);

        //数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        //状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        //返回得结果
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        //点赞触发事件
        //需要判断一下点赞的状态，点赞才通知，取消赞不通知
        if (likeStatus == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId); //需要知道点赞的是哪篇帖子，通知的时候将帖子链接也要发送过来
            eventProducer.fireEvent(event);
        }
        if (entityType == ENTITY_TYPE_POST) {
            //获取的key存储了需要重新计算分数的帖子
            String redisKey = RedisKeyUtil.getPostScoreKey();
            //这里不要用list来存储，因为一个帖子会不停的有人赞和评论，会有重复，用set的话就不会出现这种情况
            redisTemplate.opsForSet().add(redisKey,postId);
        }


        return CommunityUtil.getJSONString(0,null,map);

    }
}
