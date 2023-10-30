package org.luckyjourney.service.video.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.luckyjourney.entity.Video;
import org.luckyjourney.entity.VideoStar;
import org.luckyjourney.holder.UserHolder;
import org.luckyjourney.mapper.video.VideoStarMapper;
import org.luckyjourney.service.InterestPushService;
import org.luckyjourney.service.video.VideoService;
import org.luckyjourney.service.video.VideoStarService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xhy
 * @since 2023-10-24
 */
@Service
public class VideoStarServiceImpl extends ServiceImpl<VideoStarMapper, VideoStar> implements VideoStarService {


    @Autowired
    @Lazy
    private VideoService videoService;

    @Autowired
    private InterestPushService interestPushService;

    @Override
    public boolean starVideo(Long videoId) {
        final VideoStar videoStar = new VideoStar();
        videoStar.setVideoId(videoId);
        final Long userId = UserHolder.get();
        videoStar.setUserId(userId);
        try {
            // 对应视频是否存在
            final Video video = videoService.getById(videoId);
            if (video == null) throw new IllegalArgumentException("对应视频不存在");
            // 添加概率
            this.save(videoStar);
        }catch (Exception e){
            // 存在则取消点赞
            this.remove(new LambdaQueryWrapper<VideoStar>().eq(VideoStar::getVideoId,videoId).eq(VideoStar::getUserId,userId));
            return false;
        }
        return true;
    }

    @Override
    public long getStarCount(Long videoId) {
        return this.count(new LambdaQueryWrapper<VideoStar>().eq(VideoStar::getVideoId,videoId));
    }

    @Override
    public List<Long> getStarUserIds(Long videoId) {
        return this.list(new LambdaQueryWrapper<VideoStar>().eq(VideoStar::getVideoId,videoId)).stream().map(VideoStar::getUserId).collect(Collectors.toList());
    }
}
