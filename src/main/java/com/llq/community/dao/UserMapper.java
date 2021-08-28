package com.llq.community.dao;

import com.llq.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author llq
 * @create 2021-08-27  11:34
 */
@Mapper //如果只有这个注解在注入的时候会爆红，但是不影响使用
@Repository //如果只有这个注解，spring要配置mapperscan才能扫描到这个类，这样才能正常使用
public interface UserMapper {

    User selectById(int id);

    User selectByName(String username);

    User selectByEmail(String email);

    int insertUser(User user);

    int updateStatus(@Param("id") int id, @Param("status") int status);

    int updateHeader(@Param("id") int id, @Param("headerUrl") String headerUrl);

    int updatePassword( @Param("id") int id, @Param("password") String password);  //有多个参数时，一定要用到这个注解
}
