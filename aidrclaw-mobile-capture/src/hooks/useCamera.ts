import { useState, useEffect, useCallback, useRef } from 'react';
import { Platform, PermissionsAndroid, Alert, AppState, AppStateStatus } from 'react-native';
import { mediaDevices, MediaStream, RTCView } from 'react-native-webrtc';
import { check, request, PERMISSIONS, RESULTS, PermissionStatus } from 'react-native-permissions';

export interface UseCameraOptions {
  facingMode?: 'user' | 'environment';
  videoConstraints?: MediaTrackConstraints;
  audioConstraints?: boolean;
}

export interface UseCameraReturn {
  stream: MediaStream | null;
  hasPermission: boolean;
  error: string | null;
  isRequesting: boolean;
  facingMode: 'user' | 'environment';
  startCapture: () => Promise<void>;
  stopCapture: () => void;
  toggleCamera: () => Promise<void>;
  toggleMute: () => void;
  isMuted: boolean;
}

export function useCamera(options: UseCameraOptions = {}): UseCameraReturn {
  const {
    facingMode: initialFacingMode = 'user',
    videoConstraints,
    audioConstraints = true,
  } = options;

  const [stream, setStream] = useState<MediaStream | null>(null);
  const [hasPermission, setHasPermission] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isRequesting, setIsRequesting] = useState(false);
  const [facingMode, setFacingMode] = useState<'user' | 'environment'>(initialFacingMode);
  const [isMuted, setIsMuted] = useState(false);

  const streamRef = useRef<MediaStream | null>(null);
  const appStateRef = useRef<AppStateStatus>('active');
  const previousStateRef = useRef<AppStateStatus>('active');

  const stopCurrentStream = useCallback(() => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach(track => track.stop());
      streamRef.current = null;
      setStream(null);
    }
  }, []);

  const requestPermissions = useCallback(async (): Promise<boolean> => {
    try {
      if (Platform.OS === 'android') {
        const cameraGranted = await PermissionsAndroid.check(
          PermissionsAndroid.PERMISSIONS.CAMERA
        );
        const audioGranted = await PermissionsAndroid.check(
          PermissionsAndroid.PERMISSIONS.RECORD_AUDIO
        );

        if (!cameraGranted || !audioGranted) {
          const cameraResult = await PermissionsAndroid.request(
            PermissionsAndroid.PERMISSIONS.CAMERA,
            {
              title: '摄像头权限',
              message: '需要使用摄像头进行录制',
              buttonPositive: '允许',
              buttonNegative: '拒绝',
            }
          );

          const audioResult = await PermissionsAndroid.request(
            PermissionsAndroid.PERMISSIONS.RECORD_AUDIO,
            {
              title: '麦克风权限',
              message: '需要使用麦克风录制音频',
              buttonPositive: '允许',
              buttonNegative: '拒绝',
            }
          );

          return cameraResult === PermissionsAndroid.RESULTS.GRANTED &&
                 audioResult === PermissionsAndroid.RESULTS.GRANTED;
        }

        return true;
      } else {
        const cameraStatus = await check(PERMISSIONS.IOS.CAMERA);
        const micStatus = await check(PERMISSIONS.IOS.MICROPHONE);

        if (cameraStatus === RESULTS.GRANTED && micStatus === RESULTS.GRANTED) {
          return true;
        }

        if (cameraStatus === RESULTS.DENIED || micStatus === RESULTS.DENIED) {
          const [cameraResult, micResult] = await Promise.all([
            request(PERMISSIONS.IOS.CAMERA),
            request(PERMISSIONS.IOS.MICROPHONE),
          ]);

          return cameraResult === RESULTS.GRANTED && micResult === RESULTS.GRANTED;
        }

        return false;
      }
    } catch (err) {
      console.error('Permission request error:', err);
      return false;
    }
  }, []);

  const getFriendlyErrorMessage = (err: any): string => {
    const errorMessage = err instanceof Error ? err.message : String(err);
    
    if (errorMessage.includes('Permission')) {
      return '摄像头权限被拒绝，请在设置中允许摄像头和麦克风访问';
    } else if (errorMessage.includes('not found') || errorMessage.includes(' unavailable')) {
      return '未找到可用摄像头，请检查设备';
    } else if (errorMessage.includes('in use')) {
      return '摄像头正在被其他应用使用，请关闭后重试';
    } else {
      return `摄像头启动失败：${errorMessage}`;
    }
  };

  const startCapture = useCallback(async () => {
    setIsRequesting(true);
    setError(null);

    try {
      const granted = await requestPermissions();
      
      if (!granted) {
        setError('权限被拒绝，无法使用摄像头');
        setHasPermission(false);
        return;
      }

      const constraints: MediaStreamConstraints = {
        video: {
          facingMode,
          width: { ideal: 1280 },
          height: { ideal: 720 },
          frameRate: { ideal: 30 },
          ...videoConstraints,
        },
        audio: audioConstraints,
      };

      const mediaStream = await mediaDevices.getUserMedia(constraints);

      streamRef.current = mediaStream;
      setStream(mediaStream);
      setHasPermission(true);
      setIsMuted(false);

    } catch (err) {
      const friendlyError = getFriendlyErrorMessage(err);
      setError(friendlyError);
      setHasPermission(false);
      console.error('getUserMedia error:', err);
    } finally {
      setIsRequesting(false);
    }
  }, [facingMode, videoConstraints, audioConstraints, requestPermissions]);

  const stopCapture = useCallback(() => {
    stopCurrentStream();
    setHasPermission(false);
    setError(null);
  }, [stopCurrentStream]);

  const toggleCamera = useCallback(async () => {
    if (!streamRef.current) return;

    const newFacingMode = facingMode === 'user' ? 'environment' : 'user';

    try {
      const constraints: MediaStreamConstraints = {
        video: {
          facingMode: newFacingMode,
          width: { ideal: 1280 },
          height: { ideal: 720 },
          frameRate: { ideal: 30 },
        },
        audio: audioConstraints,
      };

      const newStream = await mediaDevices.getUserMedia(constraints);

      stopCurrentStream();

      streamRef.current = newStream;
      setStream(newStream);
      setFacingMode(newFacingMode);
    } catch (err) {
      setError('切换摄像头失败');
      console.error('toggleCamera error:', err);
    }
  }, [facingMode, audioConstraints, stopCurrentStream]);

  const toggleMute = useCallback(() => {
    if (!streamRef.current) return;

    const audioTracks = streamRef.current.getAudioTracks();
    audioTracks.forEach(track => {
      track.enabled = !track.enabled;
    });

    setIsMuted(prev => !prev);
  }, []);

  useEffect(() => {
    const handleAppStateChange = (nextAppState: AppStateStatus) => {
      if (
        previousStateRef.current.match(/inactive|background/) &&
        nextAppState === 'active'
      ) {
        console.log('App returned to foreground');
      } else if (
        previousStateRef.current === 'active' &&
        nextAppState.match(/inactive|background/)
      ) {
        console.log('App went to background');
      }

      previousStateRef.current = nextAppState;
      appStateRef.current = nextAppState;
    };

    const subscription = AppState.addEventListener('change', handleAppStateChange);

    return () => {
      subscription.remove();
      stopCurrentStream();
    };
  }, [stopCurrentStream]);

  return {
    stream,
    hasPermission,
    error,
    isRequesting,
    facingMode,
    startCapture,
    stopCapture,
    toggleCamera,
    toggleMute,
    isMuted,
  };
}
