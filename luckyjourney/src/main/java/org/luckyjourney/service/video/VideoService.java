package org.luckyjourney.service.video;

import org.luckyjourney.entity.video.Video;
import com.baomidou.mybatisplus.extension.service.IService;
import org.luckyjourney.entity.video.VideoShare;

import java.util.Collection;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xhy
 * @since 2023-10-24
 */
public interface VideoService extends IService<Video> {

    /**
     * 获取视频信息
     * @param id
     * @return
     */
    Video getVideoById(Long id);

    /**
     * 发布/修改视频
     * 修改无法更换视频源
     * @param video
     */
    void publishVideo(Video video);

    /**
     * 删除视频
     * @param id
     */
    void deleteVideo(Long id);

    /**
     * 主页推送视频
     * @return
     */
    Collection<Video> pushVideos();

    /**
     * 根据视频分类获取视频
     * @param typeId
     * @return
     */
    Collection<Video> getVideoByTypeId(Long typeId);

    /**
     * 根据title查询视频
     * @param title
     * @return
     */
    Collection<Video> searchVideo(String title);

    /**
     * 审核处理
     * @param video
     */
    void auditProcess(Video video);

    /**
     * 视频点赞
     * @param videoId
     * @return
     */
    boolean startVideo(Long videoId);

    /**
     * 分享视频
     * @param videoShare
     * @return
     */
    boolean shareVideo(VideoShare videoShare);

    /**
     * 添加历史记录
     * @param videoId
     */
    void historyVideo(Long videoId,Long userId);

    /**
     * 获取当前用户浏览记录
     * @return
     */
    Collection<Video> getHistory();

    Collection<Video> listVideoByFavorites(Long favoritesId);
}
