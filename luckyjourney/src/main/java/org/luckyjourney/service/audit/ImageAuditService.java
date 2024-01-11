package org.luckyjourney.service.audit;

import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.util.StringMap;
import org.luckyjourney.constant.AuditStatus;
import org.luckyjourney.entity.Setting;
import org.luckyjourney.entity.json.*;
import org.luckyjourney.entity.response.AuditResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @description: 图片审核
 * @Author: Xhy
 * @CreateTime: 2023-11-04 15:48
 */
@Service
public class ImageAuditService extends AbstractAuditService<String,AuditResponse>{

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


    @Override
    public AuditResponse audit(String url) {
        AuditResponse auditResponse = new AuditResponse();
        auditResponse.setAuditStatus(AuditStatus.SUCCESS);

        if (!isNeedAudit()) {
            return auditResponse;
        }
        String body=null;
        if(!url.contains(QiNiuConfig.CNAME)) {
            String fileName = url;
            String encodedFileName = URLEncoder.encode(fileName, "utf-8").replace("+", "%20");
            String domainOfBucket = QiNiuConfig.CNAME;
            String publicUrl = String.format("%s/%s", domainOfBucket, encodedFileName);
            Auth auth = Auth.create(qiNiuConfig.getAccessKey(), qiNiuConfig.getSecretKey());
            long expireInSeconds = 3600;//1小时，可以自定义链接过期时间
            String finalUrl = auth.privateDownloadUrl(publicUrl, expireInSeconds);
            System.out.println(finalUrl);
            finalUrl = appendUUID(finalUrl);//竟然是拿来回源鉴权的
            body = imageBody.replace("${url}", finalUrl);
        } else {
            url = appendUUID(url);
            body = imageBody.replace("${url}", url);
        }

        String method = "POST";
        // 获取token
        final String token = qiNiuConfig.getToken(imageUlr, method, body, contentType);
        StringMap header = new StringMap();
        header.put("Host", "ai.qiniuapi.com");
        header.put("Authorization", token);
        header.put("Content-Type", contentType);
        Configuration cfg = new Configuration(Region.region2());
        final Client client = new Client(cfg);
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
            // 审核
            auditResponse = audit(auditRule, bodyJson);
            return auditResponse;
        } catch (Exception e) {
            auditResponse.setAuditStatus(AuditStatus.SUCCESS);
            e.printStackTrace();
        }
        return auditResponse;
    }
}
