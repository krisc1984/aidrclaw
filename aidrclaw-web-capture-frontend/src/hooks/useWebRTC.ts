import { useState, useEffect, useCallback, useRef } from 'react';

export interface UseWebRTCOptions {
  video?: boolean | MediaTrackConstraints;
  audio?: boolean | MediaTrackConstraints;
  onStreamReady?: (stream: MediaStream) => void;
  onStreamError?: (error: Error) => void;
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
    video = { facingMode: 'user', width: { ideal: 1280 }, height: { ideal: 720 } },
    audio = true,
    onStreamReady,
    onStreamError,
  } = options;

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
        video,
        audio,
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
  }, [video, audio, onStreamReady, onStreamError]);

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
    return () => {
      stopCurrentStream();
    };
  }, [stopCurrentStream]);

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
