package com.llq.community.dao;

import com.llq.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

/**
 * @author llq
 * @create 2021-08-31  15:22
 */
@Mapper
@Repository
//@Deprecated 表示该方法已经过时，不建议再使用，但是仍然是可以用的
public interface LoginTicketMapper {
    @Insert({
            "insert into login_ticket(user_id, ticket, status, expired) ",
            "values(#{userId}, #{ticket}, #{status}, #{expired})"
    })
    //自动生成主键，并将生成的主键注入实体类中
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);
    @Select("select user_id, ticket, status, expired from login_ticket " +
            "where ticket = #{ticket}")
    LoginTicket selectByTicket(String ticket);
    @Update("update login_ticket set status = #{status} where ticket = #{ticket}")
    int updateStatus(@Param("ticket") String ticket, @Param("status") int status);
}
