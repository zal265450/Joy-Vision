package org.luckyjourney.service;

import org.luckyjourney.entity.Captcha;
import org.luckyjourney.entity.user.User;
import org.luckyjourney.entity.vo.FindPWVO;
import org.luckyjourney.entity.vo.RegisterVO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-11-04 12:38
 */
public interface LoginService {


    User login(User user);

    Boolean checkCode(String email,Integer code);

    void captcha(String uuid, HttpServletResponse response) throws IOException;

    Boolean getCode(Captcha captcha) throws Exception;

    Boolean register(RegisterVO registerVO) throws Exception;

    Boolean findPassword(FindPWVO findPWVO);
}
