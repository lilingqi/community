package com.llq.community.dao;

import com.llq.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author llq
 * @create 2021-09-09  17:25
 */
@Mapper
@Repository
public interface CommentMapper {
    //通过帖子的类型回复类型和回复id来查询相关的评论
    List<Comment> selectCommentsByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId, @Param("offset") int offset, @Param("limit") int limit);

    //来查询评论的数量
    int selectCountByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId);

    //插入评论
    int insertComment(Comment comment);

    //根据id找评论
    Comment selectCommentById ( int id);

}
