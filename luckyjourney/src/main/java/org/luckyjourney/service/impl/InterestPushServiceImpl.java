package org.luckyjourney.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.luckyjourney.constant.RedisConstant;
import org.luckyjourney.entity.video.Type;
import org.luckyjourney.entity.video.Video;
import org.luckyjourney.entity.user.User;
import org.luckyjourney.entity.vo.Model;
import org.luckyjourney.entity.vo.UserModel;
import org.luckyjourney.service.InterestPushService;
import org.luckyjourney.service.video.TypeService;
import org.luckyjourney.util.RedisCacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

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

    final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Async
    public void pushSystemStockIn(Video video) {
        // 往系统库中添加
        final Long typeId = video.getTypeId();
        final Long videoId = video.getId();
        redisCacheUtil.sSet(RedisConstant.SYSTEM_STOCK+typeId,videoId);
    }

    @Override
    public void deleteSystemStockIn(Video video) {
        final Long typeId = video.getTypeId();
        final Long videoId = video.getId();
        redisCacheUtil.setRemove(RedisConstant.SYSTEM_STOCK+typeId,videoId);
    }

    @Override
    @Async
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

    /**
     * todo
     * 1.新增历史记录接口,将用户模型和历史记录拆分开
     * 2.新增修改用户模型接口，可手动传入更新概率 -> 点赞,搜索
     * 3.如用户无概率，则执行游客逻辑
     * 4.热门视频 -> 当天视频浏览量
     * @param userModel 模型
     */

    @Override
    @Async
    public void updateUserModel(UserModel userModel) {
        // 可能出现游客
        // 添加概率
        final Long userId = userModel.getUserId();
        if(userId!=null){
            final List<Model> modelList = userModel.getModelList();
            if (!modelList.isEmpty()) {
                String modelKey = RedisConstant.MODEL;
                String historyVideoKey = RedisConstant.HISTORY_VIDEO;
                redisCacheUtil.pipeline(connection -> {
                    for (Model model : modelList) {
                        final Boolean flag = model.getFlag();
                        final Long typeId = model.getTypeId();
                        final Long videoId = model.getVideoId();
                        double probabilityValue = flag ? 1 : -0.5;
                        String key = modelKey+userId;
                        final Map<Object, Object> modelMap = redisCacheUtil.hmget(key);
                        if (!modelMap.containsKey(typeId)) {
                            modelMap.put(typeId,0);
                        }
                        modelMap.put(typeId,Double.valueOf(modelMap.get(typeId).toString())+probabilityValue);
                        final HashMap<byte[], byte[]> byteMap = new HashMap<>();
                        modelMap.forEach((k,v)->{
                            try {
                                byteMap.put(objectMapper.writeValueAsBytes(k),objectMapper.writeValueAsBytes(v));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        connection.hMSet(key.getBytes(),byteMap);
                        connection.set((historyVideoKey+videoId+":"+userId).getBytes(),videoId.toString().getBytes());
                    }
                    return null;
                });

            }
        }
    }

    @Override
    public List<Long> listVideoByUserModel(User user) {
        // 创建结果集
        List<Long> videoIds = new ArrayList<>(10);

        if (user!=null){
            final Long userId = user.getId();
            // 从模型中拿概率
            final Map<Object, Object> modelMap = redisCacheUtil.hmget(RedisConstant.MODEL + userId);

            // 组成数组
            final long[] probabilityArray = initProbabilityArray(modelMap);
            final Boolean sex = user.getSex();
            // 获取视频
            final Random randomObject = new Random();
            for (int i = 0; i < 8 ; i++) {
                int count = 0;
                final long videoId = getVideoId(randomObject, probabilityArray);
                // 查重
                Object interestVideoId = redisCacheUtil.get(RedisConstant.HISTORY_VIDEO + videoId+":"+userId);
                if (interestVideoId!=null){
                    // 尝试3次
                    while (count++ < 3){
                        // 重新走上面的逻辑
                        interestVideoId = getVideoId(randomObject, probabilityArray);
                        if (interestVideoId!=null){
                            break;
                        }
                    }
                    // 可能没视频了，随机找一个视频
                    videoIds.add(randomVideoId(sex));
                }
                // 添加
                if (interestVideoId!=null){
                    videoIds.add(Long.valueOf(interestVideoId.toString()));
                }
            }
            // todo 找热门视频

            // 随机挑选一个视频,根据性别: 男：美女 女：宠物
            videoIds.add(randomVideoId(sex));
        }else {
            // 游客
            // 获取所有分类，随机挑选10个分类中的随机视频进行推送
            final List<Long> typeIds = typeService.list(null).stream().map(Type::getId).collect(Collectors.toList());
            final ArrayList<String> keyTypes = new ArrayList<>();
            int size = typeIds.size();
            final Random random = new Random();
            // 获取随机的分类
            for (int i = 0; i < 10; i++) {
                final int typeId = random.nextInt(size)+1;
                keyTypes.add(RedisConstant.SYSTEM_STOCK+typeId);
            }
            // 获取videoId
            videoIds = redisCacheUtil.sRandom(keyTypes).stream().map(id->Long.valueOf(id.toString())).collect(Collectors.toList());
        }
        return videoIds;
    }



    public long randomVideoId(Boolean sex){
        long typeId = typeService.getOne(new LambdaQueryWrapper<Type>().eq(Type::getName,sex ? "美女":"宠物")).getId();
        String key = RedisConstant.SYSTEM_STOCK+typeId;
        return (long) redisCacheUtil.sRandom(key);
    }

    // 随机获取视频id
    public long getVideoId(Random random,long[] probabilityArray){
        long typeId = probabilityArray[random.nextInt(probabilityArray.length)+1];
        // 获取对应所有视频
        String key = RedisConstant.SYSTEM_STOCK+typeId;
        final Long videoId = (long) redisCacheUtil.sRandom(key);
        return videoId;
    }

    // 初始化概率数组
    public long[] initProbabilityArray(Map<Object,Object> modelMap){
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
        return probabilityArray;
    }
}
