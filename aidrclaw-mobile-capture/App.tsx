import React, { useState } from 'react';
import { StatusBar } from 'react-native';
import { SafeAreaProvider, SafeAreaView } from 'react-native-safe-area-context';
import { CameraPreview } from './src/components/CameraPreview';
import { RecordingControls } from './src/components/RecordingControls';
import { useCamera } from './src/hooks/useCamera';

const App: React.FC = () => {
  const [isRecording, setIsRecording] = useState(false);
  const [recordingTime, setRecordingTime] = useState(0);

  const camera = useCamera({
    facingMode: 'user',
    audioConstraints: true,
  });

  const handleStartRecording = () => {
    setIsRecording(true);
    setRecordingTime(0);
    console.log('Recording started');
  };

  const handleStopRecording = () => {
    setIsRecording(false);
    console.log('Recording stopped, total time:', recordingTime, 'seconds');
  };

  return (
    <SafeAreaProvider>
      <SafeAreaView style={{ flex: 1 }} edges={['top', 'bottom']}>
        <StatusBar barStyle="light-content" backgroundColor="#000" />
        
        <CameraPreview
          camera={camera}
          onStartRecording={handleStartRecording}
          onStopRecording={handleStopRecording}
          isRecording={isRecording}
          recordingTime={recordingTime}
        />

        <RecordingControls
          stream={camera.stream}
          onRecordingStart={handleStartRecording}
          onRecordingStop={(filePath) => {
            console.log('Video saved to:', filePath);
            setRecordingTime(0);
          }}
        />
      </SafeAreaView>
    </SafeAreaProvider>
  );
};

export default App;
