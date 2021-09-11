package com.llq.community.dao;

import com.llq.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author llq
 * @create 2021-09-10  15:52
 */
@Mapper
@Repository
public interface MessageMapper {
    //查询当前用户的会话列表，针对每个会话只返回一个最新的私信
    List<Message> selectConversations(@Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);
    //查询当前用户的会话数量
    int selectConversationCount(int userId);

    //查询某个会话所包括的私信列表
    List<Message> selectLetters(@Param("conversationId") String conversationId, @Param("offset") int offset, @Param("limit") int limit);

    //查询某个会话包括的私信数量
    int selectLetterCount(String conversationId);

    //查询未读私信的数量
    int selectLetterUnreadCount(@Param("userId") int userId, @Param("conversationId") String conversationId);

    //新增信息
    int insertMessage(Message message);

    //更新信息的状态 0：未读 1：已读 2：删除
    //这里是需要批量来改信息的状态的，因为一个界面可能有多个信息是未读的！
    int updateStatus(@Param("ids") List<Integer> ids, @Param("status") int status);


}
