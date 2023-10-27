package org.luckyjourney.service.impl;

import com.qiniu.util.Auth;
import org.luckyjourney.config.QiNiuConfig;
import org.luckyjourney.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private QiNiuConfig qiNiuConfig;

    @Override
    public String getToken() {

        final Auth auth = qiNiuConfig.buildAuth();
        String bucket = qiNiuConfig.getBucketName();
        String upToken = auth.uploadToken(bucket);
        return upToken;
    }

}
