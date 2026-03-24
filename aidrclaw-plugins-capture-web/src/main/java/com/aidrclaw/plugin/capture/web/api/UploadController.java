package com.aidrclaw.plugin.capture.web.api;

import com.aidrclaw.plugin.PluginContext;
import com.aidrclaw.plugin.PluginResult;
import com.aidrclaw.plugin.capture.web.WebCapturePlugin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/capture/web")
@RequiredArgsConstructor
public class UploadController {

    private final WebCapturePlugin webCapturePlugin;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file) {
        
        log.info("接收到文件上传请求，文件名：{}, 大小：{} bytes", 
                file.getOriginalFilename(), file.getSize());

        try {
            PluginContext context = new PluginContext();
            Map<String, Object> input = new HashMap<>();
            input.put("file", file);
            context.getInput().putAll(input);

            PluginResult result = webCapturePlugin.execute(context);

            if (result.isSuccess()) {
                return ResponseEntity.ok(result.getData());
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", result.getErrorMessage()
                ));
            }

        } catch (Exception e) {
            log.error("文件上传处理异常", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "文件处理失败：" + e.getMessage()
            ));
        }
    }

    @PostMapping("/test")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "ok",
            "message", "Web 采集服务运行正常"
        ));
    }
}
