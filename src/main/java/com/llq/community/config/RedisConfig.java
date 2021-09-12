package com.llq.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @author llq
 * @create 2021-09-12  16:20
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        //设置key的序列化方式
        template.setKeySerializer(RedisSerializer.string());

        //设置value的序列化方式
        template.setValueSerializer(RedisSerializer.json());

        //设置hash的key序列化方式
        template.setHashKeySerializer(RedisSerializer.string());

        //设置hash的value序列化方式
        template.setValueSerializer(RedisSerializer.json());

        template.afterPropertiesSet();
        return template;
    }
}
