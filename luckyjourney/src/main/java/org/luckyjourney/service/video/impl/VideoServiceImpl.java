package org.luckyjourney.service.video.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.luckyjourney.config.QiNiuConfig;
import org.luckyjourney.constant.AuditStatus;
import org.luckyjourney.constant.RedisConstant;
import org.luckyjourney.entity.video.Type;
import org.luckyjourney.entity.video.Video;
import org.luckyjourney.entity.video.VideoShare;
import org.luckyjourney.entity.video.VideoStar;
import org.luckyjourney.entity.response.AuditResponse;
import org.luckyjourney.entity.user.User;
import org.luckyjourney.entity.vo.BasePage;
import org.luckyjourney.entity.vo.HotVideo;
import org.luckyjourney.entity.vo.UserVO;
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
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    private Integer maxThreadCount = 8;

    @Override
    public Video getVideoById(Long videoId)   {
        final Video video = this.getOne(new LambdaQueryWrapper<Video>().eq(Video::getId, videoId));
        if (video == null) throw new IllegalArgumentException("指定视频不存在");
        video.setUser(userService.getInfo(video.getUserId()));
        video.setUrl(QiNiuConfig.CNAME+"/"+video.getUrl());
        return video;
    }

    @Override
    public void publishVideo(Video video) {

        UserHolder.set(1L);
        final Long userId = UserHolder.get();

        // 不允许修改视频
        if (video.getId()!=null){
            // url不能一致
            final Video old = this.getOne(new LambdaQueryWrapper<Video>().eq(Video::getId, video.getId()).eq(Video::getUserId, userId));
            if (old.getUrl().equals(video.getUrl()) || old.getCover().equals(video.getCover())){
                throw new IllegalArgumentException("不能更换视频源,只能修改视频信息");
            }
        }
        // 判断对应分类是否存在
        final Type type = typeService.getById(video.getTypeId());
        if (type == null){
            throw new IllegalArgumentException("分类不存在");
        }
        // 校验标签最多不能超过5个
        if (video.getLabels().size() > 5){
            throw new IllegalArgumentException("标签最多只能选择5个");
        }

        video.setYV("YV"+UUID.randomUUID().toString().replace("-","").substring(8));
        // 修改状态
        video.setAuditStatus(AuditStatus.PROCESS);
        video.setUserId(userId);
        // 首次才需要进入审核队列
        if (video.getId() == null){
            audit(video);
        }
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
        final Collection<Long> videoIds = interestPushService.listVideoIdByUserModel(user);
        Collection<Video> videos = new ArrayList<>();

        if (!ObjectUtils.isEmpty(videoIds)){
            videos = listByIds(videoIds);
            setUserVoAndUrl(videos);
        }
        return videos;
    }

    @Override
    public Collection<Video> getVideoByTypeId(Long typeId) {
        if (typeId == null) return Collections.EMPTY_LIST;
        final Type type = typeService.getById(typeId);
        if (type == null) return Collections.EMPTY_LIST;

        final Collection<Long> videoIds = interestPushService.listVideoIdByTypeId(typeId);
        final Collection<Video> videos = listByIds(videoIds);

        setUserVoAndUrl(videos);
        return videos;
    }

    @Override
    public IPage<Video> searchVideo(String title, BasePage basePage) {
        final IPage<Video> page = this.page(basePage.page(), new LambdaQueryWrapper<Video>().like(Video::getTitle, title));
        final List<Video> videos = page.getRecords();
        setUserVoAndUrl(videos);
        return page;
    }

    @Override
    public void auditProcess(Video video) {
        // 放行后
        updateById(video);
        // 放入系统库
        if (video.getAuditStatus() == AuditStatus.SUCCESS) {
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
    public void historyVideo(Long videoId,Long userId)   {
        String key = RedisConstant.HISTORY_VIDEO + videoId + ":" + userId;
        final Object o = redisCacheUtil.get(key);
        if (o == null){
            redisCacheUtil.set(key,videoId,RedisConstant.HISTORY_TIME);
            final Video video = getById(videoId);
            video.setUser(userService.getInfo(video.getUserId()));
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

    @Override
    public List<HotVideo> hotRank() {
        final Set<ZSetOperations.TypedTuple<Object>> zSet = redisCacheUtil.getZSet(RedisConstant.HOT_RANK);
        final ArrayList<HotVideo> hotVideos = new ArrayList<>();
        for (ZSetOperations.TypedTuple<Object> objectTypedTuple : zSet) {
            final HotVideo hotVideo = (HotVideo) objectTypedTuple.getValue();
            hotVideo.setHot(objectTypedTuple.getScore());
            hotVideos.add(hotVideo);
        }
        return hotVideos;
    }

    @Override
    public boolean favorites(Long fId, Long vId) {
        final Video video = getById(vId);
        if (video == null){
            throw new IllegalArgumentException("指定视频不存在");
        }
        final boolean favorites = favoritesService.favorites(fId, vId);
        updateFavorites(video, favorites ? 1L : -1L);
        return favorites;
    }

    @Override
    public Collection<Video> listSimilarVideo(List<String> labels) {

        if (ObjectUtils.isEmpty(labels)) return Collections.EMPTY_LIST;
        final ArrayList<String> labelNames = new ArrayList<>();
        labelNames.addAll(labels);
        labelNames.addAll(labels);
        final Collection<Long> videoIds = interestPushService.listVideoIdByLabels(labelNames);

        Collection<Video> videos = new ArrayList<>();

        if (!ObjectUtils.isEmpty(videoIds)){
            videos = listByIds(videoIds);
            setUserVoAndUrl(videos);
        }
        return videos;
    }

    @Override
    public IPage<Video> listByUserId(Long userId, BasePage basePage) {

        final IPage<Video> page = page(basePage.page(), new LambdaQueryWrapper<Video>().eq(Video::getUserId, userId).orderByDesc(Video::getGmtCreated));
        final List<Video> videos = page.getRecords();
        setUserVoAndUrl(videos);
        return page;
    }

    @Override
    public String getAuditQueueState() {
        final int activeCount = executor.getActiveCount();
        return activeCount < maxThreadCount ? "快速" : "慢速";
    }


    public void setUserVoAndUrl(Collection<Video> videos){
        final List<Long> userIds = videos.stream().map(Video::getUserId).collect(Collectors.toList());
        final Map<Long, String> userMap = userService.list(userIds).stream().collect(Collectors.toMap(User::getId, User::getNickName));
        for (Video video : videos) {
            final UserVO userVO = new UserVO();
            userVO.setId(video.getUserId());
            userVO.setNickName(userMap.get(video.getUserId()));
            video.setUser(userVO);
            video.setUrl(QiNiuConfig.CNAME+"/"+video.getUrl());
        }
    }

    public void audit(Video video){

        submit(video);
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

    public void updateFavorites(Video video,Long value){
        final UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("favorites_count = favorites_count + " + value);
        updateWrapper.lambda().eq(Video::getId,video.getId()).eq(Video::getFavoritesCount,video.getFavoritesCount());
        update(video,updateWrapper);
    }



    @Override
    public void submit(Video video) {
        executor.submit(()->{
            // 审核视频
            final AuditResponse videoAuditResponse = auditService.audit(QiNiuConfig.CNAME+"/"+video.getUrl(), video.getAuditQueueStatus(),QiNiuConfig.VIDEO_URL);
            //审核封面
            final AuditResponse coverAuditResponse = auditService.audit(video.getCover(), video.getAuditQueueStatus(),QiNiuConfig.IMAGE_URL);
            final Integer videoAuditStatus = videoAuditResponse.getAuditStatus();
            final Integer coverAuditStatus = coverAuditResponse.getAuditStatus();
            boolean f1 = videoAuditStatus == AuditStatus.SUCCESS;
            boolean f2 = coverAuditStatus == AuditStatus.SUCCESS;
            if (f1 && f2) {
                video.setMsg("通过");
                video.setAuditStatus(AuditStatus.SUCCESS);
                interestPushService.pushSystemStockIn(video);
            }else {
                video.setAuditStatus(AuditStatus.PASS);
                video.setMsg(f1 ? coverAuditResponse.getMsg() : videoAuditResponse.getMsg());
            }
            updateById(video);
        });
    }


    // 用于初始化线程
    @Override
    public void afterPropertiesSet() throws Exception {
        executor  = new ThreadPoolExecutor(5, maxThreadCount, 60, TimeUnit.SECONDS, new ArrayBlockingQueue(1000));
    }

}
