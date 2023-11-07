package org.luckyjourney;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.junit.jupiter.api.Test;
import org.luckyjourney.config.QiNiuConfig;
import org.luckyjourney.entity.video.Video;
import org.luckyjourney.mapper.video.VideoMapper;
import org.luckyjourney.service.video.VideoService;
import org.luckyjourney.util.RedisCacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.Http2;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class LuckyJourneyApplicationTests {

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private RedisTemplate redisTemplate;

    @Resource
    private VideoMapper videoMapper;

    @Test
    void contextLoads() {

        List<Video> videos = videoMapper.selectList(null);
        List<Video> collect = videos.stream().map(e -> {
            e.setCover(e.getCover().replace(QiNiuConfig.CNAME, ""));
            videoMapper.updateById(e);
            return e;
        }).collect(Collectors.toList());



    }

    public static void test_file(){
        String accessKey = "SXJerOTcNdkqu4NUTR5tQszp_3pSumPPSTsLlHSJ";
        String secretKey = "aF3K8NyeBbhfjZJ5YRYxDcR9XM0ufKKHJfAtpdJM";
        String bucket = "lucky-journey-x";

        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);
        System.out.println(upToken);

        //构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.region2());
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;// 指定分片上传版本
//...其他参数参考类注释

        UploadManager uploadManager = new UploadManager(cfg);
//如果是Windows情况下，格式是 D:\\qiniu\\test.png
        String localFilePath = "D:\\桌面\\临时压缩文件地址\\c841c61e1ddfa3f171983445c48959e.jpg";
//默认不指定key的情况下，以文件内容的hash值作为文件名
        String key = null;

        try {
            Response response = uploadManager.put(localFilePath, key, upToken);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            System.out.println(putRet.key);
            System.out.println(putRet.hash);
            System.out.println(putRet);
        } catch (QiniuException ex) {
            ex.printStackTrace();
            if (ex.response != null) {
                System.err.println(ex.response);

                try {
                    String body = ex.response.toString();
                    System.err.println(body);
                } catch (Exception ignored) {
                }
            }
        }

    }
    public static void test_token(){
        String accessKey = "SXJerOTcNdkqu4NUTR5tQszp_3pSumPPSTsLlHSJ";
        String secretKey = "aF3K8NyeBbhfjZJ5YRYxDcR9XM0ufKKHJfAtpdJM";
        String bucket = "lucky-journey-x";

        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);
        System.out.println(upToken);
    }
    public static void main(String[] args) {
        test_file();

    }

}
