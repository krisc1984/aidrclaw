import React from 'react';
import { useDevices, DeviceInfo } from '../hooks/useDevices';

export interface DeviceSelectorProps {
  onDeviceChange?: (videoId: string, audioId: string) => void;
  className?: string;
}

export const DeviceSelector: React.FC<DeviceSelectorProps> = ({
  onDeviceChange,
  className = '',
}) => {
  const {
    videoDevices,
    audioDevices,
    selectedVideoId,
    selectedAudioId,
    selectVideo,
    selectAudio,
    isDeviceDisconnected,
  } = useDevices();

  const handleVideoChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const deviceId = e.target.value;
    selectVideo(deviceId);
    onDeviceChange?.(deviceId, selectedAudioId || '');
  };

  const handleAudioChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const deviceId = e.target.value;
    selectAudio(deviceId);
    onDeviceChange?.(selectedVideoId || '', deviceId);
  };

  return (
    <div className={`device-selector ${className}`}>
      <div className="device-selector-group">
        <label htmlFor="video-select" className="device-label">
          📹 摄像头
        </label>
        <select
          id="video-select"
          value={selectedVideoId || ''}
          onChange={handleVideoChange}
          className="device-select"
        >
          {videoDevices.map(device => (
            <option key={device.deviceId} value={device.deviceId}>
              {device.label}
            </option>
          ))}
        </select>
      </div>

      <div className="device-selector-group">
        <label htmlFor="audio-select" className="device-label">
          🎤 麦克风
        </label>
        <select
          id="audio-select"
          value={selectedAudioId || ''}
          onChange={handleAudioChange}
          className="device-select"
        >
          {audioDevices.map(device => (
            <option key={device.deviceId} value={device.deviceId}>
              {device.label}
            </option>
          ))}
        </select>
      </div>

      {isDeviceDisconnected && (
        <div className="device-disconnected-alert">
          ⚠️ 设备已断开，请重新选择
        </div>
      )}
    </div>
  );
};
