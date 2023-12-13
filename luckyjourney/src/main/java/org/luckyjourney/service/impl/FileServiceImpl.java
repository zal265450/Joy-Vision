package org.luckyjourney.service.impl;

import com.qiniu.storage.model.FileInfo;
import org.checkerframework.checker.units.qual.A;
import org.luckyjourney.config.LocalCache;
import org.luckyjourney.config.QiNiuConfig;
import org.luckyjourney.entity.File;
import org.luckyjourney.entity.video.Video;
import org.luckyjourney.holder.UserHolder;
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
    public Long save(String fileKey,Long userId) {

        // 判断文件
        final FileInfo videoFileInfo = qiNiuFileService.getFileInfo(fileKey);

        if (videoFileInfo == null){
            throw new IllegalArgumentException("参数不正确");
        }

        final File videoFile = new File();
        String type = videoFileInfo.mimeType;
        videoFile.setFileKey(fileKey);
        videoFile.setFormat(type);
        videoFile.setType(type.contains("video") ? "视频" : "图片");
        videoFile.setUserId(userId);
        videoFile.setSize(videoFileInfo.fsize);
        save(videoFile);

        return videoFile.getId();
    }

    @Override
    public Long generatePhoto(Long fileId,Long userId) {
        final File file = getById(fileId);;
        final String fileKey = file.getFileKey() + "?vframe/jpg/offset/1";
        final File fileInfo = new File();
        fileInfo.setFileKey(fileKey);
        file.setFormat("image/*");
        file.setType("图片");
        file.setUserId(userId);
        save(fileInfo);
        return fileInfo.getId();
    }
}
