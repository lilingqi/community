package com.llq.community.service;

import com.llq.community.dao.LoginTicketMapper;
import com.llq.community.dao.UserMapper;
import com.llq.community.entity.LoginTicket;
import com.llq.community.entity.User;
import com.llq.community.utils.CommunityConstant;
import com.llq.community.utils.CommunityUtil;
import com.llq.community.utils.MailClient;
import com.llq.community.utils.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author llq
 * @create 2021-08-27  21:13
 */
@Service
public class UserService implements CommunityConstant {
    /**
     * autowired 是按照类型来自动注解   resource可以 byname也可以bytype
     * 默认是byname，如果是指定了bytype 没找到该类型  或者找到了多个
     * 都会报错
     */
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private LoginTicketMapper loginTicketMapper;
    //发送邮件的类
    @Resource
    private MailClient mailClient;
    //用来通过thymeleaf模板发送html类型文件
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private RedisTemplate redisTemplate;
    //获取项目的访问域名
    @Value("${community.path.domain}")
    private String domain;
    //获取项目的访问路径前缀
    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
        //return userMapper.selectById(id);
        //先从缓存中取
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }

    public User findUserByName(String name) {
        return userMapper.selectByName(name);
    }

    public User findUserByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    /**
     * 注册页面的逻辑
     *
     * @param user
     * @return
     */
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        //验证参数
        if (user == null) {
            //抛出非法参数异常
            throw new IllegalArgumentException("参数不能为空！！");
        }
        //验证注册的数据是否都合法
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }

        //验证注册数据是否被注册过
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "账号已存在");
            return map;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "邮箱已被注册");
            return map;
        }
        //注册过程（注册数据需要存入数据库）
        //salt是一个随机数，加在密码后面再进行md5加密提高安全性
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        //将密码md5加密，放入数据库
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);//type=0代表普通用户
        user.setStatus(0); //status = 0 代表还没激活
        user.setActivationCode(CommunityUtil.generateUUID());//随机生成激活码
        //这个网址是牛客网提供的一些头像
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(100)));
        user.setCreateTime(new Date());
        //user对象是没设置id的但是再通过这个操作之后，mybatis会将生成的主键注入到user中
        userMapper.insertUser(user);
        //生成激活邮件（html格式），并发送
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        //http://localhost:8080/activation/{id}/{activationCode}
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);
        return map;
    }

    //判断激活状态
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
            //判断userid的用户的激活码是不是跟随机生成的相等
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVAION_FAILURE;
        }
    }

    //登录页逻辑
    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        //空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "用户名不能为空");
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空");
        }

        //验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在");
            return map;
        }
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活");
            return map;
        }
        //验证密码
        //同一个字符串MD5加密之后的结果是一样的
        password = CommunityUtil.md5(password + user.getSalt());
        if (!password.equals(user.getPassword())) {
            map.put("passwordMsg", "密码不正确");
            return map;
        }
        //通过验证，生成验证凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));

        // loginTicketMapper.insertLoginTicket(loginTicket);
        //将登录凭证保存redis中
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        //redis会将对象转化成为json字符串
        redisTemplate.opsForValue().set(redisKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    //退出登录，修改ticket用户得状态status=1
    public void logout(String ticket) {
        // loginTicketMapper.updateStatus(ticket,1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    //获取登录凭证
    public LoginTicket findLoginTicket(String ticket) {
//        return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    //上传头像
    public int updateHeader(int userId, String headerUrl) {
        // return userMapper.updateHeader(userId,headerUrl);
        //修改了用户的信息需要清除redis的缓存
        int row = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return row;
    }

    //修改密码
    public int changePassword(int userId, String password) {
       // return userMapper.updatePassword(userId, password);
        int row = userMapper.updatePassword(userId, password);
        clearCache(userId);
        return row;

    }

    // 1.优先从缓存中取值
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    // 2.取不到时初始化缓存数据
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    // 3.数据变更时清除缓存数据
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
