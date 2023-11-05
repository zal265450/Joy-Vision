package org.luckyjourney.service.impl;

import org.luckyjourney.constant.RedisConstant;
import org.luckyjourney.service.FeedService;
import org.luckyjourney.service.user.FollowService;
import org.luckyjourney.service.video.VideoService;
import org.luckyjourney.util.DateUtil;
import org.luckyjourney.util.RedisCacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-11-03 16:30
 */
@Service
public class FeedServiceImpl implements FeedService {



    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private RedisTemplate redisTemplate;



    @Override //todo 记得加上异步，现在跑数据
    public void pusOutBoxFeed(Long userId, Long videoId, Long time) {
        redisCacheUtil.zadd(RedisConstant.OUT_FOLLOW + userId, time, videoId, -1);
    }

    @Override
    public void pushInBoxFeed(Long userId, Long videoId, Long time) {
        // 需要推吗这个场景？只需要拉
    }

    @Override
    @Async
    public void deleteOutBoxFeed(Long userId,Collection<Long> fans,Long videoId) {
        String t = RedisConstant.IN_FOLLOW;
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Long fan : fans) {
                connection.zRem((t+fan).getBytes(),String.valueOf(videoId).getBytes());
            }
            connection.zRem((RedisConstant.OUT_FOLLOW + userId).getBytes(), String.valueOf(videoId).getBytes());
            return null;
        });
    }

    @Override
    public void deleteInBoxFeed(Long userId,Collection<Long> videoIds) {

        redisTemplate.opsForZSet().remove(RedisConstant.IN_FOLLOW + userId, videoIds.toArray(new Object[videoIds.size()]));
    }


    @Override
    @Async
    public void initFollowFeed(Long userId,Collection<Long> followIds) {
        String t2 = RedisConstant.IN_FOLLOW;
        final Date curDate = new Date();
        final Date limitDate = DateUtil.addDateDays(curDate, -7);

        final Set<ZSetOperations.TypedTuple<Long>> set = redisTemplate.opsForZSet().rangeWithScores(t2 + userId, -1, -1);
        if (!ObjectUtils.isEmpty(set)) {
            Double oldTime = set.iterator().next().getScore();
            init(userId,oldTime.longValue(),new Date().getTime(),followIds);
        } else {
            init(userId,limitDate.getTime(),curDate.getTime(),followIds);
        }

    }

    public void init(Long userId,Long min,Long max,Collection<Long> followIds) {
        String t1 = RedisConstant.OUT_FOLLOW;
        String t2 = RedisConstant.IN_FOLLOW;



        // 查看关注人的发件箱
        final List<Set<DefaultTypedTuple>> result = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (Long followId : followIds) {
                    connection.zRevRangeByScoreWithScores((t1 + followId).getBytes(), min, max, 0, 50);
                }
                return null;
            });

        final HashSet<Long> ids = new HashSet<>();
        // 放入收件箱
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Set<DefaultTypedTuple> tuples : result) {
                if (!ObjectUtils.isEmpty(tuples)) {

                    for (DefaultTypedTuple tuple : tuples) {

                        final Object value = tuple.getValue();
                        ids.add(Long.parseLong(value.toString()));
                        final byte[] key = (t2 + userId).getBytes();
                        connection.zAdd(key, tuple.getScore(), Long.valueOf(value.toString()).toString().getBytes());
                        connection.expire(key, RedisConstant.HISTORY_TIME);
                    }
                }
            }
            return null;
        });
    }

}
