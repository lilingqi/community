package com.llq.community.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

/**
 * @author llq
 * @create 2021-08-30  15:43
 * 生成某些配置
 */
public class CommunityUtil {
    //生成随机字符
    public static String generateUUID(){
        //将生成的-给去掉
        return UUID.randomUUID().toString().replaceAll("-","");
    }
    //md5加密
    public static String md5(String key){
        //检查是否为空，或者为空格字符等
        if(StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

}
