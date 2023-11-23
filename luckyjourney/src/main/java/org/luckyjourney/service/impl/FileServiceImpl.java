package org.luckyjourney.service.impl;

import com.qiniu.storage.model.FileInfo;
import org.checkerframework.checker.units.qual.A;
import org.luckyjourney.config.LocalCache;
import org.luckyjourney.config.QiNiuConfig;
import org.luckyjourney.entity.File;
import org.luckyjourney.entity.video.Video;
import org.luckyjourney.mapper.FileMapper;
import org.luckyjourney.service.FileService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.luckyjourney.service.QiNiuFileService;
import org.luckyjourney.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.UUID;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xhy
 * @since 2023-11-20
 */
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {

    @Autowired
    private QiNiuFileService qiNiuFileService;

    @Override
    public Long saveVideoFile(String url,Long userId) {
        final String uuid = UUID.randomUUID().toString();
        final File videoFile = new File();
        try {
            final FileInfo videoFileInfo = qiNiuFileService.getFileInfo(url);
            LocalCache.put(uuid,true);
            final String duration = FileUtil.getVideoDuration(QiNiuConfig.CNAME+"/"+url+"?uuid="+uuid);
            videoFile.setFileKey(url);
            videoFile.setFormat(videoFileInfo.mimeType);
            videoFile.setDuration(duration);
            videoFile.setType("视频");
            videoFile.setUserId(userId);
            videoFile.setSize(videoFileInfo.fsize);
            save(videoFile);
        }finally {
            LocalCache.rem(uuid);
        }

        return videoFile.getId();
    }



    @Override
    public Long savePhotoFile(String url, Long userId) {
        FileInfo videoFileInfo = qiNiuFileService.getFileInfo(url);

        final File videoFile = new File();
        videoFile.setFileKey(url);
        videoFile.setFormat(ObjectUtils.isEmpty(videoFileInfo.mimeType) ? "image/jpeg" : videoFileInfo.mimeType);
        videoFile.setType("图片");
        videoFile.setUserId(userId);
        videoFile.setSize(videoFileInfo.fsize);
        save(videoFile);
        return videoFile.getId();
    }
}
