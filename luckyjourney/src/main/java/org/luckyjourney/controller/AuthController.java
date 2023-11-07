package org.luckyjourney.controller;

import com.github.benmanes.caffeine.cache.Cache;
import org.luckyjourney.config.LocalCache;
import org.luckyjourney.entity.Setting;
import org.luckyjourney.exception.BaseException;
import org.luckyjourney.service.SettingService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * @description: 回源鉴权
 * @Author: Xhy
 * @CreateTime: 2023-11-07 14:20
 */
@RestController
@RequestMapping("/luckyjourney/cdn/auth")
public class AuthController implements InitializingBean {


    @Autowired
    private Cache cache;

    @Autowired
    private SettingService settingService;


    @GetMapping("/get")
    public void getUUid(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {

        String ip = request.getHeader("referer");
        if (!LocalCache.containsKey(ip)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        // 如果不是指定ip调用的该接口，则不返回
        final String s = UUID.randomUUID().toString();
        cache.put(s,true);

        if (url.contains("?")){
            url = url+"&uuid="+s;
        }else {
            url = url+"?uuid="+s;
        }
        response.sendRedirect(url);
    }

    @PostMapping
    public void auth(@RequestParam(required = false) String uuid, HttpServletResponse response) throws IOException {
        if (uuid == null || cache.getIfPresent(uuid) == null){
            response.sendError(401);
        }else {
            response.sendError(200);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final Setting setting = settingService.list(null).get(0);
        for (String s : setting.getAllowIp().split(",")) {
            LocalCache.put(s,true);
        }
    }
}
