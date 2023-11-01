package org.luckyjourney.service;

import org.luckyjourney.entity.response.AuditResponse;

/**
 * @description: 用于处理审核
 * @Author: Xhy
 * @CreateTime: 2023-10-29 14:39
 */
public interface AuditService {

    /**
     *
     * @param url 对应链接
     * @param auditState 快慢审核
     * @param urlType 用于审核类型
     * @return
     */
    AuditResponse audit(String url,boolean auditState,String urlType);
}
