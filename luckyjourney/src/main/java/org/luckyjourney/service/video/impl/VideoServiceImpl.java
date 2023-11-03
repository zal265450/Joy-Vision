package org.luckyjourney.service.video.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.luckyjourney.config.QiNiuConfig;
import org.luckyjourney.constant.AuditStatus;
import org.luckyjourney.constant.RedisConstant;
import org.luckyjourney.entity.task.VideoTask;
import org.luckyjourney.entity.user.User;
import org.luckyjourney.entity.video.Type;
import org.luckyjourney.entity.video.Video;
import org.luckyjourney.entity.video.VideoShare;
import org.luckyjourney.entity.video.VideoStar;
import org.luckyjourney.entity.vo.BasePage;
import org.luckyjourney.entity.vo.HotVideo;
import org.luckyjourney.entity.vo.UserModel;
import org.luckyjourney.entity.vo.UserVO;
import org.luckyjourney.holder.UserHolder;
import org.luckyjourney.mapper.video.VideoMapper;
import org.luckyjourney.service.FileService;
import org.luckyjourney.service.InterestPushService;
import org.luckyjourney.service.audit.AuditService;
import org.luckyjourney.service.audit.VideoPublishAuditServiceImpl;
import org.luckyjourney.service.user.FavoritesService;
import org.luckyjourney.service.user.UserService;
import org.luckyjourney.service.video.TypeService;
import org.luckyjourney.service.video.VideoService;
import org.luckyjourney.service.video.VideoShareService;
import org.luckyjourney.service.video.VideoStarService;
import org.luckyjourney.util.RedisCacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {

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
    private FavoritesService favoritesService;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private VideoPublishAuditServiceImpl videoPublishAuditService;

    final ObjectMapper objectMapper = new ObjectMapper();



    @Override
    public Video getVideoById(Long videoId)   {
        final Video video = this.getOne(new LambdaQueryWrapper<Video>().eq(Video::getId, videoId));
        if (video == null) throw new IllegalArgumentException("指定视频不存在");
        if (video.getOpen()) return new Video();

        // 异步
        video.setUrl(QiNiuConfig.CNAME+"/"+video.getUrl());
        // 当前视频用户自己是否有收藏/点赞过信息
        final CompletableFuture<Object> future = new CompletableFuture<>();
        // 这里需要优化 todo
        video.setUser(userService.getInfo(video.getUserId()));
        final Long userId = UserHolder.get();
        video.setStart(videoStarService.starState(videoId, userId));
        video.setFavorites(favoritesService.favoritesState(videoId,userId));
        return video;
    }



    @Override
    public void publishVideo(Video video) {

        final Long userId = UserHolder.get();
        Video old = new Video();
        // 不允许修改视频
        final Long videoId = video.getId();
        if (videoId !=null){
            // url不能一致
            old = this.getOne(new LambdaQueryWrapper<Video>().eq(Video::getId, videoId).eq(Video::getUserId, userId));
            if (!(old.getUrl().equals(video.getUrl())) || !(old.getCover().equals(video.getCover()))){
                throw new IllegalArgumentException("不能更换视频源,只能修改视频信息");
            }
        }
        // 判断对应分类是否存在
        final Type type = typeService.getById(video.getTypeId());
        if (type == null){
            throw new IllegalArgumentException("分类不存在");
        }
        // 校验标签最多不能超过5个
        if (video.buildLabel().size() > 5){
            throw new IllegalArgumentException("标签最多只能选择5个");
        }


        // 修改状态
        video.setAuditStatus(AuditStatus.PROCESS);
        video.setUserId(userId);

        boolean isAdd = videoId == null ? true : false;

        if (!isAdd){
            video.setDuration(null);
            video.setVideoType(null);
            video.setLabelNames(null);
        }else {
            video.setYV("YV"+UUID.randomUUID().toString().replace("-","").substring(8));
        }

        this.saveOrUpdate(video);
        /**
         * 新增：审核通过并且是公开才需要放入系统库
         * 修改：审核通过并且是是公开，如果是修改则从系统库中删除
         * 得再审核完成后中进行判断
         *
         * 1.新增：需要将视频封面，视频源，标题，简介进行审核
         * 2.修改：需要将标题和简介进行审核
         */
        final VideoTask videoTask = new VideoTask();
        videoTask.setOldVideo(old);
        videoTask.setVideo(video);
        videoTask.setIsAdd(isAdd);
        videoTask.setOldState(isAdd ? video.getOpen() : old.getOpen());
        videoTask.setNewState(video.getOpen());
        videoPublishAuditService.audit(videoTask,false);
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
                interestPushService.deleteSystemStockIn(video);
            }).start();
        }
    }

    @Override
    public Collection<Video> pushVideos() {
        // todo
        UserHolder.set(1);
        Long userId = UserHolder.get();
        User user = null;
        if (userId!=null){
            user = userService.getById(userId);
        }
        Collection<Long> videoIds = interestPushService.listVideoIdByUserModel(user);
        Collection<Video> videos = new ArrayList<>();

        if (ObjectUtils.isEmpty(videoIds)){
            videoIds = list(new LambdaQueryWrapper<Video>().orderByDesc(Video::getGmtCreated)).stream().map(Video::getId).collect(Collectors.toList());
            videoIds = new HashSet<>(videoIds).stream().limit(10).collect(Collectors.toList());
        }
        videos = listByIds(videoIds);
        setUserVoAndUrl(videos);
        return videos;
    }

    @Override
    public Collection<Video> getVideoByTypeId(Long typeId) {
        if (typeId == null) return Collections.EMPTY_LIST;
        final Type type = typeService.getById(typeId);
        if (type == null) return Collections.EMPTY_LIST;

        Collection<Long> videoIds = interestPushService.listVideoIdByTypeId(typeId);
        if (ObjectUtils.isEmpty(videoIds)){
            // 随便给点视频 测试用
            videoIds = list(new LambdaQueryWrapper<Video>().orderByDesc(Video::getGmtCreated)).stream().map(Video::getId).collect(Collectors.toList());
            videoIds = new HashSet<>(videoIds).stream().limit(10).collect(Collectors.toList());
        }
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
        // 获取标签
        final List<String> labels = video.buildLabel();

        final UserModel userModel = UserModel.buildUserModel(labels, videoId, 1.0);
        interestPushService.updateUserModel(userModel);

        return result;
    }

    @Override
    public boolean favoritesVideo(Long fId, Long vId) {
        final Video video = getById(vId);
        if (video == null){
            throw new IllegalArgumentException("指定视频不存在");
        }
        final boolean favorites = favoritesService.favorites(fId, vId);
        updateFavorites(video, favorites ? 1L : -1L);

        final List<String> labels = video.buildLabel();

        final UserModel userModel = UserModel.buildUserModel(labels, vId, 2.0);
        interestPushService.updateUserModel(userModel);

        return favorites;
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
    public LinkedHashMap<String, List<Video>> getHistory(BasePage basePage) {

        final Long userId = UserHolder.get();
        String key = RedisConstant.USER_HISTORY_VIDEO + userId;
        final Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisCacheUtil.zSetGetByPage(key, basePage.getPage(), basePage.getLimit());
        if (ObjectUtils.isEmpty(typedTuples)){
            return new LinkedHashMap<>();
        }
        List<Video> temp = new ArrayList<>();
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final LinkedHashMap<String,List<Video>> result = new LinkedHashMap<>();
        for (ZSetOperations.TypedTuple<Object> typedTuple : typedTuples) {
            final Date date = new Date(typedTuple.getScore().longValue());
            final String format = simpleDateFormat.format(date);
            if (!result.containsKey(format)) {
                result.put(format,new ArrayList<>());
            }
            final Video video = (Video) typedTuple.getValue();
            result.get(format).add(video);
            temp.add(video);
        }
        setUserVoAndUrl(temp);

        return result;
    }

    @Override
    public Collection<Video> listVideoByFavorites(Long favoritesId) {
        final List<Long> videoIds = favoritesService.listVideoIds(favoritesId, UserHolder.get());
        if (ObjectUtils.isEmpty(videoIds)){
            return Collections.EMPTY_LIST;
        }
        final Collection<Video> videos = listByIds(videoIds);
        setUserVoAndUrl(videos);
        return videos;
    }

    @Override
    public List<HotVideo> hotRank() {
        final Set<ZSetOperations.TypedTuple<Object>> zSet = redisCacheUtil.getZSet(RedisConstant.HOT_RANK);
        final ArrayList<HotVideo> hotVideos = new ArrayList<>();
        for (ZSetOperations.TypedTuple<Object> objectTypedTuple : zSet) {
            final HotVideo hotVideo;
            try {
                hotVideo = objectMapper.readValue(objectTypedTuple.getValue().toString(), HotVideo.class);
                hotVideo.setHot(objectTypedTuple.getScore());
                hotVideos.add(hotVideo);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return hotVideos;
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
        return videoPublishAuditService.getAuditQueueState() ? "快速" : "慢速";
    }

    @Override
    public List<Video> selectNDaysAgeVideo(long id,int days,int limit) {
        return videoMapper.selectNDaysAgeVideo(id,days,limit);
    }

    @Override
    public Collection<Video> listHotVideo() {

        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DATE);

        final HashMap<String, Integer> map = new HashMap<>();
        map.put(RedisConstant.HOT_VIDEO+today,5);
        map.put(RedisConstant.HOT_VIDEO+(today-1),3);
        map.put(RedisConstant.HOT_VIDEO+(today-2),2);

        // 游客不用记录
        // 获取今天日期
        final List<Long> videoIds = redisCacheUtil.pipeline(connection -> {
            map.forEach((k, v) -> {
                connection.sRandMember(k.getBytes(), v);
            });
            return null;
        });
        final HashSet<Long> vIds = new HashSet<>();
        // 会返回结果有null，做下校验
        for (Object videoId : videoIds) {
            if (!ObjectUtils.isEmpty(videoId)){
                vIds.addAll((Collection<? extends Long>) videoId);
            }

        }
        // 和浏览记录做交集? 不需要做交集，热门视频和兴趣推送不一样
        final Collection<Video> videos = listByIds(new HashSet<>(vIds));
        setUserVoAndUrl(videos);
        return videos;
    }


    public void setUserVoAndUrl(Collection<Video> videos){
        if (!ObjectUtils.isEmpty(videos)){
            final Set<Long> userIds = videos.stream().map(Video::getUserId).collect(Collectors.toSet());
            final Map<Long, String> userMap = userService.list(userIds).stream().collect(Collectors.toMap(User::getId, User::getNickName));
            for (Video video : videos) {
                final UserVO userVO = new UserVO();
                userVO.setId(video.getUserId());
                userVO.setNickName(userMap.get(video.getUserId()));
                video.setUser(userVO);
                video.setUrl(QiNiuConfig.CNAME+"/"+video.getUrl());
            }
        }

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





}
