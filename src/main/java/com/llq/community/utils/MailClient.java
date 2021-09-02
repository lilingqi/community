package com.llq.community.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * @author llq
 * @create 2021-08-30  10:00
 */
@Component
public class MailClient {
    //属于该类的日志类
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);
    //这是java提供的发送邮件的类，已经被spring管理了，直接注入即可
    @Resource
    private JavaMailSender mailSender;
    //发件人用户名
    @Value("${spring.mail.username}") //@Value(“${xxxx}”)注解从配置文件读取值。@Value(“${xxxx}”)注解从配置文件读取值
    private String from;

    public void sendMail(String to, String subject, String content){

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            //设置发送者
            helper.setFrom(from);
            //设置接收者
            helper.setTo(to);
            //设置主题
            helper.setSubject(subject);
            //正文，true开启超文本
            helper.setText(content,true);
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            logger.error("发送邮件失败" + e.getMessage());
        }
    }
}
