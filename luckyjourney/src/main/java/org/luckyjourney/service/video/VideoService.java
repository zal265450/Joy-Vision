package org.luckyjourney.service.video;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.luckyjourney.entity.video.Video;
import com.baomidou.mybatisplus.extension.service.IService;
import org.luckyjourney.entity.video.VideoShare;
import org.luckyjourney.entity.vo.BasePage;
import org.luckyjourney.entity.vo.HotVideo;
import org.luckyjourney.schedul.HotRank;

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
    Video getVideoById(Long id)  ;

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
     * 根据视频分类获取视频,乱序
     * @param typeId
     * @return
     */
    Collection<Video> getVideoByTypeId(Long typeId);

    /**
     * 根据title查询视频
     * @param title
     * @return
     */
    IPage<Video> searchVideo(String title,BasePage basePage);

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
    void historyVideo(Long videoId,Long userId) throws Exception;

    /**
     * 收藏视频
     * @param fId
     * @param vId
     * @return
     */
    boolean favoritesVideo(Long fId, Long vId);

    /**
     * 获取当前用户浏览记录,带分页
     * @return
     */
    Collection<Video> getHistory(BasePage basePage);

    /**
     * 根据收藏夹获取视频
     * @param favoritesId
     * @return
     */
    Collection<Video> listVideoByFavorites(Long favoritesId);

    /**
     * 获取热度排行榜
     * @return
     */
    Collection<HotVideo> hotRank();

    /**
     * 根据标签推送相似视频
     * @param labels
     * @return
     */
    Collection<Video> listSimilarVideo(List<String> labels);

    /**
     * 根据userId获取对应视频
     * @param userId
     * @return
     */
    IPage<Video> listByUserId(Long userId, BasePage basePage);

    /**
     * 获取当前审核队列
     * @return
     */
    String getAuditQueueState();

    /**
     * 获取N天前的视频
     * @param id id
     * @param days 天数
     * @param limit 限制
     * @return
     */
    List<Video> selectNDaysAgeVideo(long id,int days,int limit);

    /**
     * 获取热门视频
     * @return
     */
    Collection<Video> listHotVideo();
}
