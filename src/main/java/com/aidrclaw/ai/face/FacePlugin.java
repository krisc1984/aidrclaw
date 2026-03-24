package com.aidrclaw.ai.face;

import com.aidrclaw.plugin.Plugin;
import com.aidrclaw.plugin.PluginContext;
import com.aidrclaw.plugin.PluginMetadata;
import com.aidrclaw.plugin.PluginResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class FacePlugin implements Plugin {

    private static final String PLUGIN_ID = "face";
    private static final String PLUGIN_VERSION = "1.0.0";
    private static final String PLUGIN_NAME = "人脸比对插件";
    private static final String PLUGIN_DESCRIPTION = "基于 InsightFace 的人脸比对和活体检测插件";

    private String faceServiceUrl;
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    @Override
    public void init(PluginContext context) {
        this.faceServiceUrl = context.getConfigString("face.service.url");
        if (this.faceServiceUrl == null) {
            this.faceServiceUrl = "http://localhost:8001";
        }
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        log.info("FacePlugin 初始化完成，人脸服务地址：{}", faceServiceUrl);
    }

    @Override
    public PluginResult execute(PluginContext context) {
        String action = context.getInputString("action");

        if (action == null) {
            return PluginResult.error("缺少 action 参数");
        }

        return switch (action) {
            case "compare" -> compareFaces(context);
            case "liveness" -> checkLiveness(context);
            default -> PluginResult.error("未知操作：" + action);
        };
    }

    private PluginResult compareFaces(PluginContext context) {
        try {
            byte[] image1 = context.getInput("image1", byte[].class);
            byte[] image2 = context.getInput("image2", byte[].class);

            if (image1 == null || image2 == null) {
                return PluginResult.error("缺少 image1 或 image2 参数");
            }

            String image1Base64 = Base64.getEncoder().encodeToString(image1);
            String image2Base64 = Base64.getEncoder().encodeToString(image2);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("image1", image1Base64);
            requestBody.put("image2", image2Base64);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    faceServiceUrl + "/face/compare",
                    request,
                    String.class
            );

            if (response.getStatusCode().value() == 200) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                double similarity = jsonResponse.get("similarity").asDouble();
                boolean match = jsonResponse.get("match").asBoolean();
                double threshold = jsonResponse.get("threshold").asDouble();

                Map<String, Object> resultData = new HashMap<>();
                resultData.put("similarity", similarity);
                resultData.put("match", match);
                resultData.put("threshold", threshold);

                log.info("人脸比对完成：相似度={:.4f}, 匹配={}", similarity, match);
                return PluginResult.success(resultData);
            } else {
                return PluginResult.error("人脸服务返回错误状态：" + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("人脸比对失败", e);
            return PluginResult.error("人脸比对失败：" + e.getMessage());
        }
    }

    private PluginResult checkLiveness(PluginContext context) {
        try {
            byte[] image = context.getInput("image", byte[].class);
            List<String> actions = context.getInput("actions", List.class);

            if (image == null) {
                return PluginResult.error("缺少 image 参数");
            }

            String imageBase64 = Base64.getEncoder().encodeToString(image);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("image", imageBase64);
            if (actions != null) {
                requestBody.put("actions", actions);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    faceServiceUrl + "/face/liveness",
                    request,
                    String.class
            );

            if (response.getStatusCode().value() == 200) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                boolean passed = jsonResponse.get("passed").asBoolean();
                List<String> detectedActions = objectMapper.convertValue(
                    jsonResponse.get("detected_actions"), List.class);

                Map<String, Object> resultData = new HashMap<>();
                resultData.put("passed", passed);
                resultData.put("detectedActions", detectedActions);

                log.info("活体检测完成：通过={}, 检测动作={}", passed, detectedActions);
                return PluginResult.success(resultData);
            } else {
                return PluginResult.error("人脸服务返回错误状态：" + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("活体检测失败", e);
            return PluginResult.error("活体检测失败：" + e.getMessage());
        }
    }

    @Override
    public void destroy() {
        log.info("FacePlugin 销毁");
    }

    @Override
    public PluginMetadata getMetadata() {
        return new PluginMetadata() {
            @Override
            public String getPluginId() {
                return PLUGIN_ID;
            }

            @Override
            public String getVersion() {
                return PLUGIN_VERSION;
            }

            @Override
            public String getName() {
                return PLUGIN_NAME;
            }

            @Override
            public String getDescription() {
                return PLUGIN_DESCRIPTION;
            }
        };
    }
}
