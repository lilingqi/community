package com.llq.community.utils;

/**
 * @author llq
 * @create 2021-08-30  18:40
 */
public interface CommunityConstant {
    //激活成功
    int ACTIVATION_SUCCESS = 0;

    //重复激活
    int ACTIVATION_REPEAT = 1;

    //激活失败
    int ACTIVAION_FAILURE = 2;

    //默认时间
    int DEFAULT_EXPIRED = 3600 * 12;

    //记住我---时间
    int REMEMBER_EXPIRED=3600 * 24 * 100;
}