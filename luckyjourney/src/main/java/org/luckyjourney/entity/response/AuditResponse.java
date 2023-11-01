package org.luckyjourney.entity.response;

import lombok.Data;
import lombok.ToString;

/**
 * @description: 审核返回结果
 * @Author: Xhy
 * @CreateTime: 2023-10-29 14:40
 */
@Data
@ToString
public class AuditResponse {

    Integer auditStatus;
    // true:违规 false:正常
    Boolean flag;
    // 信息
    String msg;

    Long offset;
}
