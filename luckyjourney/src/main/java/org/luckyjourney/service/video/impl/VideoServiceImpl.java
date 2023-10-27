package org.luckyjourney.service.video.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.luckyjourney.entity.Type;
import org.luckyjourney.entity.Video;
import org.luckyjourney.entity.user.User;
import org.luckyjourney.entity.vo.VideoVO;
import org.luckyjourney.holder.UserHolder;
import org.luckyjourney.mapper.video.VideoMapper;
import org.luckyjourney.service.InterestPushService;
import org.luckyjourney.service.user.UserService;
import org.luckyjourney.service.video.TypeService;
import org.luckyjourney.service.video.VideoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.luckyjourney.service.video.VideoShareService;
import org.luckyjourney.service.video.VideoStarService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author xhy
 * @since 2023-10-24
 */
@Service
public class
VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {

    @Autowired
    private TypeService typeService;

    @Autowired
    private InterestPushService interestPushService;

    @Autowired
    private UserService userService;

    @Autowired
    private VideoStarService videoStarService;

    @Autowired
    private VideoShareService videoShareService;

    @Override
    public VideoVO getVideoById(Long videoId) {
        final Video video = this.getOne(new LambdaQueryWrapper<Video>().eq(Video::getId, videoId));
        final VideoVO videoVO = new VideoVO();
        if (video == null){
            return videoVO;
        }
        BeanUtils.copyProperties(video,videoVO);
        // 获取浏览量 todo

        // 获取点赞量
        videoVO.setStars(videoStarService.getStarCount(videoId));
        // 获取分享量
        videoVO.setShares(videoShareService.getShareCount(videoId));
        return videoVO;
    }

    @Override
    public void publishVideo(Video video) {
        final Long userId = UserHolder.get();

        // 不允许修改视频
        if (video.getId()!=null){
            // url不能一致
            final Video old = this.getOne(new LambdaQueryWrapper<Video>().eq(Video::getId, video.getId()).eq(Video::getUserId, userId));
            if (old.getUrl().equals(video.getUrl())){
                throw new IllegalArgumentException("不能更换视频源,只能修改视频信息");
            }
        }
        // 判断对应分类是否存在
        final Type type = typeService.getById(video.getTypeId());
        if (type == null){
            throw new IllegalArgumentException("分类不存在");
        }
        // 判断审核通过 todo
        video.setUserId(userId);
        this.saveOrUpdate(video);

        // 公开后逻辑
        interestPushService.pushSystemStockIn(video);
    }

    @Override
    public void deleteVideo(Long id) {

        if (id == null){
            throw new IllegalArgumentException("删除指定的视频不存在");
        }

        final Long userId = UserHolder.get();
        final Video video = this.getOne(new LambdaQueryWrapper<Video>().eq(Video::getId, id).eq(Video::getUserId, userId));
        if (video == null){
            throw new IllegalArgumentException("删除指定的视频不存在");
        }

        removeById(id);
        // todo 删除分享量 点赞量 浏览量

        interestPushService.deleteSystemStockIn(video);
    }

    @Override
    public Collection<Video> pushVideos() {

        Long userId = UserHolder.get();
        User user = null;
        if (userId!=null){
            user = userService.getById(userId);
        }
        final List<Long> videoIds = interestPushService.listVideoByUserModel(user);


        return listByIds(videoIds);
    }

    @Override
    public Collection<Video> getVideoByTypeId(Long typeId) {

        final Type type = typeService.getById(typeId);
        if (type == null) return Collections.EMPTY_LIST;
        return this.list(new LambdaQueryWrapper<Video>().eq(Video::getTypeId,typeId));
    }

    @Override
    public Collection<Video> searchVideo(String title) {


        return this.list(new LambdaQueryWrapper<Video>().like(Video::getTitle,title));
    }




}
