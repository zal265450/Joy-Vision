package org.luckyjourney.service.impl;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.luckyjourney.config.QiNiuConfig;
import org.luckyjourney.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;


@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private QiNiuConfig qiNiuConfig;

    String yuming = "s36d82b8z.hn-bkt.clouddn.com";

    @Override
    public String getToken() {

        final Auth auth = qiNiuConfig.buildAuth();
        String bucket = qiNiuConfig.getBucketName();
        String upToken = auth.uploadToken(bucket);
        return upToken;
    }

    @Override
    public void uploadFile(File file) {
        Configuration cfg = new Configuration(Region.region2());
        UploadManager uploadManager = new UploadManager(cfg);
        try {

            Response response = uploadManager.put(file,null,qiNiuConfig.uploadToken());
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            System.out.println(putRet.key);
            System.out.println(putRet.hash);
        } catch (QiniuException ex) {
            ex.printStackTrace();
            if (ex.response != null) {
                System.err.println(ex.response);

                try {
                    String body = ex.response.toString();
                    System.err.println(body);
                } catch (Exception ignored) {
                }
            }
        }
    }

}
