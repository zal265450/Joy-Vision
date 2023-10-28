package org.luckyjourney.service.poll;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.util.StringMap;
import org.luckyjourney.config.QiNiuConfig;
import org.luckyjourney.constant.VideoStatus;
import org.luckyjourney.entity.Video;
import org.luckyjourney.entity.json.BodyJson;
import org.luckyjourney.service.video.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @description: 视频审核线程池
 * @Author: Xhy
 * @CreateTime: 2023-10-29 01:49
 */
@Service
public class VideoAuditThreadPoll implements ThreadPoll<Video>{

    ThreadPoolExecutor executor;

    ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    @Autowired
    private QiNiuConfig qiNiuConfig;

    @Autowired
    private VideoService videoService;


    String url = "http://ai.qiniuapi.com/v3/video/censor";
    String method = "POST";
    String contentType = "application/json";
    String body = "{\n" +
            "    \"data\": {\n" +
            "        \"uri\": \"${url}\",\n" +
            "        \"id\": \"video_censor_test\"\n" +
            "    },\n" +
            "    \"params\": {\n" +
            "        \"scenes\": [\n" +
            "            \"pulp\",\n" +
            "            \"terror\",\n" +
            "            \"politician\"\n" +
            "        ],\n" +
            "        \"cut_param\": {\n" +
            "            \"interval_msecs\": 5000\n" +
            "        }\n" +
            "    }\n" +
            "}";

    @Override
    public void submit(Video video) {
        executor.submit(()->{
            if (video.getAuditStatus()) {
                quickly(video);
            }else {
                slow(video);
            }
        });
    }

    // 快速
    public void quickly(Video video){
        new Thread(()->{
            slow(video);
        }).start();
    }
    // 慢速
    public void slow(Video video){
        body.replace("${url}",video.getUrl());
        // 获取token
        final String token = qiNiuConfig.getToken(url, method, body, contentType);
        StringMap header = new StringMap();
        header.put("Host", "ai.qiniuapi.com");
        header.put("Authorization", token);
        header.put("Content-Type", contentType);
        Configuration cfg = new Configuration(Region.region2());
        final Client client = new Client(cfg);
        try {
            Response response = client.post(url, body.getBytes(), header, contentType);
            final Map map = objectMapper.readValue(response.getInfo().split(" \n")[2], Map.class);
            final Object job = map.get("job");
            url = "http://ai.qiniuapi.com/v3/jobs/video/"+job.toString();
            method = "GET";
            header = new StringMap();
            header.put("Host","ai.qiniuapi.com");
            header.put("Authorization",qiNiuConfig.getToken(url,method,null,null));
            while (true) {
                Response response1 = client.get(url, header);
                final BodyJson bodyJson1 = objectMapper.readValue(response1.getInfo().split(" \n")[2], BodyJson.class);
                if (bodyJson1.getStatus().equals("FINISHED")) {
                    // TODO 后续更改为审核宽松度,根据比例放行,暂时先这样
                    if (bodyJson1.getResult().getResult().getSuggestion().equals("pass")){
                        // 审核通过
                        video.setStatus(VideoStatus.SUCCESS);
                    }else {
                        // 不予通过
                        video.setStatus(VideoStatus.PASS);
                    }
                    videoService.publishVideoHandler(video);
                    return;
                }
                Thread.sleep(2000L);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    // 用于初始化线程
    @Override
    public void afterPropertiesSet() throws Exception {
        executor  = new ThreadPoolExecutor(5, 8, 60, TimeUnit.SECONDS, new ArrayBlockingQueue(1000));
    }
}
