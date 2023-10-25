package org.luckyjourney.service.impl;

import org.luckyjourney.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-10-25 15:07
 */
@Service
public class EmailServiceImpl implements EmailService {


    @Autowired
    private SimpleMailMessage simpleMailMessage;

    @Autowired
    JavaMailSender javaMailSender;

    @Override
    @Async
    public void send(String email, String context) {
        // todo 使用redis保存
//        Object code = redisCacheUtil.get(email+":");
//        code01 = ObjectUtils.isEmpty(code) ? code01 : code.toString();
        simpleMailMessage.setSubject("抖鸭");
        simpleMailMessage.setFrom("1416318023@qq.com");
        simpleMailMessage.setTo(email);
        simpleMailMessage.setText(context);
        javaMailSender.send(simpleMailMessage);
//        redisCacheUtil.set(email+ ":",code,300);
    }
}
