package org.luckyjourney.service;

import org.luckyjourney.entity.File;
import com.baomidou.mybatisplus.extension.service.IService;
import org.luckyjourney.entity.video.Video;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xhy
 * @since 2023-11-20
 */
public interface FileService extends IService<File> {


    Long save(String fileKey,Long userId);

    /**
     * 根据视频id生成图片
     * @param fileId
     * @return
     */
    Long generatePhoto(Long fileId,Long userId);
}
