package org.luckyjourney.service.video;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.util.StringMap;
import org.luckyjourney.config.QiNiuConfig;
import org.luckyjourney.constant.AuditMsgMap;
import org.luckyjourney.constant.AuditStatus;
import org.luckyjourney.entity.Setting;
import org.luckyjourney.entity.json.*;
import org.luckyjourney.entity.response.AuditResponse;
import org.luckyjourney.service.AuditService;
import org.luckyjourney.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @description: 视频审核
 * @Author: Xhy
 * @CreateTime: 2023-10-29 14:40
 */
@Service
public class VideoAuditServiceImpl implements AuditService {

    @Autowired
    private QiNiuConfig qiNiuConfig;

    @Autowired
    private SettingService settingService;


    ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    static String method = "POST";
    static final String contentType = "application/json";
    static String body = "{\n" +
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
    public AuditResponse audit(String url, boolean auditStatus) {
        AuditResponse auditResponse = null;
        if (auditStatus) {
            quickly(url);
        } else {
            auditResponse = slow(url);
        }
        return auditResponse;
    }

    public AuditResponse quickly(String url) {
        final CompletableFuture<AuditResponse> quickly = new CompletableFuture<>();
        quickly.supplyAsync(() -> slow(url));
        try {
            return quickly.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }


    // 慢速
    public AuditResponse slow(String url) {
        body = body.replace("${url}", url);
        String qiniuUrl = "http://ai.qiniuapi.com/v3/video/censor";
        // 获取token
        final String token = qiNiuConfig.getToken(qiniuUrl, method, body, contentType);
        StringMap header = new StringMap();
        header.put("Host", "ai.qiniuapi.com");
        header.put("Authorization", token);
        header.put("Content-Type", contentType);
        Configuration cfg = new Configuration(Region.region2());
        final Client client = new Client(cfg);
        AuditResponse auditResponse = new AuditResponse();
        try {

            Response response = client.post(qiniuUrl, body.getBytes(), header, contentType);

            final Map map = objectMapper.readValue(response.getInfo().split(" \n")[2], Map.class);
            final Object job = map.get("job");
            qiniuUrl = "http://ai.qiniuapi.com/v3/jobs/video/" + job.toString();
            method = "GET";
            header = new StringMap();
            header.put("Host", "ai.qiniuapi.com");
            header.put("Authorization", qiNiuConfig.getToken(qiniuUrl, method, null, null));
            while (true) {
                Response response1 = client.get(qiniuUrl, header);
                final BodyJson bodyJson = objectMapper.readValue(response1.getInfo().split(" \n")[2], BodyJson.class);
                if (bodyJson.getStatus().equals("FINISHED")) {
                    // 1.从系统配置表获取 pulp politician terror比例
                    final Setting setting = settingService.getById(1);
                    final SettingScoreJson settingScoreRule = objectMapper.readValue(setting.getAuditPolicy(), SettingScoreJson.class);
                    final List<ScoreJson> auditRule = Arrays.asList(settingScoreRule.getManualScore(), settingScoreRule.getPassScore(), settingScoreRule.getSuccessScore());
                    auditResponse = audit(auditRule, bodyJson);
                    return auditResponse;
                }
                Thread.sleep(2000L);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return auditResponse;
    }

    public AuditResponse audit(List<ScoreJson> scoreJsonList, BodyJson bodyJson) {
        AuditResponse audit = null;
        // 遍历的是通过,人工,失败的审核规则,我当前没有办法知道是什么状态
        for (ScoreJson scoreJson : scoreJsonList) {
            audit = audit(scoreJson, bodyJson);
            if (audit.getFlag()){
                audit.setAuditStatus(scoreJson.getAuditStatus());
                return audit;
            }
        }
        return audit;
    }

    /**
     * 当前审核规则如果能匹配上也就是进入了if判断中,则需要获取违规信息
     * 如果走到末尾则说明没有匹配上
     * @param scoreJson
     * @param bodyJson
     * @return
     */
    public AuditResponse audit(ScoreJson scoreJson, BodyJson bodyJson) {

        AuditResponse auditResponse = new AuditResponse();
        auditResponse.setFlag(true);
        auditResponse.setAuditStatus(scoreJson.getAuditStatus());

        final Double minPolitician = scoreJson.getMinPolitician();
        final Double maxPolitician = scoreJson.getMaxPolitician();
        final Double minPulp = scoreJson.getMinPulp();
        final Double maxPulp = scoreJson.getMaxPulp();
        final Double minTerror = scoreJson.getMinTerror();
        final Double maxTerror = scoreJson.getMaxTerror();

        // 所有都要比较,如果返回的有问题则直接返回
        if (!ObjectUtils.isEmpty(bodyJson.getPolitician())) {
            if (bodyJson.checkViolation(bodyJson.getPolitician(),minPolitician,maxPolitician)) {
                final AuditResponse response = getInfo(bodyJson.getPolitician(), minPolitician, "group");
                auditResponse.setMsg(response.getMsg());
                auditResponse.setOffset(response.getOffset());
                if (response.getFlag()) {
                    return auditResponse;
                }
            }
        }
        if (!ObjectUtils.isEmpty(bodyJson.getPulp())) {
            if (bodyJson.checkViolation(bodyJson.getPulp(),minPulp,maxPulp)) {
                final AuditResponse response = getInfo(bodyJson.getPulp(), minPulp, "normal");
                auditResponse.setMsg(response.getMsg() + "\n" + auditResponse.getMsg());
                auditResponse.setOffset(response.getOffset());
                if (response.getFlag()) {
                    return auditResponse;
                }
            }
        }
        if (!ObjectUtils.isEmpty(bodyJson.getTerror())) {
            if (bodyJson.checkViolation(bodyJson.getTerror(),minTerror,maxTerror)) {
                final AuditResponse response = getInfo(bodyJson.getTerror(), minTerror, "normal");
                auditResponse.setMsg(response.getMsg() + "\n" + auditResponse.getMsg());
                auditResponse.setOffset(response.getOffset());
                if (response.getFlag()) {
                    return auditResponse;
                }
            }
        }
        auditResponse.setFlag(false);
        return auditResponse;
    }

    /**
     * 返回对应规则的信息
     *
     * @param types
     * @param minPolitician
     * @return
     */
    public AuditResponse getInfo(List<CutsJson> types, Double minPolitician, String key) {
        AuditResponse auditResponse = new AuditResponse();
        auditResponse.setMsg("正常");
        String info = null;
        // 获取信息
        for (CutsJson type : types) {
            for (DetailsJson detail : type.getDetails()) {
                // 人工/PASS ? 交给七牛云状态，我只获取信息和offset
                if (detail.getScore() > minPolitician) {
                    if (!detail.getLabel().equals("normal")) {
                        info = AuditMsgMap.getInfo(detail.getLabel());
                        auditResponse.setMsg(info);
                        auditResponse.setOffset(type.getOffset());
                        auditResponse.setFlag(true);
                    }
                }
            }
        }
        return auditResponse;
    }


}
