package com.llq.community;

import com.llq.community.dao.DiscussPostMapper;
import com.llq.community.dao.UserMapper;
import com.llq.community.entity.DiscussPost;
import com.llq.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author llq
 * @create 2021-08-27  11:41
 */
@RunWith(SpringRunner.class) //如果爆红可能是因为么有引入junit
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)

public class MapperTest {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Test
    public void testSelectUser(){
        System.out.println(userMapper.selectById(162));
        System.out.println(userMapper.selectByName("liubei"));
        System.out.println(userMapper.selectByEmail("nowcoder101@sina.com"));
    }
    @Test
    public void testInsert(){  //这个方法插入数据不成功，可能是回滚了
        User user = new User();
        user.setUsername("llq");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        System.out.println(userMapper.insertUser(user));
        System.out.println(user.getId());
    }
    @Test
    public void testUpdate(){
        System.out.println(userMapper.updatePassword(101, "hello1"));
        System.out.println(userMapper.updateHeader(101, "localhost:8080"));
        System.out.println(userMapper.updateStatus(101, 0));
    }

    @Test
    public void testSelectDiscussPost(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0, 0, 10);
        for(DiscussPost discussPost: list){
            System.out.println(discussPost);
        }
        System.out.println(discussPostMapper.selectDiscussPostRows(0));
    }

}
