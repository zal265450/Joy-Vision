package org.luckyjourney.service.video.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.luckyjourney.entity.VideoShare;
import org.luckyjourney.holder.UserHolder;
import org.luckyjourney.mapper.video.VideoShareMapper;
import org.luckyjourney.service.video.VideoShareService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class VideoShareServiceImpl extends ServiceImpl<VideoShareMapper, VideoShare> implements VideoShareService {

    @Override
    public void record(VideoShare videoShare) {
        videoShare.setUserId(UserHolder.get());
        this.save(videoShare);
    }

    @Override
    public long getShareCount(Long videoId) {
        return this.count(new LambdaQueryWrapper<VideoShare>().eq(VideoShare::getVideoId,videoId));
    }

    @Override
    public List<Long> getShareUserId(Long videoId) {
        return this.list(new LambdaQueryWrapper<VideoShare>().eq(VideoShare::getVideoId,videoId)).stream().map(VideoShare::getUserId).collect(Collectors.toList());
    }


}
