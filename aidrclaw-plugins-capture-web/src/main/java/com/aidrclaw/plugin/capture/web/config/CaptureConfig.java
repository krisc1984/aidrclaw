package com.aidrclaw.plugin.capture.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "capture")
public class CaptureConfig {

    private Video video = new Video();
    private Audio audio = new Audio();
    private Upload upload = new Upload();

    @Data
    public static class Video {
        private int width = 1280;
        private int height = 720;
        private int frameRate = 30;
        private int bitrate = 1500000;
        private String codec = "vp9";
    }

    @Data
    public static class Audio {
        private int sampleRate = 44100;
        private int bitrate = 64000;
        private int channels = 1;
        private String codec = "opus";
    }

    @Data
    public static class Upload {
        private String directory = "uploads/web-capture";
        private long maxFileSize = 104857600;
        private String[] allowedTypes = new String[]{"video/webm", "video/mp4"};
    }

    public int getVideoWidth() {
        return video.getWidth();
    }

    public int getVideoHeight() {
        return video.getHeight();
    }

    public int getVideoFrameRate() {
        return video.getFrameRate();
    }

    public int getVideoBitrate() {
        return video.getBitrate();
    }

    public int getAudioSampleRate() {
        return audio.getSampleRate();
    }

    public int getAudioBitrate() {
        return audio.getBitrate();
    }

    public String getUploadDirectory() {
        return upload.getDirectory();
    }

    public long getMaxFileSize() {
        return upload.getMaxFileSize();
    }
}
