package org.luckyjourney.controller;

import com.github.benmanes.caffeine.cache.Cache;
import com.sun.xml.internal.ws.policy.PolicyMapUtil;
import org.checkerframework.checker.units.qual.A;
import org.luckyjourney.config.LocalCache;
import org.luckyjourney.config.QiNiuConfig;
import org.luckyjourney.entity.Setting;
import org.luckyjourney.exception.BaseException;
import org.luckyjourney.service.FileService;
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
    private SettingService settingService;

    @Autowired
    private FileService fileService;

    @GetMapping("/get") // url = > 文件表id
    public void getUUid(HttpServletRequest request, HttpServletResponse response, Long fileId) throws IOException {

        String ip = request.getHeader("referer");
        if (!LocalCache.containsKey(ip)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        // 如果不是指定ip调用的该接口，则不返回
        final String s = UUID.randomUUID().toString();
        LocalCache.put(s,true);
        String url = QiNiuConfig.CNAME + "/" + fileService.getById(fileId).getFileKey();
        if (url.contains("?")){
            url = url+"&uuid="+s;
        }else {
            url = url+"?uuid="+s;
        }
        response.sendRedirect(url);
    }

    @PostMapping
    public void auth(@RequestParam(required = false) String uuid, HttpServletResponse response) throws IOException {
        if (uuid == null || LocalCache.containsKey(uuid) == null){
            response.sendError(401);
        }else {
            LocalCache.rem(uuid);
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
