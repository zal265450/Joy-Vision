package org.luckyjourney.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/**
 *  读取配置文件的配置到本类属性中
 * @author 郭建勇
 * @date 2023-10-25
 **/
@Data
@Component
@ConfigurationProperties(prefix = "qiniu.kodo")
public class KodoConfig {
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


}