package org.luckyjourney.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.luckyjourney.authority.Authority;
import org.luckyjourney.constant.AuditStatus;
import org.luckyjourney.entity.video.Type;
import org.luckyjourney.entity.video.Video;
import org.luckyjourney.entity.user.User;
import org.luckyjourney.entity.vo.BasePage;
import org.luckyjourney.service.user.UserService;
import org.luckyjourney.service.video.TypeService;
import org.luckyjourney.service.video.VideoService;
import org.luckyjourney.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
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


    @Autowired
    private TypeService typeService;

    @GetMapping("/{id}")
    @Authority("admin:video:get")
    public R get(@PathVariable Long id){
        return R.ok().data(videoService.getVideoById(id,null));
    }


    @GetMapping("/page")
    @Authority("admin:video:page")
    public R list(BasePage basePage){
        final IPage<Video> page = videoService.page(basePage.page(), null);

        final List<Video> records = page.getRecords();
        if (ObjectUtils.isEmpty(records)) return R.ok();

        final ArrayList<Long> userIds = new ArrayList<>();
        final ArrayList<Long> typeIds = new ArrayList<>();
        for (Video video : records) {
            userIds.add(video.getUserId());
            typeIds.add(video.getTypeId());
        }

        final Map<Long, String> userMap = userService.list(new LambdaQueryWrapper<User>().select(User::getId, User::getNickName)
        .in(User::getId,userIds))
                .stream().collect(Collectors.toMap(User::getId, User::getNickName));

        final Map<Long, String> typeMap = typeService.listByIds(typeIds).stream().collect(Collectors.toMap(Type::getId, Type::getName));

        for (Video video : records) {
            video.setAuditStateName(AuditStatus.getName(video.getAuditStatus()));
            video.setUserName(userMap.get(video.getUserId()));
            video.setOpenName(video.getOpen() ? "公开" : "私密");
            video.setTypeName(typeMap.get(video.getTypeId()));
        }
        return R.ok().data(records).count(records.size());
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
