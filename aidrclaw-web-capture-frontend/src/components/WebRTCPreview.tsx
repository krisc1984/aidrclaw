import React, { useRef, useEffect } from 'react';
import { useWebRTC, UseWebRTCOptions } from '../hooks/useWebRTC';

export interface WebRTCPreviewProps extends UseWebRTCOptions {
  onStreamReady?: (stream: MediaStream) => void;
  onStreamError?: (error: Error) => void;
  className?: string;
  videoRef?: React.RefObject<HTMLVideoElement>;
}

export const WebRTCPreview: React.FC<WebRTCPreviewProps> = ({
  onStreamReady,
  onStreamError,
  className = '',
  videoRef,
  ...options
}) => {
  const localVideoRef = useRef<HTMLVideoElement>(null);
  const resolvedVideoRef = videoRef || localVideoRef;

  const {
    stream,
    error,
    isRequesting,
    hasPermission,
    startCapture,
    stopCapture,
  } = useWebRTC({
    ...options,
    onStreamReady,
    onStreamError,
  });

  useEffect(() => {
    if (resolvedVideoRef.current && stream) {
      resolvedVideoRef.current.srcObject = stream;
    }
  }, [stream, resolvedVideoRef]);

  const handleStartClick = async () => {
    await startCapture();
  };

  const handleStopClick = () => {
    stopCapture();
  };

  if (error) {
    return (
      <div className={`webrtc-preview error ${className}`}>
        <div className="error-message">
          <span role="img" aria-label="error">⚠️</span>
          {error}
        </div>
        <button onClick={handleStartClick}>重新尝试</button>
      </div>
    );
  }

  if (isRequesting) {
    return (
      <div className={`webrtc-preview requesting ${className}`}>
        <div className="loading-message">
          <span className="spinner"></span>
          正在请求摄像头权限...
        </div>
      </div>
    );
  }

  if (!hasPermission) {
    return (
      <div className={`webrtc-preview no-permission ${className}`}>
        <div className="preview-placeholder">
          <span role="img" aria-label="camera">📷</span>
          <p>摄像头未启动</p>
          <button onClick={handleStartClick}>开始摄像头</button>
        </div>
      </div>
    );
  }

  return (
    <div className={`webrtc-preview active ${className}`}>
      <video
        ref={resolvedVideoRef}
        autoPlay
        playsInline
        muted
        className="webrtc-video"
      />
      <div className="preview-controls">
        <button onClick={handleStopClick}>停止摄像头</button>
      </div>
    </div>
  );
};
