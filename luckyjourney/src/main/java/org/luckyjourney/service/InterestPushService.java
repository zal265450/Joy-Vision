package org.luckyjourney.service;

import org.luckyjourney.entity.video.Video;
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
     * @param labels 分类id
     */
    void initUserModel(Long userId, List<Long> labels);

    /**
     * 用户模型修改概率 : 可分批次发送
     * 修改场景:
     * 1.观看浏览量到达总时长1/5  +1概率
     * 2.观看浏览量未到总时长1/5 -0.5概率
     * 3.点赞视频  +2概率
     * 4.收藏视频  +3概率
     */
    void updateUserModel(UserModel userModel);

    /**
     * 用于给用户推送视频
     * @param user 传id和sex
     * @return videoIds
     */
    List<Long> listVideoByUserModel(User user);
}
