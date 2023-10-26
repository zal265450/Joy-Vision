package org.luckyjourney.entity.bo;

import lombok.Data;

/**
 * @author 郭建勇
 * @date 2023/10/25
 **/
@Data
public class KodoPolicyResult {

    /**
     * 用户表单上传的策略,经过base64编码过的字符串
     */
    private String policy;
}
