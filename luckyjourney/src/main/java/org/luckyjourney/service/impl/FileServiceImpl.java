package org.luckyjourney.service.impl;

import com.qiniu.util.Auth;
import org.luckyjourney.config.KodoConfig;
import org.luckyjourney.entity.bo.KodoPolicyResult;
import org.luckyjourney.service.FileService;
import org.luckyjourney.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 郭建勇
 * @date 2023/10/25
 **/
@Service
public class FileServiceImpl implements FileService {

    /**
     * kodo配置类
     */
    @Autowired
    private KodoConfig kodoConfig;


    @Override
    public R policy() {
        System.out.println(kodoConfig);
        Auth auth = Auth.create(kodoConfig.getAccessKey(), kodoConfig.getSecretKey());
        String upToken = auth.uploadToken(kodoConfig.getBucketName());
        KodoPolicyResult kodoPolicyResult = new KodoPolicyResult();
        kodoPolicyResult.setPolicy(upToken);
        return R.ok().data(kodoPolicyResult);
    }
}
