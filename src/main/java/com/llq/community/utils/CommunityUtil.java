package com.llq.community.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.io.ObjectStreamClass;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author llq
 * @create 2021-08-30  15:43
 * 生成某些配置
 */
public class CommunityUtil {
    //生成随机字符
    public static String generateUUID() {
        //将生成的-给去掉
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    //md5加密
    public static String md5(String key) {
        //检查是否为空，或者为空格字符等
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    //获取json数据
    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String s : map.keySet()) {
                json.put(s, map.get(s));
            }
        }
        return json.toString();
    }

    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }

//    public static void main(String[] args) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("name","Llq");
//        map.put("age", 23);
//        String jsonString = getJSONString(0, "ok", map);
//        System.out.println(jsonString);
//
//    }

}
