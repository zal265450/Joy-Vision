package org.luckyjourney.service.video;

import org.luckyjourney.entity.Video;

/**
 * @description: 视频审核
 * @Author: Xhy
 * @CreateTime: 2023-10-29 01:26
 */
public interface VideoAuditService {

    /**
     * 审核
     * @param video
     */
    void audit(Video video);
}
