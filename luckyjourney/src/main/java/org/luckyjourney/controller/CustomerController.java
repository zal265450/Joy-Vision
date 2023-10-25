package org.luckyjourney.controller;

import org.luckyjourney.entity.user.User;
import org.luckyjourney.entity.vo.BasePage;
import org.luckyjourney.entity.vo.FollowVO;
import org.luckyjourney.entity.vo.ModelVO;
import org.luckyjourney.holder.UserHolder;
import org.luckyjourney.service.user.UserService;
import org.luckyjourney.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-10-25 15:27
 */
@RestController
@RequestMapping("/luckyjourney/customer")
public class CustomerController {


    @Autowired
    UserService userService;

    // 获取个人信息
    @GetMapping("/getInfo/{userId}")
    public R getInfo(@PathVariable Long userId){
        return R.ok().data(userService.getInfo(userId));
    }


    // 填写信息,性别,兴趣
    @PostMapping("/setModel")
    public R setModel(@RequestBody @Validated ModelVO modelVO){
        userService.setModel(modelVO);
        return R.ok().message("填写完毕");
    }


    // 获取关注人员
    @GetMapping("/follows")
    public R getFollows(BasePage basePage,Long userId){
        return R.ok().data(userService.getFollows(userId,basePage));
    }

    // 获取粉丝
    @GetMapping("/fans")
    public R getFans(BasePage basePage,Long userId){
        return R.ok().data(userService.getFans(userId,basePage));
    }

}
