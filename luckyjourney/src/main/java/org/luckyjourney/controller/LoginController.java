package org.luckyjourney.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.luckyjourney.constant.RedisConstant;
import org.luckyjourney.entity.Captcha;
import org.luckyjourney.entity.user.User;
import org.luckyjourney.entity.vo.FindPWVO;
import org.luckyjourney.entity.vo.RegisterVO;
import org.luckyjourney.service.CaptchaService;
import org.luckyjourney.service.EmailService;
import org.luckyjourney.service.user.UserService;
import org.luckyjourney.util.JwtUtils;
import org.luckyjourney.util.R;
import org.luckyjourney.util.RedisCacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.awt.image.BufferedImage;
import java.io.IOException;
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

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @PostMapping("/login")
    public R login(@RequestBody @Validated User user){
        final String password = user.getPassword();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        user = userService.getOne(wrapper.eq(User::getEmail, user.getEmail()));
        if (ObjectUtils.isEmpty(user)) return R.error().message("没有该账号");

        if (!password.equals(user.getPassword())) return R.error().message("密码不一致");

        // 登录成功，生成token
        String token = JwtUtils.getJwtToken(user.getId(), user.getNickName());
        final HashMap<Object, Object> map = new HashMap<>();
        map.put("token",token);
        map.put("name",user.getNickName());
        return R.ok().data(map);
    }


    @GetMapping("/captcha.jpg/{uuId}")
    public R captcha(HttpServletResponse response, @PathVariable String uuId) throws IOException {
        if (ObjectUtils.isEmpty(uuId)) return R.error().message("uuid不能为空");
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpeg");
        BufferedImage image = captchaService.getCaptcha(uuId);
        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(image, "jpg", out);
        IOUtils.closeQuietly(out);
        return R.ok();
    }


    @PostMapping("/getCode")
    public R getCode(@RequestBody @Validated Captcha captcha) throws Exception {
        if (captchaService.validate(captcha)){
            return R.error().message("验证码错误");
        }
        return R.ok().message("发送成功,请耐心等待");
    }


    @PostMapping("/register")
    public R register(@RequestBody @Validated RegisterVO registerVO) throws Exception {
        userService.register(registerVO);
        return R.ok().message("注册成功");
    }

    /**
     * 找回密码
     * 邮箱
     *
     * @return
     */
    @PostMapping("/findPassword")
    public R findPassword(@RequestBody @Validated FindPWVO findPWVO){
        final Boolean b = userService.findPassword(findPWVO);
        return R.ok().message(b ? "修改成功" : "修改失败,验证码不正确");
    }


    public int randomSixNumber(){
        return (int)((Math.random()*9+1)*100000);
    }
}
