package org.luckyjourney.service;

import org.luckyjourney.entity.Video;
import com.baomidou.mybatisplus.extension.service.IService;
import org.luckyjourney.util.R;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xhy
 * @since 2023-10-24
 */
public interface VideoService extends IService<Video> {

    R postVideo(Video video);
}
