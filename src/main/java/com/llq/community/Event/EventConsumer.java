package com.llq.community.Event;

import com.alibaba.fastjson.JSONObject;
import com.llq.community.entity.Event;
import com.llq.community.entity.Message;
import com.llq.community.service.MessageService;
import com.llq.community.utils.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
}
