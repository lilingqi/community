package com.llq.community.dao;

import com.llq.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author llq
 * @create 2021-08-27  17:11
 */
@Mapper
@Repository
public interface DiscussPostMapper {
    //对于集合类型，在xml配置文件中只用返回相应的泛型类型即可
    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId,
                                         @Param("offset") int offset,
                                         @Param("limit") int limit);
    int selectDiscussPostRows(@Param("userId") int userId);

}
