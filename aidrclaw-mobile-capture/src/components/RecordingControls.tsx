import React, { useState, useRef, useCallback, useEffect, Platform } from 'react';
import { View, Text, StyleSheet, ActivityIndicator, Alert } from 'react-native';
import { MediaStream, MediaRecorder } from 'react-native-webrtc';
import RNFS from 'react-native-fs';

export interface RecordingControlsProps {
  stream: MediaStream | null;
  onRecordingStart?: () => void;
  onRecordingStop?: (filePath: string) => void;
  uploadUrl?: string;
}

export const RecordingControls: React.FC<RecordingControlsProps> = ({
  stream,
  onRecordingStart,
  onRecordingStop,
  uploadUrl = 'http://localhost:8080/api/capture/web/upload',
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
      Alert.alert('错误', '摄像头未启动，无法录制');
      return;
    }

    try {
      const mediaRecorder = new MediaRecorder(stream);

      chunksRef.current = [];

      mediaRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          chunksRef.current.push(event.data);
        }
      };

      mediaRecorder.onstop = async () => {
        const blob = new Blob(chunksRef.current, { type: 'video/mp4' });
        chunksRef.current = [];

        const filePath = `${RNFS.DocumentDirectoryPath}/recording_${Date.now()}.mp4`;
        
        try {
          const fileData = await blobToBase64(blob);
          await RNFS.writeFile(filePath, fileData, 'base64');
          
          onRecordingStop?.(filePath);
          
          await uploadRecording(filePath);
        } catch (err) {
          console.error('Save file error:', err);
          Alert.alert('保存失败', '无法保存录制文件');
        }
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
      console.error('Start recording error:', error);
      Alert.alert('录制失败', '无法开始录制');
    }
  }, [stream, onRecordingStop, onRecordingStart]);

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

  const blobToBase64 = async (blob: Blob): Promise<string> => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onloadend = () => {
        const base64String = reader.result as string;
        const base64Data = base64String.split(',')[1];
        resolve(base64Data);
      };
      reader.onerror = reject;
      reader.readAsDataURL(blob);
    });
  };

  const uploadRecording = async (filePath: string) => {
    setIsUploading(true);
    setUploadProgress(0);

    try {
      const formData = new FormData();
      
      const fileStat = await RNFS.stat(filePath);
      const fileData = await RNFS.readFile(filePath, 'base64');
      
      formData.append('file', {
        uri: Platform.OS === 'ios' ? filePath : `file://${filePath}`,
        type: 'video/mp4',
        name: `recording_${Date.now()}.mp4`,
      } as any);

      const response = await fetch(uploadUrl, {
        method: 'POST',
        body: formData,
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      if (response.ok) {
        const result = await response.json();
        Alert.alert('上传成功', '视频已成功上传');
        
        try {
          await RNFS.unlink(filePath);
        } catch (err) {
          console.error('Delete file error:', err);
        }
      } else {
        Alert.alert('上传失败', '请重试');
      }
    } catch (error) {
      console.error('Upload error:', error);
      Alert.alert('上传失败', error instanceof Error ? error.message : '未知错误');
    } finally {
      setIsUploading(false);
      setUploadProgress(100);
    }
  };

  useEffect(() => {
    return () => {
      if (timerRef.current) {
        clearInterval(timerRef.current);
      }
      if (mediaRecorderRef.current && isRecording) {
        mediaRecorderRef.current.stop();
      }
    };
  }, [isRecording]);

  return (
    <View style={styles.container}>
      {isUploading && (
        <View style={styles.uploadingOverlay}>
          <ActivityIndicator size="large" color="#fff" />
          <Text style={styles.uploadingText}>上传中... {uploadProgress}%</Text>
        </View>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  uploadingOverlay: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'rgba(0, 0, 0, 0.7)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  uploadingText: {
    color: '#fff',
    marginTop: 16,
    fontSize: 16,
  },
});
