package org.luckyjourney.config;

import com.qiniu.util.Auth;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


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

}