package com.aidrclaw.ai.tts;

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
public class TtsPlugin implements Plugin {

    private static final String PLUGIN_ID = "tts";
    private static final String PLUGIN_VERSION = "1.0.0";
    private static final String PLUGIN_NAME = "语音合成插件";
    private static final String PLUGIN_DESCRIPTION = "基于 VITS 的语音合成插件";

    private String ttsServiceUrl;
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    @Override
    public void init(PluginContext context) {
        this.ttsServiceUrl = context.getConfigString("tts.service.url");
        if (this.ttsServiceUrl == null) {
            this.ttsServiceUrl = "http://localhost:8002";
        }
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        log.info("TtsPlugin 初始化完成，TTS 服务地址：{}", ttsServiceUrl);
    }

    @Override
    public PluginResult execute(PluginContext context) {
        String text = context.getInputString("text");
        String speaker = context.getInputString("speaker");
        Float speed = context.getInput("speed", Float.class);

        if (text == null || text.isEmpty()) {
            return PluginResult.error("缺少 text 参数");
        }

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("text", text);
            if (speaker != null) {
                requestBody.put("speaker", speaker);
            }
            if (speed != null) {
                requestBody.put("speed", speed);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    ttsServiceUrl + "/tts/synthesize",
                    request,
                    String.class
            );

            if (response.getStatusCode().value() == 200) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                String audioBase64 = jsonResponse.get("audio").asText();
                boolean success = jsonResponse.get("success").asBoolean();
                double duration = jsonResponse.get("duration").asDouble();

                byte[] audioData = Base64.getDecoder().decode(audioBase64);

                Map<String, Object> resultData = new HashMap<>();
                resultData.put("audioData", audioData);
                resultData.put("duration", duration);
                resultData.put("success", success);

                log.info("TTS 合成完成：时长={:.2f}s", duration);
                return PluginResult.success(resultData);
            } else {
                return PluginResult.error("TTS 服务返回错误状态：" + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("TTS 合成失败", e);
            return PluginResult.error("TTS 合成失败：" + e.getMessage());
        }
    }

    @Override
    public void destroy() {
        log.info("TtsPlugin 销毁");
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
