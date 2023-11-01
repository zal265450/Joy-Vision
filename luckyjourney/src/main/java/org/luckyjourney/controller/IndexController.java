package org.luckyjourney.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.luckyjourney.entity.video.Type;
import org.luckyjourney.entity.video.Video;
import org.luckyjourney.entity.video.VideoShare;
import org.luckyjourney.entity.vo.BasePage;
import org.luckyjourney.service.video.TypeService;
import org.luckyjourney.service.video.VideoService;
import org.luckyjourney.util.JwtUtils;
import org.luckyjourney.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collection;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-10-27 16:06
 */
@RestController
@RequestMapping("/luckyjourney/index")
public class IndexController {


    @Autowired
    private VideoService videoService;

    @Autowired
    private TypeService typeService;

    /**
     * 推送视频
     * @return
     */
    @GetMapping("/pushVideos")
    public R pushVideos(){
        return R.ok().data(videoService.pushVideos());
    }

    /**
     * 搜索视频
     * @return
     */
    @GetMapping("/search")
    public R searchVideo(@RequestParam(required = false) String searchName, BasePage basePage){
        return R.ok().data( videoService.searchVideo(searchName,basePage));
    }

    /**
     * 根据视频分类获取
     * @param typeId
     * @return
     */
    @GetMapping("/video/type/{typeId}")
    public R getVideoByTypeId(@PathVariable Long typeId){

        return R.ok().data(videoService.getVideoByTypeId(typeId));
    }

    /**
     * 获取所有分类
     * @return
     */
    @GetMapping("/types")
    public R getTypes(){
        return R.ok().data(typeService.list(new LambdaQueryWrapper<Type>().select(Type::getIcon,Type::getId,Type::getName)));
    }

    /**
     * 分享视频
     * @param videoId
     * @param request
     * @return
     */
    @PostMapping("/share/{videoId}")
    public R share(@PathVariable Long videoId, HttpServletRequest request){

        String ip = null;
        if (request.getHeader("x-forwarded-for") == null)
            ip = request.getRemoteAddr();
        else
            ip = request.getHeader("x-forwarded-for");
        final VideoShare videoShare = new VideoShare();

        videoShare.setVideoId(videoId);
        videoShare.setIp(ip);
        if (JwtUtils.checkToken(request)) {
            videoShare.setUserId(JwtUtils.getUserId(request));
        }
        videoService.shareVideo(videoShare);
        return R.ok();
    }

    /**
     * 根据id获取视频
     * @param id
     * @return
     */
    @GetMapping("/video/{id}")
    public R getVideoById(@PathVariable Long id) throws Exception {

        return R.ok().data(videoService.getVideoById(id));
    }

    /**
     * 获取热度排行榜
     * @return
     */
    @GetMapping("/video/hot/rank")
    public R listHotRank(){
        return R.ok().data(videoService.hotRank());
    }

    /**
     * 根据视频标签推送相似视频
     * @param labels
     * @return
     */
    @GetMapping("/video/similar")
    public R pushVideoSimilar(@RequestParam String labels){
        return R.ok().data(videoService.listSimilarVideo(Arrays.asList(labels.split(","))));
    }

    /**
     * 推送热门视频
     * @return
     */
    @GetMapping("/video/hot")
    public R listHotVideo(){
        return R.ok().data(videoService.listHotVideo());
    }
}
