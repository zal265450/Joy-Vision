package org.luckyjourney.controller;

import org.luckyjourney.entity.Video;
import org.luckyjourney.entity.VideoShare;
import org.luckyjourney.entity.user.User;
import org.luckyjourney.service.InterestPushService;
import org.luckyjourney.service.user.UserService;
import org.luckyjourney.service.video.VideoService;
import org.luckyjourney.util.JwtUtils;
import org.luckyjourney.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-10-27 16:06
 */
@RestController
@RequestMapping("/index")
public class IndexController {


    @Autowired
    private VideoService videoService;

    // 推送视频
    @GetMapping("/pushVideos")
    public R pushVideos(){
        return R.ok().data(videoService.pushVideos());
    }

    /**
     * 搜索视频
     * @return
     */
    @GetMapping("/search/{title}")
    public R searchVideo(@PathVariable String title){
        Collection<Video> videos = videoService.searchVideo(title);
        return R.ok().data(videos);
    }
}
