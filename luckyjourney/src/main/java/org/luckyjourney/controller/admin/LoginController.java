package org.luckyjourney.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.luckyjourney.entity.user.User;
import org.luckyjourney.service.user.UserService;
import org.luckyjourney.util.JwtUtils;
import org.luckyjourney.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-10-24 16:29
 */

@RestController
public class LoginController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public R login(String username, String password){
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        User user = userService.getOne(wrapper.eq(User::getUserName, username));
        if (ObjectUtils.isEmpty(user)) return R.error().message("没有该账号");

        if (!user.getPassword().equals(password)) return R.error().message("密码不一致");

        // 登录成功，生成token
        String token = JwtUtils.getJwtToken(user.getId(), user.getUserName());
        final HashMap<Object, Object> map = new HashMap<>();
        map.put("token",token);
        map.put("name",username);
        return R.ok().data(map);
    }
}
