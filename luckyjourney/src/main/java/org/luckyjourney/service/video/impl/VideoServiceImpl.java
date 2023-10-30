package org.luckyjourney.service.video.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.luckyjourney.constant.AuditStatus;
import org.luckyjourney.constant.RedisConstant;
import org.luckyjourney.entity.video.Type;
import org.luckyjourney.entity.video.Video;
import org.luckyjourney.entity.video.VideoShare;
import org.luckyjourney.entity.video.VideoStar;
import org.luckyjourney.entity.response.AuditResponse;
import org.luckyjourney.entity.user.User;
import org.luckyjourney.holder.UserHolder;
import org.luckyjourney.mapper.video.VideoMapper;
import org.luckyjourney.service.AuditService;
import org.luckyjourney.service.FileService;
import org.luckyjourney.service.InterestPushService;
import org.luckyjourney.service.poll.VideoAuditThreadPoll;
import org.luckyjourney.service.user.FavoritesService;
import org.luckyjourney.service.user.UserService;
import org.luckyjourney.service.video.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.luckyjourney.util.RedisCacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author xhy
 * @since 2023-10-24
 */
@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService, VideoAuditThreadPoll {

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

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private AuditService auditService;

    @Autowired
    private FileService fileService;

    @Autowired
    private FavoritesService favoritesService;

    ThreadPoolExecutor executor;

    @Override
    public Video getVideoById(Long videoId) {
        final Video video = this.getOne(new LambdaQueryWrapper<Video>().eq(Video::getId, videoId));
        if (video == null) throw new IllegalArgumentException("指定视频不存在");
        video.setUserName(userService.getById(video.getUserId()).getNickName());
        return video;
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
        video.setYV("YV"+UUID.randomUUID().toString().substring(8));
        // 修改状态
        video.setStatus(AuditStatus.PROCESS);
        video.setUserId(userId);
        // 进入审核队列
        audit(video);

        this.saveOrUpdate(video);
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
        final boolean b = removeById(id);
        if (b){
            // 解耦
            new Thread(()->{
                // 删除分享量 点赞量
                videoShareService.remove(new LambdaQueryWrapper<VideoShare>().eq(VideoShare::getVideoId,id).eq(VideoShare::getUserId,userId));
                videoStarService.remove(new LambdaQueryWrapper<VideoStar>().eq(VideoStar::getVideoId,id).eq(VideoStar::getUserId,userId));
                // 删除七牛云中的视频
                fileService.deleteFile(video.getUrl());
                interestPushService.deleteSystemStockIn(video);
            }).start();
        }
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

    @Override
    public void auditProcess(Video video) {
        // 放行后
        updateById(video);
        // 放入系统库
        if (video.getStatus() == AuditStatus.SUCCESS) {
            interestPushService.pushSystemStockIn(video);
        }
    }

    @Override
    public boolean startVideo(Long videoId) {
        final Video video = getById(videoId);
        if (video == null) throw new IllegalArgumentException("指定视频不存在");

        final VideoStar videoStar = new VideoStar();
        videoStar.setVideoId(videoId);
        videoStar.setUserId(UserHolder.get());
        final boolean result = videoStarService.starVideo(videoStar);
        updateStar(video,result ? 1L : -1L);
        return result;
    }

    @Override
    public boolean shareVideo(VideoShare videoShare) {
        final Video video = getById(videoShare.getVideoId());
        if (video == null) throw new IllegalArgumentException("指定视频不存在");
        final boolean result = videoShareService.share(videoShare);
        updateShare(video,result ? 1L : 0L);
        return result;
    }

    @Override
    @Async
    public void historyVideo(Long videoId,Long userId) {
        String key = RedisConstant.HISTORY_VIDEO + videoId + ":" + userId;
        final Object o = redisCacheUtil.get(key);
        if (o == null){
            redisCacheUtil.set(key,videoId,RedisConstant.HISTORY_TIME);
            final Video video = getById(videoId);
            video.setUserName(userService.getById(video.getUserId()).getNickName());
            video.setTypeName(typeService.getById(video.getTypeId()).getName());
            redisCacheUtil.zadd(RedisConstant.USER_HISTORY_VIDEO+userId,new Date().getTime(),video,RedisConstant.HISTORY_TIME);
            updateHistory(video,1L);
        }
    }

    @Override
    public Collection<Video> getHistory() {

        final Long userId = UserHolder.get();
        String key = RedisConstant.USER_HISTORY_VIDEO + userId;
        final Set videoIds = redisCacheUtil.zGet(key);
        return videoIds;
    }

    @Override
    public Collection<Video> listVideoByFavorites(Long favoritesId) {
        final List<Long> videoIds = favoritesService.listVideoIds(favoritesId, UserHolder.get());
        if (ObjectUtils.isEmpty(videoIds)){
            return Collections.EMPTY_LIST;
        }
        return listByIds(videoIds);
    }


    public void audit(Video video){
        submit(video);
    }

    @Override
    public void submit(Video video) {
        executor.submit(()->{
            final AuditResponse auditResponse = auditService.audit(video.getUrl(), video.getAuditStatus());
            System.out.println(auditResponse);
            video.setStatus(auditResponse.getAuditStatus());
            if (auditResponse.getAuditStatus() == AuditStatus.SUCCESS) {
                interestPushService.pushSystemStockIn(video);
            }
            updateById(video);
        });
    }

    /**
     * 点赞数
     * @param video
     */
    public void updateStar(Video video,Long value){
        final UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("start_count = start_count + " + value);
        updateWrapper.lambda().eq(Video::getId,video.getId()).eq(Video::getStartCount,video.getStartCount());
        update(video,updateWrapper);
    }

    /**
     * 分享数
     * @param video
     */
    public void updateShare(Video video,Long value){
        final UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("share_count = share_count + " + value);
        updateWrapper.lambda().eq(Video::getId,video.getId()).eq(Video::getShareCount,video.getShareCount());
        update(video,updateWrapper);
    }

    /**
     * 浏览量
     * @param video
     */
    public void updateHistory(Video video,Long value){
        final UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("history_count = history_count + " + value);
        updateWrapper.lambda().eq(Video::getId,video.getId()).eq(Video::getHistoryCount,video.getHistoryCount());
        update(video,updateWrapper);
    }

    // 用于初始化线程
    @Override
    public void afterPropertiesSet() throws Exception {
        executor  = new ThreadPoolExecutor(5, 8, 60, TimeUnit.SECONDS, new ArrayBlockingQueue(1000));
    }

}
