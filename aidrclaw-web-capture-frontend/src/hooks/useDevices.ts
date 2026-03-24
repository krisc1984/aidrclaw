import { useState, useEffect, useCallback } from 'react';

export interface DeviceInfo {
  deviceId: string;
  kind: 'videoinput' | 'audioinput';
  label: string;
  groupId?: string;
}

export interface UseDevicesReturn {
  videoDevices: DeviceInfo[];
  audioDevices: DeviceInfo[];
  selectedVideoId: string | null;
  selectedAudioId: string | null;
  selectVideo: (deviceId: string) => void;
  selectAudio: (deviceId: string) => void;
  isDeviceDisconnected: boolean;
  refreshDevices: () => Promise<void>;
}

export function useDevices(): UseDevicesReturn {
  const [videoDevices, setVideoDevices] = useState<DeviceInfo[]>([]);
  const [audioDevices, setAudioDevices] = useState<DeviceInfo[]>([]);
  const [selectedVideoId, setSelectedVideoId] = useState<string | null>(null);
  const [selectedAudioId, setSelectedAudioId] = useState<string | null>(null);
  const [isDeviceDisconnected, setIsDeviceDisconnected] = useState(false);

  const enumerateDevices = useCallback(async () => {
    try {
      const devices = await navigator.mediaDevices.enumerateDevices();
      
      const videos = devices
        .filter(device => device.kind === 'videoinput')
        .map(device => ({
          deviceId: device.deviceId,
          kind: 'videoinput' as const,
          label: device.label || `摄像头 ${videoDevices.length + 1}`,
          groupId: device.groupId,
        }));

      const audios = devices
        .filter(device => device.kind === 'audioinput')
        .map(device => ({
          deviceId: device.deviceId,
          kind: 'audioinput' as const,
          label: device.label || `麦克风 ${audioDevices.length + 1}`,
          groupId: device.groupId,
        }));

      setVideoDevices(videos);
      setAudioDevices(audios);

      if (!selectedVideoId && videos.length > 0) {
        setSelectedVideoId(videos[0].deviceId);
      }

      if (!selectedAudioId && audios.length > 0) {
        setSelectedAudioId(audios[0].deviceId);
      }

    } catch (error) {
      console.error('Enumerate devices error:', error);
    }
  }, [videoDevices.length, audioDevices.length, selectedVideoId, selectedAudioId]);

  const handleDeviceChange = useCallback(() => {
    enumerateDevices();
    
    if (selectedVideoId) {
      const stillExists = videoDevices.some(d => d.deviceId === selectedVideoId);
      if (!stillExists) {
        setIsDeviceDisconnected(true);
      } else {
        setIsDeviceDisconnected(false);
      }
    }
  }, [enumerateDevices, selectedVideoId, videoDevices]);

  useEffect(() => {
    enumerateDevices();

    navigator.mediaDevices.ondevicechange = handleDeviceChange;

    return () => {
      navigator.mediaDevices.ondevicechange = null;
    };
  }, [enumerateDevices, handleDeviceChange]);

  const selectVideo = useCallback((deviceId: string) => {
    setSelectedVideoId(deviceId);
    setIsDeviceDisconnected(false);
  }, []);

  const selectAudio = useCallback((deviceId: string) => {
    setSelectedAudioId(deviceId);
    setIsDeviceDisconnected(false);
  }, []);

  const refreshDevices = useCallback(async () => {
    await enumerateDevices();
    setIsDeviceDisconnected(false);
  }, [enumerateDevices]);

  return {
    videoDevices,
    audioDevices,
    selectedVideoId,
    selectedAudioId,
    selectVideo,
    selectAudio,
    isDeviceDisconnected,
    refreshDevices,
  };
}
