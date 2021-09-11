package com.llq.community.entity;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * @author llq
 * @create 2021-09-10  15:54
 */
@Data
@ToString
public class Message {
    //每条信息的id
    private int id;
    //发送者的id
    private int fromId;
    //接收者的id
    private int toId;
    //会话的id
    private String conversationId;
    //信息内容
    private  String content;
    //信息状态0：未读， 1：已读 2：已删除
    private int status;
    //信息的发送时间
    private Date createTime;
}
