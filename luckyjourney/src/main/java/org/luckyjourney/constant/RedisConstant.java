package org.luckyjourney.constant;

/**
 * @description: redis常量
 * @Author: Xhy
 * @CreateTime: 2023-10-25 17:56
 */
public interface RedisConstant {

    String MODEL = "model:";

    // 用于兴趣推送时去重
    String HISTORY_VIDEO = "history:video:";

    // 用于用户浏览记录
    String USER_HISTORY_VIDEO = "user:history:video:";

    String SYSTEM_STOCK = "system:stock:";

    Long HISTORY_TIME = 432000L;
}
