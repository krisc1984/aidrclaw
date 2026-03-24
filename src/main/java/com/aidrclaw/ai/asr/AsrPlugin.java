package com.aidrclaw.ai.asr;

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
import java.util.Map;

@Slf4j
@Component
public class AsrPlugin implements Plugin {

    private static final String PLUGIN_ID = "asr";
    private static final String PLUGIN_VERSION = "1.0.0";
    private static final String PLUGIN_NAME = "语音识别插件";
    private static final String PLUGIN_DESCRIPTION = "基于 FunASR 的语音识别插件";

    private String asrServiceUrl;
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    @Override
    public void init(PluginContext context) {
        this.asrServiceUrl = context.getConfigString("asr.service.url");
        if (this.asrServiceUrl == null) {
            this.asrServiceUrl = "http://localhost:8000";
        }
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        log.info("AsrPlugin 初始化完成，ASR 服务地址：{}", asrServiceUrl);
    }

    @Override
    public PluginResult execute(PluginContext context) {
        byte[] audioData = context.getInput("audioData", byte[].class);
        Integer sampleRate = context.getInput("sampleRate", Integer.class);

        if (audioData == null) {
            return PluginResult.error("缺少 audioData 参数");
        }

        if (sampleRate == null) {
            sampleRate = 16000;
        }

        try {
            String audioBase64 = Base64.getEncoder().encodeToString(audioData);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("audio", audioBase64);
            requestBody.put("sample_rate", sampleRate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    asrServiceUrl + "/asr/recognize",
                    request,
                    String.class
            );

            if (response.getStatusCode().value() == 200) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                String text = jsonResponse.get("text").asText();
                double confidence = jsonResponse.get("confidence").asDouble();
                boolean success = jsonResponse.get("success").asBoolean();

                Map<String, Object> resultData = new HashMap<>();
                resultData.put("text", text);
                resultData.put("confidence", confidence);
                resultData.put("success", success);

                log.info("ASR 识别成功：{} (confidence: {})", text, confidence);
                return PluginResult.success(resultData);
            } else {
                return PluginResult.error("ASR 服务返回错误状态：" + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("ASR 识别失败", e);
            return PluginResult.error("ASR 识别失败：" + e.getMessage());
        }
    }

    @Override
    public void destroy() {
        log.info("AsrPlugin 销毁");
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
