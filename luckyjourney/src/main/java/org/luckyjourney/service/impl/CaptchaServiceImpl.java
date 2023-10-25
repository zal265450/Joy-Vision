package org.luckyjourney.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.code.kaptcha.Producer;
import org.luckyjourney.entity.Captcha;
import org.luckyjourney.mapper.CaptchaMapper;
import org.luckyjourney.service.CaptchaService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.luckyjourney.service.EmailService;
import org.luckyjourney.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.validation.constraints.Email;
import java.awt.image.BufferedImage;
import java.util.Date;

/**
 * <p>
 * 系统验证码 服务实现类
 * </p>
 *
 * @author xhy
 * @since 2023-10-25
 */
@Service
public class CaptchaServiceImpl extends ServiceImpl<CaptchaMapper, Captcha> implements CaptchaService {


    @Autowired
    private Producer producer;

    @Autowired
    private EmailService emailService;

    @Override
    public BufferedImage getCaptcha(String uuId) {
        String code = this.producer.createText();
        Captcha captcha = new Captcha();
        captcha.setUuid(uuId);
        captcha.setCode(code);
        captcha.setExpireTime(DateUtil.addDateMinutes(new Date(),5));
        this.save(captcha);
        return producer.createImage(code);
    }

    @Override
    public boolean validate(Captcha captcha) throws Exception {
        String email = captcha.getEmail();
        captcha = this.getOne(new LambdaQueryWrapper<Captcha>().eq(Captcha::getUuid, captcha.getUuid()));
        if (captcha == null) throw new Exception("uuId为空");

        this.removeById(captcha.getUuid());

        if (!captcha.getCode().equalsIgnoreCase(captcha.getCode()) && captcha.getExpireTime().getTime() >= System.currentTimeMillis()){
            return true;
        }
        String code = getSixCode();
        emailService.send(email,"注册验证码:"+code+",验证码5分钟之内有效");
        return false;
    }


    public static String getSixCode(){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int code = (int) (Math.random()*10);
            builder.append(code);
        }
        return builder.toString();
    }
}