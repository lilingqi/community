package com.llq.community.controller.interceptor;

import com.llq.community.entity.LoginTicket;
import com.llq.community.entity.User;
import com.llq.community.service.UserService;
import com.llq.community.utils.CookieUtil;
import com.llq.community.utils.HostHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * 拦截请求，将登录信息保存和显示，思考为什么要用拦截器（对需要访问得方法进行加强---aop思想）
 * 拦截器的作用：在访问的请求，访问控制层的方法之前，对该方法进行一定程度的增强----利用了aop思想
 *
 * @author llq
 * @create 2021-09-01  17:24
 */
@Component
//handlerInterceptor接口中得方法都是default方法，可以实现也可以不实现
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;
    private static final Logger logger = LoggerFactory.getLogger(LoginTicketInterceptor.class);

    //在controller之前运行----检查是否登录
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ticket = CookieUtil.getValue(request, "ticket");
        if (ticket != null) {
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                //查询已登录的用户信息
                User user = userService.findUserById(loginTicket.getUserId());
                //持有用户信息
                hostHolder.setUser(user); //我觉得这里用session应该也行
                // 构建用户认证的结果,并存入SecurityContext,以便于Security进行授权.
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user, user.getPassword(), userService.getAuthorities(user.getId()));
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }

        return true; //必须是true之后得代码才能运行
    }

    //在controller之后运行
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }

    //在templates之后运行
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
       hostHolder.clear();
       SecurityContextHolder.clearContext();
    }
}
