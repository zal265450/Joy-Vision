package org.luckyjourney.service.audit;

import java.util.function.Supplier;

/**
 * @description: 用于处理审核
 * @Author: Xhy
 * @CreateTime: 2023-10-29 14:39
 */
public interface AuditService<T> {

    /**
     *  审核，接受一个任务且执行
     * @param task
     * @return
     */
    T audit(T task);
}
