package org.luckyjourney.controller;


import org.luckyjourney.entity.Video;
import org.luckyjourney.entity.vo.VideoVO;
import org.luckyjourney.service.FileService;
import org.luckyjourney.service.video.VideoService;
import org.luckyjourney.service.video.VideoStarService;
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

    @Autowired
    private VideoStarService videoStarService;

    @GetMapping("/token")
    public R getToken(){
        final String token = fileService.getToken();
        return R.ok().data(token);
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
        String msg = "已点赞";
        if (!videoStarService.starVideo(id)) {
            msg = "取消点赞";
        }
        return R.ok().message(msg);
    }
}

