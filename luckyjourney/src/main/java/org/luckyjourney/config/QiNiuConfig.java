package org.luckyjourney.config;

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

    public Auth buildAuth(){
        String accessKey = this.getAccessKey();
        String secretKey = this.getSecretKey();
        return Auth.create(accessKey, secretKey);
    }

    public String uploadToken(){
        final Auth auth = buildAuth();
        UserHolder.set(10);
        final Long userId = UserHolder.get();
        // 文件隔离
        String key = userId+"/"+ UUID.randomUUID().toString();
        return auth.uploadToken(bucketName,key,3600,new
                StringMap().put("mimeLimit","video/*"));
    }

    public String getToken(String url,String method,String body,String contentType){

        final Auth auth = buildAuth();
        String qiniuToken = "Qiniu "+ auth.signQiniuAuthorization(url, method, body == null ? null : body.getBytes(), contentType);
        return qiniuToken;
    }

}