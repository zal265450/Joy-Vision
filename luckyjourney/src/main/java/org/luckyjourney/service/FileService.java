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

    /**
     * 保存文件信息
     * @param url
     * @param userId
     * @return 新增记录后的id
     */
    Long saveVideoFile(String url,Long userId);

    Long savePhotoFile(String url,Long userId);

}
