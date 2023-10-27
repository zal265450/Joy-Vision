package org.luckyjourney.service;

import org.luckyjourney.entity.Video;
import org.luckyjourney.entity.user.User;

import java.util.List;

/**
 * @description: 兴趣推送
 * @Author: Xhy
 * @CreateTime: 2023-10-26 11:54
 */
public interface InterestPushService {


    /**
     * 用户发布视频后调用
     * @param video
     */
    void pushSystemStockIn(Video video);

    /**
     * 用户初始化模型
     * @param userId 用户id
     * @param typeIds 分类id
     */
    void initUserModel(Long userId, List<Long> typeIds);


    /**
     * 用户刷视频更新概率
     * @param userId 用户id
     * @param typeId 视频分类id
     * @param flag 增加/减少
     */
    void updateUserModel(Long userId,Long typeId,Long videoId,Boolean flag);

    /**
     * 用于给用户推送视频
     * @param user
     * @return videoIds
     */
    List<Long> listByUserModel(User user);
}
