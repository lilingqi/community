package com.llq.community.controller;

import com.llq.community.entity.Message;
import com.llq.community.entity.Page;
import com.llq.community.entity.User;
import com.llq.community.service.MessageService;
import com.llq.community.service.UserService;
import com.llq.community.utils.CommunityUtil;
import com.llq.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @author llq
 * @create 2021-09-10  17:09
 */
@Controller
public class MessageController {
    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    //消息列表
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {
        //当前登录用户的信息
        User user = hostHolder.getUser();
        //设置分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        //会话列表
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        //需要在页面上显示的信息
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));

                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);

        //查寻整个会话的未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        return "/site/letter";
    }

    @RequestMapping(path = "/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable() String conversationId, Page page, Model model) {
        //分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        //私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);
        //私信目标
        model.addAttribute("target", getLetterTarget(conversationId));

        //设置已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";
    }

    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    private List<Integer> getLetterIds (List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();

        if (letterList != null){
           for (Message message : letterList){
               if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                   ids.add(message.getId());
               }
           }
        }
        return ids;
    }

    @RequestMapping("/letter/send")
    @ResponseBody
    public String sendMessage(String toName, String content) {
        User target = userService.findUserByName(toName);
        if (toName == null) {
            return CommunityUtil.getJSONString(1, "当前用户不存在");
        }
        User fromUser = hostHolder.getUser();
        Message message = new Message();
        message.setFromId(fromUser.getId());
        message.setToId(target.getId());
        message.setContent(content);
        message.setCreateTime(new Date());
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }

        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0);
    }

}
