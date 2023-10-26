package org.luckyjourney.controller;


import org.luckyjourney.entity.Video;
import org.luckyjourney.service.FileService;
import org.luckyjourney.service.VideoService;
import org.luckyjourney.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 根据id获取视频信息
     *
     * @return 视频信息
     */
    @GetMapping("/info/{videoId}")
    public R getVideoInfo(@PathVariable Long videoId) {
        Video video = videoService.getById(videoId);
        if (video == null) {
            return R.error();
        }
        return R.ok().data(video);
    }

    /**
     * 上传一个视频
     *
     * @return 视频信息
     */
    @GetMapping("/post")
    public R postVideo(@RequestBody @Validated Video video) {
        return videoService.postVideo(video);
    }


}

