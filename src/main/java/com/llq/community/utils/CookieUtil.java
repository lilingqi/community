package com.llq.community.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * @author llq
 * @create 2021-09-01  19:53
 * 注意：cookie只是一个普通的类，存放的并不是map键值对，只是形式相似
 */
public class CookieUtil {
    public static String getValue(HttpServletRequest request, String key) {
        if (request == null || key == null) {
            throw new IllegalArgumentException("参数为空");
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies){
                if (cookie.getName().equals(key)){ //这里一开始用了== 我是傻逼！
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}
