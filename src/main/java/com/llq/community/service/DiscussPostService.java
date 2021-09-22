package com.llq.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.llq.community.dao.DiscussPostMapper;
import com.llq.community.dao.UserMapper;
import com.llq.community.entity.DiscussPost;
import com.llq.community.utils.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * @author llq
 * @create 2021-08-27  21:09
 */
@Service
public class DiscussPostService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    // Caffeine核心接口: Cache, LoadingCache, AsyncLoadingCache
    //帖子列表缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;

    //帖子总数缓存
    private LoadingCache<Integer, Integer> postRowsCache;


    @PostConstruct
    public void init() {
        // 初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Nullable
                    @Override
                    public List<DiscussPost> load(@NonNull String key) throws Exception {
                        if (key == null || key.length() == 0) {
                            throw new IllegalArgumentException("参数错误!");
                        }

                        String[] params = key.split(":");
                        if (params == null || params.length != 2) {
                            throw new IllegalArgumentException("参数错误!");
                        }

                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        // 二级缓存: Redis -> mysql

                        logger.debug("load post list from DB.");
                        return discussPostMapper.selectDiscussPosts(0, offset, limit, 1);
                    }
                });
        // 初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Nullable
                    @Override
                    public Integer load(@NonNull Integer key) throws Exception {
                        logger.debug("load post rows from DB.");
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }


    //我们在首页显示帖子的时候，其实是不需要userId的，只有在查询用户自己发的帖子才需要userId
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit,int orderMode) {
        //将热门帖子存放在caffine中，查询的时候从本地缓存中取
        if (userId == 0 && orderMode == 1) {
            return postListCache.get(offset + ":" + limit);
        }
        logger.debug("load post list from DB ###");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit,orderMode);
    }

    public int findDiscussPostRows(int userId) {
        //查行数不需要有什么限制
        if (userId == 0) {
            return postRowsCache.get(userId);
        }
        return discussPostMapper.selectDiscussPostRows(userId);

    }

    public int insertDiscussPost(DiscussPost discussPost) {
        if (discussPost == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        //转义html标记，将损害网页的一些字符过滤
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));

        //敏感词屏蔽
        String title = sensitiveFilter.filter(discussPost.getTitle());
        discussPost.setTitle(title);
        String content = sensitiveFilter.filter(discussPost.getContent());
        discussPost.setContent(content);
        return discussPostMapper.insertDiscussPost(discussPost);
    }

    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    public int updateCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id, commentCount);
    }
    //修改帖子的类型
    public int updateDiscussPostType(int id, int type) {
        return discussPostMapper.updateDiscussPostType(id, type);
    }
    //修改帖子的状态
    public int updateDiscussPostStatus(int id, int status) {
        return discussPostMapper.updateDiscussPostStatus(id, status);
    }
    //更新帖子的分数
    public int updateScore(int id, double score) {
        return discussPostMapper.updateScore(id, score);
    }
}
