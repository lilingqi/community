package com.llq.community.Event;

import com.alibaba.fastjson.JSONObject;
import com.llq.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author llq
 * @create 2021-09-15  10:18
 */
@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    //处理事件
    public void fireEvent(Event event) {
        //将事件发布到相关的主题
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
