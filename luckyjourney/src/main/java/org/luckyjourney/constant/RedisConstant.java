package org.luckyjourney.constant;

/**
 * @description: redis常量
 * @Author: Xhy
 * @CreateTime: 2023-10-25 17:56
 */
public interface RedisConstant {

    String USER_MODEL = "user:model:";

    // 用于兴趣推送时去重
    String HISTORY_VIDEO = "history:video:";

    // 用于用户浏览记录
    String USER_HISTORY_VIDEO = "user:history:video:";

    String SYSTEM_STOCK = "system:stock:";

    String EMAIL_CODE = "email:code:";

    String HOT_RANK = "hot:rank";

    Long HISTORY_TIME = 432000L;

    Long EMAIL_CODE_TIME = 300L;


}
