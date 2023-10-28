package org.luckyjourney.service.video.impl;

import org.luckyjourney.entity.Video;
import org.luckyjourney.service.poll.VideoAuditThreadPoll;
import org.luckyjourney.service.video.VideoAuditService;
import org.luckyjourney.service.video.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-10-29 01:27
 */
@Service
public class VideoAuditServiceImpl implements VideoAuditService {

    @Autowired
    private VideoAuditThreadPoll videoAuditThreadPoll;

    @Override
    public void audit(Video video) {
        videoAuditThreadPoll.submit(video);
    }
}
