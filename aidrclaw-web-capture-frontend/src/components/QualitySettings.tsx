import React, { useState } from 'react';

export type QualityPreset = 'sd' | 'hd' | 'fhd';

export interface QualityConfig {
  preset: QualityPreset;
  resolution: string;
  width: number;
  height: number;
  frameRate: number;
  videoBitrate: number;
  audioSampleRate: number;
  audioBitrate: number;
}

export interface QualitySettingsProps {
  onQualityChange?: (config: QualityConfig) => void;
  className?: string;
}

const QUALITY_PRESETS: Record<QualityPreset, QualityConfig> = {
  sd: {
    preset: 'sd',
    resolution: '标清 (480p)',
    width: 640,
    height: 480,
    frameRate: 30,
    videoBitrate: 800000,
    audioSampleRate: 44100,
    audioBitrate: 64000,
  },
  hd: {
    preset: 'hd',
    resolution: '高清 (720p)',
    width: 1280,
    height: 720,
    frameRate: 30,
    videoBitrate: 1500000,
    audioSampleRate: 44100,
    audioBitrate: 64000,
  },
  fhd: {
    preset: 'fhd',
    resolution: '超清 (1080p)',
    width: 1920,
    height: 1080,
    frameRate: 30,
    videoBitrate: 3000000,
    audioSampleRate: 48000,
    audioBitrate: 128000,
  },
};

export const QualitySettings: React.FC<QualitySettingsProps> = ({
  onQualityChange,
  className = '',
}) => {
  const [selectedPreset, setSelectedPreset] = useState<QualityPreset>('hd');

  const currentConfig = QUALITY_PRESETS[selectedPreset];

  const handlePresetChange = (preset: QualityPreset) => {
    setSelectedPreset(preset);
    onQualityChange?.(QUALITY_PRESETS[preset]);
  };

  return (
    <div className={`quality-settings ${className}`}>
      <h3 className="quality-title">录制质量</h3>
      
      <div className="quality-presets">
        <label className="quality-option">
          <input
            type="radio"
            name="quality"
            value="sd"
            checked={selectedPreset === 'sd'}
            onChange={() => handlePresetChange('sd')}
          />
          <div className="quality-option-content">
            <div className="quality-option-name">标清</div>
            <div className="quality-option-detail">480p · 800kbps</div>
          </div>
        </label>

        <label className="quality-option">
          <input
            type="radio"
            name="quality"
            value="hd"
            checked={selectedPreset === 'hd'}
            onChange={() => handlePresetChange('hd')}
          />
          <div className="quality-option-content">
            <div className="quality-option-name">高清</div>
            <div className="quality-option-detail">720p · 1.5Mbps</div>
          </div>
        </label>

        <label className="quality-option">
          <input
            type="radio"
            name="quality"
            value="fhd"
            checked={selectedPreset === 'fhd'}
            onChange={() => handlePresetChange('fhd')}
          />
          <div className="quality-option-content">
            <div className="quality-option-name">超清</div>
            <div className="quality-option-detail">1080p · 3Mbps</div>
          </div>
        </label>
      </div>

      <div className="quality-details">
        <h4>当前配置详情</h4>
        <div className="quality-detail-grid">
          <div className="quality-detail-item">
            <span className="detail-label">分辨率</span>
            <span className="detail-value">{currentConfig.width}x{currentConfig.height}</span>
          </div>
          <div className="quality-detail-item">
            <span className="detail-label">帧率</span>
            <span className="detail-value">{currentConfig.frameRate} fps</span>
          </div>
          <div className="quality-detail-item">
            <span className="detail-label">视频码率</span>
            <span className="detail-value">{(currentConfig.videoBitrate / 1000).toFixed(1)} Mbps</span>
          </div>
          <div className="quality-detail-item">
            <span className="detail-label">音频采样率</span>
            <span className="detail-value">{currentConfig.audioSampleRate / 1000} kHz</span>
          </div>
          <div className="quality-detail-item">
            <span className="detail-label">音频码率</span>
            <span className="detail-value">{(currentConfig.audioBitrate / 1000).toFixed(0)} kbps</span>
          </div>
        </div>
      </div>
    </div>
  );
};
