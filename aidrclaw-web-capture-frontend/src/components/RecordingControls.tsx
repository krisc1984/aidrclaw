import React, { useState, useRef, useCallback } from 'react';

export interface RecordingControlsProps {
  stream: MediaStream | null;
  onRecordingStart?: () => void;
  onRecordingStop?: (blob: Blob) => void;
  onUploadProgress?: (progress: number) => void;
  uploadUrl?: string;
  className?: string;
}

export const RecordingControls: React.FC<RecordingControlsProps> = ({
  stream,
  onRecordingStart,
  onRecordingStop,
  onUploadProgress,
  uploadUrl = '/api/capture/web/upload',
  className = '',
}) => {
  const [isRecording, setIsRecording] = useState(false);
  const [recordingTime, setRecordingTime] = useState(0);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);

  const mediaRecorderRef = useRef<MediaRecorder | null>(null);
  const chunksRef = useRef<Blob[]>([]);
  const timerRef = useRef<NodeJS.Timeout | null>(null);

  const formatTime = useCallback((seconds: number): string => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  }, []);

  const startRecording = useCallback(() => {
    if (!stream) {
      console.error('No stream available');
      return;
    }

    try {
      const mediaRecorder = new MediaRecorder(stream, {
        mimeType: 'video/webm;codecs=vp9',
      });

      chunksRef.current = [];

      mediaRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          chunksRef.current.push(event.data);
        }
      };

      mediaRecorder.onstop = async () => {
        const blob = new Blob(chunksRef.current, { type: 'video/webm' });
        chunksRef.current = [];
        
        onRecordingStop?.(blob);
        
        await uploadRecording(blob);
      };

      mediaRecorder.start(1000);
      mediaRecorderRef.current = mediaRecorder;
      setIsRecording(true);
      setRecordingTime(0);
      onRecordingStart?.();

      timerRef.current = setInterval(() => {
        setRecordingTime(prev => prev + 1);
      }, 1000);

    } catch (error) {
      console.error('Failed to start recording:', error);
    }
  }, [stream, onRecordingStop, onRecordingStart, uploadUrl]);

  const stopRecording = useCallback(() => {
    if (mediaRecorderRef.current && isRecording) {
      mediaRecorderRef.current.stop();
      setIsRecording(false);
      
      if (timerRef.current) {
        clearInterval(timerRef.current);
        timerRef.current = null;
      }
    }
  }, [isRecording]);

  const uploadRecording = async (blob: Blob) => {
    setIsUploading(true);
    setUploadProgress(0);

    try {
      const formData = new FormData();
      formData.append('file', blob, `recording-${Date.now()}.webm`);

      const xhr = new XMLHttpRequest();
      
      xhr.upload.addEventListener('progress', (event) => {
        if (event.lengthComputable) {
          const progress = Math.round((event.loaded / event.total) * 100);
          setUploadProgress(progress);
          onUploadProgress?.(progress);
        }
      });

      xhr.upload.addEventListener('load', () => {
        setIsUploading(false);
        setUploadProgress(100);
      });

      xhr.upload.addEventListener('error', () => {
        setIsUploading(false);
        console.error('Upload failed');
      });

      xhr.open('POST', uploadUrl);
      xhr.send(formData);

    } catch (error) {
      console.error('Upload error:', error);
      setIsUploading(false);
    }
  };

  const handleToggleRecording = () => {
    if (isRecording) {
      stopRecording();
    } else {
      startRecording();
    }
  };

  return (
    <div className={`recording-controls ${className}`}>
      <div className="recording-status">
        {isRecording && (
          <>
            <span className="recording-indicator">●</span>
            <span className="recording-timer">{formatTime(recordingTime)}</span>
          </>
        )}
        {isUploading && (
          <span className="uploading-status">
            上传中... {uploadProgress}%
          </span>
        )}
      </div>

      <button
        className={`record-button ${isRecording ? 'recording' : ''}`}
        onClick={handleToggleRecording}
        disabled={!stream || isUploading}
      >
        {isRecording ? '停止录制' : '开始录制'}
      </button>
    </div>
  );
};
