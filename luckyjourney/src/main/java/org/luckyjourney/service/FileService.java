package org.luckyjourney.service;

import org.luckyjourney.util.R;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;

/**
 * @author xhy
 */
public interface FileService {


    /**
     * 获取签名
     * @return
     */
    String getToken();

    /**
     * 上传文件
     * @param file
     */
    void uploadFile(File file);

    /**
     * 删除文件
     * @param url
     */
    void deleteFile(String url);
}
