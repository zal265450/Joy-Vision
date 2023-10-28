package org.luckyjourney.service.video;

import org.luckyjourney.entity.VideoShare;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xhy
 * @since 2023-10-24
 */
public interface VideoShareService extends IService<VideoShare> {

    /**
     * 添加分享记录
     * @param videoShare
     */
    void record(VideoShare videoShare);

    /**
     * 获取分享数
     * @param videoId
     * @return
     */
    long getShareCount(Long videoId);

    /**
     * 获取分享用户id
     * @param videoId
     * @return
     */
    List<Long> getShareUserId(Long videoId);
}
