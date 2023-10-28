package org.luckyjourney.service;

import org.luckyjourney.entity.Video;
import org.luckyjourney.entity.user.User;
import org.luckyjourney.entity.vo.UserModel;

import java.util.List;

/**
 * @description: 兴趣推送
 * @Author: Xhy
 * @CreateTime: 2023-10-26 11:54
 */
public interface InterestPushService {

    /**
     * 用户发布视频后调用
     * 传videoId,typeId
     * @param video
     */
    void pushSystemStockIn(Video video);

    /**
     * 删除视频
     * @param video
     */
    void deleteSystemStockIn(Video video);

    /**
     * 用户初始化模型
     * @param userId 用户id
     * @param typeIds 分类id
     */
    void initUserModel(Long userId, List<Long> typeIds);

    /**
     * 用户刷视频更新概率
     * @param userModel 模型
     */
    void updateUserModel(UserModel userModel);

    /**
     * 用于给用户推送视频
     * @param user 传id和sex
     * @return videoIds
     */
    List<Long> listVideoByUserModel(User user);
}
