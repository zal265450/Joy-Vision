package org.luckyjourney.service.audit;

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
import org.luckyjourney.service.SettingService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @description:  统一封装审核逻辑，并留给子类进行编排或者调用普通逻辑
 * @Author: Xhy
 * @CreateTime: 2023-11-03 12:05
 */
@Service
public abstract class AbstractAuditService<T> implements AuditService<T>, InitializingBean {

    @Autowired
    private QiNiuConfig qiNiuConfig;

    @Autowired
    private SettingService settingService;

    protected ThreadPoolExecutor executor;

    protected ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private int maximumPoolSize = 8;

    static final String contentType = "application/json";

    static String videoUrl = "http://ai.qiniuapi.com/v3/video/censor";
    static String videoBody = "{\n" +
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

    static String imageUlr = "http://ai.qiniuapi.com/v3/image/censor";
    static String imageBody = "{\n" +
            "    \"data\": {\n" +
            "        \"uri\": \"${url}\"\n" +
            "    },\n" +
            "    \"params\": {\n" +
            "        \"scenes\": [\n" +
            "            \"pulp\",\n" +
            "            \"terror\",\n" +
            "            \"politician\"\n" +
            "        ]\n" +
            "    }\n" +
            "}";;
    static String textUrl = "http://ai.qiniuapi.com/v3/text/censor"       ;
    static String textBody = "{\n" +
            "    \"data\": {\n" +
            "        \"text\": \"${text}\"\n" +
            "    },\n" +
            "    \"params\": {\n" +
            "        \"scenes\": [\n" +
            "            \"antispam\"\n" +
            "        ]\n" +
            "    }\n" +
            "}";

    // 审核视频
    public AuditResponse auditVideo(String url){
        String body = videoBody.replace("${url}", url);
        String method = "POST";
        // 获取token
        final String token = qiNiuConfig.getToken(videoUrl, method, body, contentType);
        StringMap header = new StringMap();
        header.put("Host", "ai.qiniuapi.com");
        header.put("Authorization", token);
        header.put("Content-Type", contentType);
        Configuration cfg = new Configuration(Region.region2());
        final Client client = new Client(cfg);
        AuditResponse auditResponse = new AuditResponse();
        try {
            Response response = client.post(videoUrl, body.getBytes(), header, contentType);
            final Map map = objectMapper.readValue(response.getInfo().split(" \n")[2], Map.class);
            final Object job = map.get("job");
            url = "http://ai.qiniuapi.com/v3/jobs/video/" + job.toString();
            method = "GET";
            header = new StringMap();
            header.put("Host", "ai.qiniuapi.com");
            header.put("Authorization", qiNiuConfig.getToken(url, method, null, null));
            while (true) {
                Response response1 = client.get(url, header);
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

    // 审核内容
    public AuditResponse auditText(String text){
        String body = textBody.replace("${text}", text);
        String method = "POST";
        String url = "http://ai.qiniuapi.com/v3/text/censor";
        // 获取token
        final String token = qiNiuConfig.getToken(textUrl, method, body, contentType);
        StringMap header = new StringMap();
        header.put("Host", "ai.qiniuapi.com");
        header.put("Authorization", token);
        header.put("Content-Type", contentType);
        Configuration cfg = new Configuration(Region.region2());
        final Client client = new Client(cfg);
        AuditResponse auditResponse = new AuditResponse();
        try {
            Response response = client.post(textUrl, body.getBytes(), header, contentType);

            final Map map = objectMapper.readValue(response.getInfo().split(" \n")[2], Map.class);
            final ResultChildJson result = objectMapper.convertValue(map.get("result"), ResultChildJson.class);
            auditResponse.setAuditStatus(AuditStatus.SUCCESS);
            // 文本审核直接审核suggestion
            if (!result.getSuggestion().equals("pass")) {
               auditResponse.setAuditStatus(AuditStatus.PASS);
                final List<DetailsJson> details = result.getScenes().getAntispam().getDetails();
                if (!ObjectUtils.isEmpty(details)) {
                    // 遍历找到有问题的
                    for (DetailsJson detail : details) {
                        if (!detail.getLabel().equals("normal")) {
                            auditResponse.setMsg(AuditMsgMap.getInfo(detail.getLabel()) + "\n");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return auditResponse;
    }


    // 审核图片
    public AuditResponse auditImage(String url){
        String body = imageBody.replace("${url}", url);
        String method = "POST";
        // 获取token
        final String token = qiNiuConfig.getToken(imageUlr, method, body, contentType);
        StringMap header = new StringMap();
        header.put("Host", "ai.qiniuapi.com");
        header.put("Authorization", token);
        header.put("Content-Type", contentType);
        Configuration cfg = new Configuration(Region.region2());
        final Client client = new Client(cfg);
        AuditResponse auditResponse = new AuditResponse();
        try {
            Response response = client.post(imageUlr, body.getBytes(), header, contentType);

            final Map map = objectMapper.readValue(response.getInfo().split(" \n")[2], Map.class);
            final ResultChildJson result = objectMapper.convertValue(map.get("result"), ResultChildJson.class);
            final BodyJson bodyJson = new BodyJson();
            final ResultJson resultJson = new ResultJson();
            resultJson.setResult(result);
            bodyJson.setResult(resultJson);
            final Setting setting = settingService.getById(1);
            final SettingScoreJson settingScoreRule = objectMapper.readValue(setting.getAuditPolicy(), SettingScoreJson.class);
            final List<ScoreJson> auditRule = Arrays.asList(settingScoreRule.getManualScore(), settingScoreRule.getPassScore(), settingScoreRule.getSuccessScore());
            auditResponse = audit(auditRule, bodyJson);
            return auditResponse;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return auditResponse;
    }


    protected AuditResponse audit(List<ScoreJson> scoreJsonList, BodyJson bodyJson) {
        AuditResponse audit = new AuditResponse();
        // 遍历的是通过,人工,失败的审核规则,我当前没有办法知道是什么状态
        for (ScoreJson scoreJson : scoreJsonList) {
            audit = audit(scoreJson, bodyJson);
            // 如果为true,说明违规，提前返回
            if (audit.getFlag()){
                audit.setAuditStatus(scoreJson.getAuditStatus());
                return audit;
            }
        }
        // 如果出来了说明审核的内容没分数 / 审核比例没调好(人员问题)
        // 比较suggest
        final ScenesJson scenes = bodyJson.getResult().getResult().getScenes();
        if (endCheck(scenes)){
            audit.setAuditStatus(AuditStatus.SUCCESS);
        }else {
            audit.setAuditStatus(AuditStatus.PASS);
            audit.setMsg("内容不合法");
        }
        return audit;
    }

    /**
     * 返回对应规则的信息
     *
     * @param types
     * @param minPolitician
     * @return
     */
    private AuditResponse getInfo(List<CutsJson> types, Double minPolitician, String key) {
        AuditResponse auditResponse = new AuditResponse();
        auditResponse.setFlag(false);
        String info = null;
        // 获取信息
        for (CutsJson type : types) {
            for (DetailsJson detail : type.getDetails()) {
                // 人工/PASS ? 交给七牛云状态，我只获取信息和offset
                if (detail.getScore() > minPolitician) {
                    // 如果违规,则填充额外信息
                    if (!detail.getLabel().equals("normal")) {
                        info = AuditMsgMap.getInfo(key);
                        auditResponse.setMsg(info);
                        auditResponse.setOffset(type.getOffset());
                        auditResponse.setFlag(true);
                    }
                }
            }
        }
        return auditResponse;
    }


    /**
     * 当前审核规则如果能匹配上也就是进入了if判断中,则需要获取违规信息
     * 如果走到末尾则说明没有匹配上
     * @param scoreJson
     * @param bodyJson
     * @return
     */
    private AuditResponse audit(ScoreJson scoreJson, BodyJson bodyJson) {

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
                if (response.getFlag()) {
                    auditResponse.setOffset(response.getOffset());
                    return auditResponse;
                }
            }
        }
        if (!ObjectUtils.isEmpty(bodyJson.getPulp())) {
            if (bodyJson.checkViolation(bodyJson.getPulp(),minPulp,maxPulp)) {
                final AuditResponse response = getInfo(bodyJson.getPulp(), minPulp, "normal");
                auditResponse.setMsg(response.getMsg());
                // 如果违规则提前返回
                if (response.getFlag()) {
                    auditResponse.setOffset(response.getOffset());
                    return auditResponse;
                }
            }
        }
        if (!ObjectUtils.isEmpty(bodyJson.getTerror())) {
            if (bodyJson.checkViolation(bodyJson.getTerror(),minTerror,maxTerror)) {
                final AuditResponse response = getInfo(bodyJson.getTerror(), minTerror, "normal");
                auditResponse.setMsg(response.getMsg());
                if (response.getFlag()) {
                    auditResponse.setOffset(response.getOffset());
                    return auditResponse;
                }
            }
        }
        auditResponse.setFlag(false);
        return auditResponse;
    }

    private boolean endCheck(ScenesJson scenes){
        final TypeJson terror = scenes.getTerror();
        final TypeJson politician = scenes.getPolitician();
        final TypeJson pulp = scenes.getPulp();
        if (terror.getSuggestion().equals("block") || politician.getSuggestion().equals("block") || pulp.getSuggestion().equals("block")) {
            return false;
        }
        return true;
    }


    public boolean getAuditQueueState(){
        return executor.getTaskCount() < maximumPoolSize;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        executor  = new ThreadPoolExecutor(5, maximumPoolSize, 60, TimeUnit.SECONDS, new ArrayBlockingQueue(1000));
    }
}
