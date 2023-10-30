package org.luckyjourney.schedul;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.luckyjourney.constant.RedisConstant;
import org.luckyjourney.entity.video.Video;
import org.luckyjourney.entity.vo.HotVideo;
import org.luckyjourney.service.video.VideoService;
import org.luckyjourney.util.RedisCacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @description: 热度排行榜
 * @Author: Xhy
 * @CreateTime: 2023-10-31 00:50
 */
@Component
public class HotRank {

    @Autowired
    private VideoService videoService;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private RedisTemplate redisTemplate;

    final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(cron = "0 0 */1 * * ?")
    public void hot(){
        // 存放videoId,title
        final PriorityQueue<HotVideo> hotRank = new PriorityQueue<>(50,(o1, o2) -> -(int)(o1.getHot() - o2.getHot()));
        long index = 0;
        // 每次拿1000个
        long id = 0;
        List<Video> videos = videoService.list(new LambdaQueryWrapper<Video>().ge(Video::getId, id).last("limit " + index));;
        while (!ObjectUtils.isEmpty(videos)){
            for (Video video : videos) {
                Long shareCount = video.getShareCount();
                Double historyCount = video.getHistoryCount()*0.8;
                Long startCount = video.getStartCount();
                // todo 收藏没写
                final Date date = new Date();
                long t = date.getTime() - video.getGmtCreated().getTime();
                final double hot = hot(shareCount + historyCount + startCount, TimeUnit.MILLISECONDS.toDays(t));
                final HotVideo hotVideo = new HotVideo(hot, video.getId(), video.getTitle());
                hotRank.add(hotVideo);
            }
            id = videos.get(videos.size()-1).getId();
            index = index + 1000;
            videos = videoService.list(new LambdaQueryWrapper<Video>().ge(Video::getId, id).last("limit " + index));;
        }
        final byte[] key = RedisConstant.HOT_RANK.getBytes();
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        jackson2JsonRedisSerializer.setObjectMapper(om);

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (HotVideo hotVideo : hotRank) {
                final Double hot = hotVideo.getHot();
                hotVideo.setHot(null);
                try {
                    // 不这样写铁报错！序列化问题
                    connection.zAdd(key,hot,jackson2JsonRedisSerializer.serialize(om.writeValueAsString(hotVideo)));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
            return null;
        });
        // 清理50后面的
        redisTemplate.opsForZSet().removeRange(RedisConstant.HOT_RANK,50,-1);
    }

    static double a = 0.011;

    public static double hot(double weight,double t){
        return weight * Math.exp(-a * t);
    }

}
