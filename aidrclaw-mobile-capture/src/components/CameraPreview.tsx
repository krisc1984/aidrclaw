import React from 'react';
import { StyleSheet, View, Text, ActivityIndicator } from 'react-native';
import { RTCView } from 'react-native-webrtc';
import { UseCameraReturn } from '../hooks/useCamera';

export interface CameraPreviewProps {
  camera: UseCameraReturn;
  onToggleCamera?: () => void;
  onStartRecording?: () => void;
  onStopRecording?: () => void;
  isRecording?: boolean;
  recordingTime?: number;
}

export const CameraPreview: React.FC<CameraPreviewProps> = ({
  camera,
  onToggleCamera,
  onStartRecording,
  onStopRecording,
  isRecording = false,
  recordingTime = 0,
}) => {
  const { stream, hasPermission, error, isRequesting, facingMode, toggleCamera, toggleMute, isMuted } = camera;

  const formatTime = (seconds: number): string => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  if (error) {
    return (
      <View style={styles.errorContainer}>
        <Text style={styles.errorIcon}>⚠️</Text>
        <Text style={styles.errorText}>{error}</Text>
        <Text style={styles.errorHint}>请检查权限设置后重试</Text>
      </View>
    );
  }

  if (isRequesting) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#007AFF" />
        <Text style={styles.loadingText}>正在启动摄像头...</Text>
      </View>
    );
  }

  if (!hasPermission || !stream) {
    return (
      <View style={styles.placeholderContainer}>
        <Text style={styles.placeholderIcon}>📷</Text>
        <Text style={styles.placeholderText}>摄像头未启动</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <RTCView
        stream={stream}
        objectFit="cover"
        style={styles.preview}
        mirror={facingMode === 'user'}
      />

      <View style={styles.controls}>
        <View style={styles.topControls}>
          <Text style={styles.timer}>
            {isRecording ? formatTime(recordingTime) : '00:00'}
          </Text>
        </View>

        <View style={styles.bottomControls}>
          <View style={styles.leftControls}>
            <View style={styles.controlButton}>
              <Text style={styles.controlButtonText} onPress={toggleMute}>
                {isMuted ? '🔇' : '🔊'}
              </Text>
            </View>
          </View>

          <View style={styles.centerControls}>
            <View
              style={[styles.recordButton, isRecording && styles.recordButtonActive]}
            >
              <Text
                style={styles.recordButtonText}
                onPress={isRecording ? onStopRecording : onStartRecording}
              >
                {isRecording ? '⏹️' : '🔴'}
              </Text>
            </View>
          </View>

          <View style={styles.rightControls}>
            <View style={styles.controlButton}>
              <Text style={styles.controlButtonText} onPress={toggleCamera}>
                🔄
              </Text>
            </View>
          </View>
        </View>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#000',
  },
  preview: {
    flex: 1,
  },
  errorContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#000',
    padding: 20,
  },
  errorIcon: {
    fontSize: 48,
    marginBottom: 16,
  },
  errorText: {
    color: '#fff',
    fontSize: 16,
    textAlign: 'center',
    marginBottom: 8,
  },
  errorHint: {
    color: '#999',
    fontSize: 14,
    textAlign: 'center',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#000',
  },
  loadingText: {
    color: '#fff',
    marginTop: 16,
    fontSize: 16,
  },
  placeholderContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#000',
  },
  placeholderIcon: {
    fontSize: 64,
    marginBottom: 16,
  },
  placeholderText: {
    color: '#666',
    fontSize: 16,
  },
  controls: {
    ...StyleSheet.absoluteFillObject,
    justifyContent: 'space-between',
  },
  topControls: {
    paddingTop: 60,
    alignItems: 'center',
  },
  timer: {
    color: '#fff',
    fontSize: 24,
    fontWeight: '600',
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
  },
  bottomControls: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingBottom: 40,
    paddingHorizontal: 20,
  },
  leftControls: {
    flex: 1,
    alignItems: 'flex-start',
  },
  centerControls: {
    flex: 1,
    alignItems: 'center',
  },
  rightControls: {
    flex: 1,
    alignItems: 'flex-end',
  },
  recordButton: {
    width: 80,
    height: 80,
    borderRadius: 40,
    backgroundColor: 'rgba(255, 255, 255, 0.3)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  recordButtonActive: {
    backgroundColor: 'rgba(255, 59, 48, 0.8)',
  },
  recordButtonText: {
    fontSize: 36,
  },
  controlButton: {
    width: 50,
    height: 50,
    borderRadius: 25,
    backgroundColor: 'rgba(255, 255, 255, 0.3)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  controlButtonText: {
    fontSize: 24,
  },
});
