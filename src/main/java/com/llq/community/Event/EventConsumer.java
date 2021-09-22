package com.llq.community.Event;

import com.alibaba.fastjson.JSONObject;
import com.llq.community.entity.DiscussPost;
import com.llq.community.entity.Event;
import com.llq.community.entity.Message;
import com.llq.community.service.DiscussPostService;
import com.llq.community.service.ElasticsearchService;
import com.llq.community.service.MessageService;
import com.llq.community.utils.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * @author llq
 * @create 2021-09-15  10:22
 */
@Component
public class EventConsumer implements CommunityConstant {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    //将监听到的通知都存入message表中
    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Value("${wk.image.command}")
    private String wkImageCommand;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_LIKE,TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if (event == null) {
            logger.error("消息格式错误");
            return;
        }

        //发送站内的通知
        Message message = new Message();
        message.setFromId(1);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }
    // 消费发帖事件
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }

        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(post);
    }
    // 消费删帖事件
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }
        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }

    // 消费分享事件
    @KafkaListener(topics = TOPIC_SHARE)
    public void handleShareMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }

        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");

        String cmd = wkImageCommand + " --quality 75 "
                + htmlUrl + " " + wkImageStorage + "/" + fileName + suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("生成长图成功: " + cmd);
        } catch (IOException e) {
            logger.error("生成长图失败: " + e.getMessage());
        }

//        // 启用定时器,监视该图片,一旦生成了,则上传至七牛云.
//        UploadTask task = new UploadTask(fileName, suffix);
//        Future future = taskScheduler.scheduleAtFixedRate(task, 500);
//        task.setFuture(future);
    }
}
