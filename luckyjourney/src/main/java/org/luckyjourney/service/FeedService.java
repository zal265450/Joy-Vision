package org.luckyjourney.service;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-11-03 16:27
 */
public interface FeedService {

    /**
     * 推入发件箱
     * @param userId 发件箱用户id
     * @param videoId 视频id
     */
    void pusOutBoxFeed(Long userId,Long videoId,Long time);

    /**
     * 推入收件箱
     * @param userId
     * @param videoId
     */
    void pushInBoxFeed(Long userId,Long videoId,Long time);

    /**
     * 删除发件箱
     * @param userId
     * @param videoId
     */
    void deleteOutBoxFeed(Long userId,Long videoId);

    /**
     * 删除收件箱
     * @param userId
     * @param videoId
     */
    void deleteInBoxFeed(Long userId,Long videoId);

    /**
     * 初始化关注流-拉模式 with TTL
     * @param userId
     */
    void initFollowFeed(Long userId);
}
