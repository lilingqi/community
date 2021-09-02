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
    //方法有参数的时候，最好是加上@param注解，防止sql语句发生错误，加上注解之后数据在域中的形式相当于map{id=id，status=status}
    //如果没加的话相当于map{param1=id，param2=status}
    int updateStatus(@Param("id") int id, @Param("status") int status);

    int updateHeader(@Param("id") int id, @Param("headerUrl") String headerUrl);

    int updatePassword( @Param("id") int id, @Param("password") String password);
}
