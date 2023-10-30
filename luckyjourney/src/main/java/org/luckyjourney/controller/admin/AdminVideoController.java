package org.luckyjourney.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.luckyjourney.authority.Authority;
import org.luckyjourney.entity.video.Video;
import org.luckyjourney.entity.user.User;
import org.luckyjourney.entity.vo.BasePage;
import org.luckyjourney.service.user.UserService;
import org.luckyjourney.service.video.VideoService;
import org.luckyjourney.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description: 视频管理端
 * @Author: Xhy
 * @CreateTime: 2023-10-29 12:41
 */
@RestController
@RequestMapping("/admin/video")
public class AdminVideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private UserService userService;


    @GetMapping("/{id}")
    @Authority("admin:video:get")
    public R get(@PathVariable Long id){
        return R.ok().data(videoService.getVideoById(id));
    }


    @GetMapping("/page")
    @Authority("admin:video:page")
    public R list(BasePage basePage){
        final IPage<Video> page = videoService.page(basePage.page(), null);
        final Map<Long, String> userMap = userService.list(new LambdaQueryWrapper<User>().select(User::getId, User::getNickName))
                .stream().collect(Collectors.toMap(User::getId, User::getNickName));
        for (Video video : page.getRecords()) {
            video.setUserName(userMap.get(video.getUserId()));
        }
        return R.ok().data(page.getRecords()).count(page.getRecords().size());
    }

    @DeleteMapping("/{id}")
    @Authority("admin:video:delete")
    public R delete(@PathVariable Long id){
        videoService.deleteVideo(id);
        return R.ok().message("删除成功");
    }

    @PostMapping("/audit")
    @Authority("admin:video:audit")
    public R audit(@RequestBody Video video){
        videoService.auditProcess(video);
        return R.ok().message("审核放行");
    }
}
