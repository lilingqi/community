package com.llq.community.utils;

import com.llq.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 用于替代session对象，使用ThreadLocal
 * 隔离的持有用户数据
 * @author llq
 * @create 2021-09-01  20:20
 * ThreadLocal 的作用，它可以解决多线程的数据安全问题。
 * ThreadLocal 它可以给当前线程关联一个数据,以当前线程为key，一次只能存取一个对象。
 * （可以是普通变量，可以是对象，也可以是数组，集合）
 */
@Component
public class HostHolder {
    private static ThreadLocal<User> users = new ThreadLocal<>();
    //存用户，以当前线程为key
    public void setUser(User user){
        users.set(user);
    }
    //取出用户
    public User getUser(){
        return users.get();
    }

    //防止内存泄漏，最好每次操作完把数据清理掉
    public void clear() {
        users.remove();
    }

}
