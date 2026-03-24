import { useState, useEffect, useCallback, useRef } from 'react';

export interface UseWebRTCOptions {
  video?: boolean | MediaTrackConstraints;
  audio?: boolean | MediaTrackConstraints;
  videoDeviceId?: string;
  audioDeviceId?: string;
  qualityPreset?: 'sd' | 'hd' | 'fhd';
  onStreamReady?: (stream: MediaStream) => void;
  onStreamError?: (error: Error) => void;
  onDeviceDisconnected?: () => void;
}

export interface UseWebRTCReturn {
  stream: MediaStream | null;
  error: string | null;
  isRequesting: boolean;
  hasPermission: boolean;
  startCapture: () => Promise<void>;
  stopCapture: () => void;
  switchCamera: () => Promise<void>;
}

export function useWebRTC(options: UseWebRTCOptions = {}): UseWebRTCReturn {
  const {
    video,
    audio = true,
    videoDeviceId,
    audioDeviceId,
    qualityPreset = 'hd',
    onStreamReady,
    onStreamError,
    onDeviceDisconnected,
  } = options;

  const qualityConstraints: Record<string, MediaTrackConstraints> = {
    sd: { width: { exact: 640 }, height: { exact: 480 }, frameRate: { ideal: 30 } },
    hd: { width: { exact: 1280 }, height: { exact: 720 }, frameRate: { ideal: 30 } },
    fhd: { width: { exact: 1920 }, height: { exact: 1080 }, frameRate: { ideal: 30 } },
  };

  const baseVideoConstraints = typeof video === 'object' ? video : {};
  const videoConstraints: MediaTrackConstraints = {
    ...qualityConstraints[qualityPreset],
    ...baseVideoConstraints,
    deviceId: videoDeviceId ? { exact: videoDeviceId } : undefined,
  };

  const audioConstraints: MediaTrackConstraints = {
    deviceId: audioDeviceId ? { exact: audioDeviceId } : undefined,
    ...(typeof audio === 'object' ? audio : {}),
  };

  const [stream, setStream] = useState<MediaStream | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isRequesting, setIsRequesting] = useState(false);
  const [hasPermission, setHasPermission] = useState(false);
  
  const streamRef = useRef<MediaStream | null>(null);

  const stopCurrentStream = useCallback(() => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach(track => track.stop());
      streamRef.current = null;
      setStream(null);
    }
  }, []);

  const startCapture = useCallback(async () => {
    setIsRequesting(true);
    setError(null);

    try {
      const mediaStream = await navigator.mediaDevices.getUserMedia({
        video: videoConstraints,
        audio: audioConstraints,
      });

      streamRef.current = mediaStream;
      setStream(mediaStream);
      setHasPermission(true);
      onStreamReady?.(mediaStream);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '无法访问摄像头';
      
      let friendlyError: string;
      if (errorMessage.includes('NotAllowedError') || errorMessage.includes('Permission denied')) {
        friendlyError = '摄像头权限被拒绝，请在浏览器设置中允许摄像头访问';
      } else if (errorMessage.includes('NotFoundError') || errorMessage.includes('DevicesNotFoundError')) {
        friendlyError = '未找到摄像头设备，请检查是否已连接摄像头';
      } else if (errorMessage.includes('NotReadableError')) {
        friendlyError = '摄像头正在被其他应用使用，请关闭其他应用后重试';
      } else {
        friendlyError = `摄像头访问失败：${errorMessage}`;
      }

      setError(friendlyError);
      setHasPermission(false);
      onStreamError?.(new Error(friendlyError));
    } finally {
      setIsRequesting(false);
    }
  }, [videoConstraints, audioConstraints, onStreamReady, onStreamError]);

  const stopCapture = useCallback(() => {
    stopCurrentStream();
    setHasPermission(false);
  }, [stopCurrentStream]);

  const switchCamera = useCallback(async () => {
    if (!streamRef.current) {
      return;
    }

    const currentVideoTrack = streamRef.current.getVideoTracks()[0];
    if (!currentVideoTrack) {
      return;
    }

    const currentFacingMode = currentVideoTrack.getSettings().facingMode;
    const newFacingMode = currentFacingMode === 'user' ? 'environment' : 'user';

    try {
      const newStream = await navigator.mediaDevices.getUserMedia({
        video: { 
          ...typeof video === 'object' ? video : {},
          facingMode: newFacingMode 
        },
        audio,
      });

      stopCurrentStream();
      
      streamRef.current = newStream;
      setStream(newStream);
      onStreamReady?.(newStream);
    } catch (err) {
      setError('切换摄像头失败');
      onStreamError?.(err instanceof Error ? err : new Error('切换摄像头失败'));
    }
  }, [streamRef.current, video, audio, stopCurrentStream, onStreamReady, onStreamError]);

  useEffect(() => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach(track => {
        track.onended = () => {
          onDeviceDisconnected?.();
          setError('设备已断开');
        };
      });
    }

    return () => {
      stopCurrentStream();
    };
  }, [streamRef.current, stopCurrentStream, onDeviceDisconnected]);

  return {
    stream,
    error,
    isRequesting,
    hasPermission,
    startCapture,
    stopCapture,
    switchCamera,
  };
}
