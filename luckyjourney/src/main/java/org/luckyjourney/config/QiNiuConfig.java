package org.luckyjourney.config;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.Data;
import org.luckyjourney.holder.UserHolder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.UUID;


/**
 * @author xhy
 * @date 2023-10-25
 **/
@Data
@Component
@ConfigurationProperties(prefix = "qiniu.kodo")
public class QiNiuConfig {
    /**
     * 账号
     */
    private String accessKey;
    /**
     * 密钥
     */
    private String secretKey;
    /**
     * bucketName
     */
    private String bucketName;

    public static final String CNAME = "http://oss.luckjourney.liuscraft.top";
    public static final String VIDEO_URL = "http://ai.qiniuapi.com/v3/video/censor";
    public static final String IMAGE_URL = "http://ai.qiniuapi.com/v3/image/censor";


    public Auth buildAuth() {
        String accessKey = this.getAccessKey();
        String secretKey = this.getSecretKey();
        return Auth.create(accessKey, secretKey);
    }


    public String uploadToken() {
        final Auth auth = buildAuth();
        return auth.uploadToken(bucketName, null, 3600, new
                StringMap().put("mimeLimit", "video/*"));
    }

    public String getToken(String url, String method, String body, String contentType) {

        final Auth auth = buildAuth();
        String qiniuToken = "Qiniu " + auth.signQiniuAuthorization(url, method, body == null ? null : body.getBytes(), contentType);
        return qiniuToken;
    }


}