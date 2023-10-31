package org.luckyjourney.controller;


import org.luckyjourney.entity.video.Video;
import org.luckyjourney.holder.UserHolder;
import org.luckyjourney.service.FileService;
import org.luckyjourney.service.video.VideoService;
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


    @Autowired
    private FileService fileService;

    /**
     * 获取文件上传token
     * @return
     */
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
        return R.ok().message("发布成功,请等待审核");
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
        if (!videoService.startVideo(id)) {
            msg = "取消点赞";
        }
        return R.ok().message(msg);
    }

    /**
     * 添加历史记录
     * @return
     */
    @PostMapping("/history/{id}")
    public R addHistory(@PathVariable Long id){
        videoService.historyVideo(id, UserHolder.get());
        return R.ok();
    }

    /**
     * 添加浏览记录
     * @return
     */
    @GetMapping("/history")
    public R getHistory(){
        return R.ok().data(videoService.getHistory());
    }

    /**
     * 获取收藏夹下的视频
     */
    @GetMapping("/favorites/{favoritesId}")
    public R listVideoByFavorites(@PathVariable Long favoritesId){
        return R.ok().data(videoService.listVideoByFavorites(favoritesId));
    }

    /**
     * 收藏视频
     * @param fId
     * @param vId
     * @return
     */
    @PostMapping("/favorites/{fId}/{vId}")
    public R favoritesVideo(@PathVariable Long fId,@PathVariable Long vId){
        String msg = videoService.favorites(fId,vId) ? "已收藏" : "取消收藏";
        return R.ok().message(msg);
    }

}

