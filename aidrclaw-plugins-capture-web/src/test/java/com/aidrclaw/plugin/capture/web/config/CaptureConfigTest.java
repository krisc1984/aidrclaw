package com.aidrclaw.plugin.capture.web.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CaptureConfigTest {

    @Autowired
    private CaptureConfig captureConfig;

    @Test
    void testContextLoads() {
        assertNotNull(captureConfig);
    }

    @Test
    void testDefaultVideoSettings() {
        assertEquals(1280, captureConfig.getVideoWidth());
        assertEquals(720, captureConfig.getVideoHeight());
        assertEquals(30, captureConfig.getVideoFrameRate());
        assertEquals(1500000, captureConfig.getVideoBitrate());
    }

    @Test
    void testDefaultAudioSettings() {
        assertEquals(44100, captureConfig.getAudioSampleRate());
        assertEquals(64000, captureConfig.getAudioBitrate());
    }

    @Test
    void testUploadSettings() {
        assertEquals("uploads/web-capture", captureConfig.getUploadDirectory());
        assertEquals(104857600, captureConfig.getMaxFileSize());
    }

    @Test
    void testVideoObject() {
        CaptureConfig.Video video = captureConfig.getVideo();
        assertNotNull(video);
        assertEquals(1280, video.getWidth());
        assertEquals(720, video.getHeight());
        assertEquals("vp9", video.getCodec());
    }

    @Test
    void testAudioObject() {
        CaptureConfig.Audio audio = captureConfig.getAudio();
        assertNotNull(audio);
        assertEquals(44100, audio.getSampleRate());
        assertEquals(1, audio.getChannels());
        assertEquals("opus", audio.getCodec());
    }
}
