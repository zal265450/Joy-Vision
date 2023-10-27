package org.luckyjourney.controller;


import org.luckyjourney.entity.Video;
import org.luckyjourney.entity.vo.VideoVO;
import org.luckyjourney.service.FileService;
import org.luckyjourney.service.video.VideoService;
import org.luckyjourney.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author xhy
 * @since 2023-10-24
 */
@RestController
@RequestMapping("/luckyjourney/video")
public class VideoController {

    @Autowired
    private VideoService videoService;


    @Autowired
    private FileService fileService;

    @GetMapping("/token")
    public R getToken(){
        final String token = fileService.getToken();
        return R.ok().data(token);
    }

    /**
     * 根据id获取视频
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R getVideoById(@PathVariable Long id){

        VideoVO videoVO = videoService.getVideoById(id);
        return R.ok().data(videoVO);
    }

    /**
     * 根据视频分类获取
     * @param typeId
     * @return
     */
    @GetMapping("/type/{typeId}")
    public R getVideoByTypeId(@PathVariable Long typeId){

        Collection<Video> video = videoService.getVideoByTypeId(typeId);

        return R.ok();
    }


    /**发布视频/修改视频
     * @param video
     * @return
     */
    @PostMapping
    public R publishVideo(@RequestBody @Validated Video video){
        videoService.publishVideo(video);
        return R.ok().message("发布成功");
    }

    /**
     * 删除视频
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public R deleteVideo(@PathVariable Long id){
        videoService.deleteVideo(id);
        return R.ok().message("删除成功");
    }

    /**
     * 点赞视频
     */
    @PostMapping("/star/{id}")
    public R starVideo(@PathVariable Long id){
        videoService.starVideo(id);
        return R.ok().message("点赞成功");
    }
}

