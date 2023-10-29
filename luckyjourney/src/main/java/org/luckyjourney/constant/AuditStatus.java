package org.luckyjourney.constant;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-10-29 01:16
 */
public interface AuditStatus {
    Integer SUCCESS = 0; // 通过
    Integer PROCESS = 1; // 审核中
    Integer PASS = 2; // 失败
    Integer MANUAL = 4; // 需要人工审核
}
