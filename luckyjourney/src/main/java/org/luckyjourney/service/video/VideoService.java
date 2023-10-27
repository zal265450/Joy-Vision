package org.luckyjourney.service.video;

import org.luckyjourney.entity.Video;
import com.baomidou.mybatisplus.extension.service.IService;
import org.luckyjourney.entity.vo.VideoVO;
import org.luckyjourney.util.R;

import java.util.Collection;
import java.util.List;

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
    VideoVO getVideoById(Long id);

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


}
