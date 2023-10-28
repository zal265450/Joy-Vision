package org.luckyjourney.service.video;

import org.luckyjourney.entity.VideoStar;
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
public interface VideoStarService extends IService<VideoStar> {

    /**
     * 视频点赞
     * @param videoId
     */
    boolean starVideo(Long videoId);

    /**
     * 视频点赞数
     * @param videoId
     * @return
     */
    long getStarCount(Long videoId);

    /**
     * 视频点赞用户
     * @param videoId
     * @return
     */
    List<Long> getStarUserIds(Long videoId);
}
