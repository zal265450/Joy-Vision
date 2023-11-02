package org.luckyjourney.entity.task;

import lombok.Data;
import org.luckyjourney.entity.video.Video;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-11-02 13:57
 */
@Data
public class VideoTask {

    private Video video;

    // 是否是新增
    private Boolean isAdd;

   // 老状态 : 0 公开  1 私密
    private Boolean oldState;

    private Boolean newState;
}
