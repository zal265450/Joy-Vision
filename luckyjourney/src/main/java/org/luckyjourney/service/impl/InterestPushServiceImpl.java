package org.luckyjourney.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.luckyjourney.constant.RedisConstant;
import org.luckyjourney.entity.Type;
import org.luckyjourney.entity.Video;
import org.luckyjourney.entity.VideoType;
import org.luckyjourney.entity.user.User;
import org.luckyjourney.service.InterestPushService;
import org.luckyjourney.service.TypeService;
import org.luckyjourney.service.VideoTypeService;
import org.luckyjourney.util.RedisCacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotBlank;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description: todo 改为异步
 * @Author: Xhy
 * @CreateTime: 2023-10-26 11:54
 */
public class InterestPushServiceImpl implements InterestPushService {

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private TypeService typeService;


    @Override
    public void pushSystemStockIn(Video video) {
        // 往系统库中添加
        final Long typeId = video.getTypeId();
        final Long videoId = video.getId();
        redisCacheUtil.lPushLeft(RedisConstant.SYSTEM_STOCK+typeId,videoId);
    }

    @Override
    public void initUserModel(Long userId, List<Long> typeIds) {

        final String key = RedisConstant.MODEL + userId;
        Map<Object, Object> modelMap = redisCacheUtil.hmget(key);
        if (!ObjectUtils.isEmpty(modelMap)){
            modelMap = new HashMap<>();
        }
        if (!ObjectUtils.isEmpty(typeIds)){
            final int size = typeIds.size();
            double probabilityValue = 100/size;
            for (Long videoType : typeIds) {
                modelMap.put(videoType,probabilityValue);
            }
        }
        redisCacheUtil.hmset(key,modelMap);

    }

    @Override
    public void updateUserModel(Long userId, Long typeId,Long videoId, Boolean flag) {
        // 可能出现游客
        // 添加概率
        if(userId!=null){
            double probabilityValue = flag ? 1 : -0.5;
            String key = RedisConstant.MODEL+userId;
            final Map<Object, Object> modelMap = redisCacheUtil.hmget(key);
            if (!modelMap.containsKey(typeId)) {
                modelMap.put(typeId,0);
            }
            modelMap.put(typeId,Double.valueOf(modelMap.get(typeId).toString())+probabilityValue);
            redisCacheUtil.hmset(key,modelMap);
            // 添加到浏览记录
            redisCacheUtil.set(RedisConstant.HISTORY_VIDEO+userId,videoId);
        }



    }

    @Override
    public List<Long> listByUserModel(User user) {
        final Long userId = user.getId();
        // 从模型中拿概率
        final Map<Object, Object> modelMap = redisCacheUtil.hmget(RedisConstant.MODEL + userId);
        // 组成数组
        Map<Long,Integer> probabilityMap = new HashMap<>();
        int size = modelMap.size();
        final AtomicInteger n = new AtomicInteger(0);
        modelMap.forEach((k,v)->{
            int probability = ((Double)v).intValue() / size;
            n.getAndAdd(probability);
            probabilityMap.put(Long.valueOf(k.toString()),probability);
        });
        final long[] probabilityArray = new long[n.get()];
        final AtomicInteger index = new AtomicInteger(0);
        // 初始化数组
        probabilityMap.forEach((type,p)->{
            int i = index.get();
            int limit = i+p;
            while (i < limit){
                probabilityArray[i++] = type;
            }
            index.set(limit);
        });


        // 创建结果集
        List<Long> videos = new ArrayList<>(10);
        // 获取视频
        final Random randomObject = new Random();
        for (int i = 0; i < 8 ; i++) {
            int count = 0;
            final long videoId = getVideoId(randomObject, probabilityArray, n.get());
            // 查重
            Object interestVideoId = redisCacheUtil.get(RedisConstant.HISTORY_VIDEO + videoId);
            if (interestVideoId!=null){
                // 尝试3次
                while (count++ < 3){
                    // 重新走上面的逻辑
                    interestVideoId = getVideoId(randomObject, probabilityArray, n.get());
                    if (interestVideoId!=null){
                        break;
                    }
                }
                // 可能没视频了，随机找一个视频 todo
            }
            // 添加
            if (interestVideoId!=null){
                videos.add(Long.valueOf(interestVideoId.toString()));
            }
        }
        // todo 找热门视频

        // 随机挑选一个视频,根据性别: 男：美女 女：宠物
        final Boolean sex = user.getSex();
        long type = typeService.getOne(new LambdaQueryWrapper<Type>().eq(Type::getName,sex ? "美女":"宠物")).getId();
        String key = RedisConstant.SYSTEM_STOCK+type;
        long videoN = redisCacheUtil.lSize(key);
        videos.add(Long.valueOf(redisCacheUtil.lGetIndex(key,randomObject.nextInt((int)videoN)).toString()));
        return videos;
    }

    public long getVideoId(Random randomObject,long[] probabilityArray,int probabilityArrayN){
        int random = randomObject.nextInt(probabilityArrayN);
        long typeId = probabilityArray[random];
        // 获取对应所有视频
        String key = RedisConstant.SYSTEM_STOCK+typeId;
        // 获取对应视频长度
        final long videoSize = redisCacheUtil.lSize(key);
        // 随机数
        int randomIndex = randomObject.nextInt((int)videoSize);
        final Long videoId = (Long) redisCacheUtil.lGetIndex(key, randomIndex);
        return videoId;
    }

}
