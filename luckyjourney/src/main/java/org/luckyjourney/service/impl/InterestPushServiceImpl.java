package org.luckyjourney.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.luckyjourney.constant.RedisConstant;
import org.luckyjourney.entity.video.Type;
import org.luckyjourney.entity.video.Video;
import org.luckyjourney.entity.user.User;
import org.luckyjourney.entity.vo.HotVideo;
import org.luckyjourney.entity.vo.Model;
import org.luckyjourney.entity.vo.UserModel;
import org.luckyjourney.service.InterestPushService;
import org.luckyjourney.service.video.TypeService;
import org.luckyjourney.util.RedisCacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-10-26 11:54
 */
// 暂时为异步
@Service
public class InterestPushServiceImpl implements InterestPushService {

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private TypeService typeService;

    @Autowired
    private RedisTemplate redisTemplate;


    final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Async
    public void pushSystemStockIn(Video video) {
        // 往系统库中添加
        final List<String> labels = video.getLabels();
        final Long videoId = video.getId();
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (String label : labels) {
                connection.sAdd((RedisConstant.SYSTEM_STOCK + label).getBytes(),String.valueOf(videoId).getBytes());
            }
            return null;
        });
    }

    @Override
    public void pushSystemTypeStockIn(Video video) {
        final Long typeId = video.getTypeId();
        redisCacheUtil.set(RedisConstant.SYSTEM_TYPE_STOCK + typeId,video.getId());
    }

    @Override
    public Collection<Long> listVideoIdByTypeId(Long typeId) {
        // 随机推送10个
        final byte[] key = (RedisConstant.SYSTEM_TYPE_STOCK + typeId).getBytes();
        final List<Long> list = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (int i = 0; i < 10; i++) {
                connection.sRandMember(key);
            }
            return null;
        });
        return new HashSet<>(list);
    }

    @Override
    public void deleteSystemStockIn(Video video) {
        final List<String> labels = video.getLabels();
        final Long videoId = video.getId();
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (String label : labels) {
                connection.sRem((RedisConstant.SYSTEM_STOCK + label).getBytes(),String.valueOf(videoId).getBytes());
            }
            return null;
        });
    }

    @Override
    @Async
    public void initUserModel(Long userId, List<Long> labels) {

        final String key = RedisConstant.USER_MODEL + userId;
        Map<Object, Object> modelMap = redisCacheUtil.hmget(key);
        if (!ObjectUtils.isEmpty(modelMap)) {
            modelMap = new HashMap<>();
        }
        if (!ObjectUtils.isEmpty(labels)) {
            final int size = labels.size();
            // 将标签分为等分概率,不可能超过100个分类
            double probabilityValue = 100 / size;
            for (Long videoType : labels) {
                modelMap.put(videoType, probabilityValue);
            }
        }
        redisCacheUtil.hmset(key, modelMap);

    }

    /**
     * todo 对单独的视频添加相似推送
     * @param userModel
     */

    @Override
    @Async
    public void updateUserModel(UserModel userModel) {

        final Long userId = userModel.getUserId();
        // 游客不用管
        if (userId != null) {
            final List<Model> models = userModel.getModels();
            // 获取用户模型
            String key = RedisConstant.USER_MODEL + userId;
            Map<Object, Object> modelMap = redisCacheUtil.hmget(key);

            if (modelMap == null) {
                modelMap = new HashMap<>();
            }
            // todo 这里需要将概率等比例缩小，否则可能会有一个标签概率增长巨大
            for (Model model : models) {
                // 修改用户模型
                if (modelMap.containsKey(model.getLabel())) {
                    modelMap.put(model.getLabel(), modelMap.get(model.getLabel().doubleValue() + model.getScore()));
                } else {
                    modelMap.put(model.getLabel(), model.getScore());
                }
            }
            // 更新用户模型
            redisCacheUtil.hmset(key, modelMap);
        }

    }

    @Override
    public Collection<Long> listVideoIdByUserModel(User user) {
        // 创建结果集
        List<Long> videoIds = new ArrayList<>(10);

        if (user != null) {
            final Long userId = user.getId();
            // 从模型中拿概率
            final Map<Object, Object> modelMap = redisCacheUtil.hmget(RedisConstant.USER_MODEL + userId);
            if (modelMap != null) {
                // 组成数组
                final long[] probabilityArray = initProbabilityArray(modelMap);
                final Boolean sex = user.getSex();
                // 获取视频
                final Random randomObject = new Random();
                for (int i = 0; i < 8; i++) {
                    int count = 0;
                    final long videoId = getVideoId(randomObject, probabilityArray);
                    // 查重
                    Object interestVideoId = redisCacheUtil.get(RedisConstant.HISTORY_VIDEO + videoId + ":" + userId);
                    if (interestVideoId != null) {
                        // 尝试3次
                        while (count++ < 2) {
                            // 重新走上面的逻辑
                            interestVideoId = getVideoId(randomObject, probabilityArray);
                            if (interestVideoId != null) {
                                break;
                            }
                        }
                        // 可能没视频了，随机找一个视频
                        videoIds.add(randomVideoId(sex));
                    }
                    // 添加
                    if (interestVideoId != null) {
                        videoIds.add(Long.valueOf(interestVideoId.toString()));
                    }
                }
                videoIds.add(randomHotVideoId());
                // 随机挑选一个视频,根据性别: 男：美女 女：宠物
                videoIds.add(randomVideoId(sex));
                return videoIds;
            }
        }
        // 游客
        // 随机获取10个标签
        final List<String> labels = typeService.random10Labels();
        final ArrayList<String> labelNames = new ArrayList<>();
        int size = labels.size();
        final Random random = new Random();
        // 获取随机的分类
        for (int i = 0; i < 10; i++) {
            final int randomIndex = random.nextInt(size) + 1;
            labelNames.add(RedisConstant.SYSTEM_STOCK + labels.get(randomIndex));
        }
        // 获取videoId
        final List<Object> list = redisCacheUtil.sRandom(labelNames);
        if (!ObjectUtils.isEmpty(list)){
            videoIds = list.stream().filter(id ->!ObjectUtils.isEmpty(id)).map(id -> Long.valueOf(id.toString())).collect(Collectors.toList());
        }

        return videoIds;
    }

    @Override
    public Collection<Long> listVideoIdByLabels(List<String> labelNames) {
        final ArrayList<String> labelKeys = new ArrayList<>();
        for (String labelName : labelNames) {
            labelKeys.add(RedisConstant.SYSTEM_STOCK + labelName);
        }
        List<Long> videoIds = new ArrayList<>();
        final List<Object> list = redisCacheUtil.sRandom(labelKeys);
        if (!ObjectUtils.isEmpty(list)){
            videoIds = list.stream().filter(id ->!ObjectUtils.isEmpty(id)).map(id -> Long.valueOf(id.toString())).collect(Collectors.toList());
        }
        return videoIds;
    }


    public long randomHotVideoId(){
        final Object o = redisTemplate.opsForZSet().randomMember(RedisConstant.HOT_RANK);
        return ((HotVideo)o).getVideoId();
    }

    public long randomVideoId(Boolean sex) {
        String key = RedisConstant.SYSTEM_STOCK + (sex ? "美女" : "宠物");
        return (long) redisCacheUtil.sRandom(key);
    }

    // 随机获取视频id
    public long getVideoId(Random random, long[] probabilityArray) {
        long typeId = probabilityArray[random.nextInt(probabilityArray.length) + 1];
        // 获取对应所有视频
        String key = RedisConstant.SYSTEM_STOCK + typeId;
        final Long videoId = (long) redisCacheUtil.sRandom(key);
        return videoId;
    }

    // 初始化概率数组
    public long[] initProbabilityArray(Map<Object, Object> modelMap) {
        // 组成数组
        Map<Long, Integer> probabilityMap = new HashMap<>();
        int size = modelMap.size();
        final AtomicInteger n = new AtomicInteger(0);
        modelMap.forEach((k, v) -> {
            int probability = ((Double) v).intValue() / size;
            n.getAndAdd(probability);
            probabilityMap.put(Long.valueOf(k.toString()), probability);
        });
        final long[] probabilityArray = new long[n.get()];
        final AtomicInteger index = new AtomicInteger(0);
        // 初始化数组
        probabilityMap.forEach((type, p) -> {
            int i = index.get();
            int limit = i + p;
            while (i < limit) {
                probabilityArray[i++] = type;
            }
            index.set(limit);
        });
        return probabilityArray;
    }
}
