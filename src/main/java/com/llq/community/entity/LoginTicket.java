package com.llq.community.entity;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * @author llq
 * @create 2021-08-31  15:18
 */
@Data
@ToString   //用lombok来导入的
public class LoginTicket {
    private int id;
    private int userId;
    private String ticket;
    private int status;
    private Date expired;

}
