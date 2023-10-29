package org.luckyjourney.service.video.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.util.StringMap;
import org.luckyjourney.config.QiNiuConfig;
import org.luckyjourney.constant.VideoStatus;
import org.luckyjourney.entity.Type;
import org.luckyjourney.entity.Video;
import org.luckyjourney.entity.json.BodyJson;
import org.luckyjourney.entity.user.User;
import org.luckyjourney.entity.vo.VideoVO;
import org.luckyjourney.holder.UserHolder;
import org.luckyjourney.mapper.video.VideoMapper;
import org.luckyjourney.service.InterestPushService;
import org.luckyjourney.service.poll.VideoAuditThreadPoll;
import org.luckyjourney.service.user.UserService;
import org.luckyjourney.service.video.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private QiNiuConfig qiNiuConfig;

    ThreadPoolExecutor executor;

    ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    String url = "http://ai.qiniuapi.com/v3/video/censor";
    String method = "POST";
    String contentType = "application/json";
    String body = "{\n" +
            "    \"data\": {\n" +
            "        \"uri\": \"${url}\",\n" +
            "        \"id\": \"video_censor_test\"\n" +
            "    },\n" +
            "    \"params\": {\n" +
            "        \"scenes\": [\n" +
            "            \"pulp\",\n" +
            "            \"terror\",\n" +
            "            \"politician\"\n" +
            "        ],\n" +
            "        \"cut_param\": {\n" +
            "            \"interval_msecs\": 5000\n" +
            "        }\n" +
            "    }\n" +
            "}";



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
        // 修改状态
        video.setStatus(VideoStatus.PROCESS);
        video.setUserId(userId);
        // 进入审核队列
        audit(video);

        this.saveOrUpdate(video);
    }

    @Override
    public void publishVideoHandler(Video video) {
        updateById(video);
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


    public void audit(Video video){
        submit(video);
    }

    @Override
    public void submit(Video video) {
        executor.submit(()->{
            if (video.getAuditStatus()) {
                quickly(video);
            }else {
                slow(video);
            }
        });
    }

    // 快速
    public void quickly(Video video){
        new Thread(()->{
            slow(video);
        }).start();
    }
    // 慢速
    public void slow(Video video){
        body.replace("${url}",video.getUrl());
        // 获取token
        final String token = qiNiuConfig.getToken(url, method, body, contentType);
        StringMap header = new StringMap();
        header.put("Host", "ai.qiniuapi.com");
        header.put("Authorization", token);
        header.put("Content-Type", contentType);
        Configuration cfg = new Configuration(Region.region2());
        final Client client = new Client(cfg);
        try {
            Response response = client.post(url, body.getBytes(), header, contentType);
            final Map map = objectMapper.readValue(response.getInfo().split(" \n")[2], Map.class);
            final Object job = map.get("job");
            url = "http://ai.qiniuapi.com/v3/jobs/video/"+job.toString();
            method = "GET";
            header = new StringMap();
            header.put("Host","ai.qiniuapi.com");
            header.put("Authorization",qiNiuConfig.getToken(url,method,null,null));
            while (true) {
                Response response1 = client.get(url, header);
                final BodyJson bodyJson1 = objectMapper.readValue(response1.getInfo().split(" \n")[2], BodyJson.class);
                if (bodyJson1.getStatus().equals("FINISHED")) {
                    // TODO 后续更改为审核宽松度,根据比例放行,暂时先这样
                    if (bodyJson1.getResult().getResult().getSuggestion().equals("pass")){
                        // 审核通过
                        video.setStatus(VideoStatus.SUCCESS);
                    }else {
                        // 不予通过
                        video.setStatus(VideoStatus.PASS);
                    }
                    publishVideoHandler(video);
                    return;
                }
                Thread.sleep(2000L);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    // 用于初始化线程
    @Override
    public void afterPropertiesSet() throws Exception {
        executor  = new ThreadPoolExecutor(5, 8, 60, TimeUnit.SECONDS, new ArrayBlockingQueue(1000));
    }

}
