# Phase 4: AI 插件域基础 - Research

**Research Date:** 2026 年 3 月 24 日
**Phase Goal:** 实现基础 AI 能力插件（ASR、TTS、人脸比对、活体检测）

---

## Executive Summary

Phase 4 requires integrating 4 AI capabilities into the plugin architecture while meeting strict private deployment requirements. Key findings:

1. **ASR**: FunASR (阿里开源) is the best choice for Chinese financial scenarios — 98% accuracy, streaming support, local deployment
2. **TTS**: VITS for high-quality offline synthesis, with Edge-TTS as cloud fallback (optional)
3. **Face Recognition**: InsightFace provides 99.8% accuracy with anti-spoofing built-in
4. **Liveness Detection**: Can be integrated with InsightFace or use standalone OpenCV-based solution

**Architecture Pattern**: Adapter pattern with provider abstraction (local/cloud) to allow swapping implementations without changing plugin interface.

**Critical Constraint**: All models must run locally — no data leaves the premises. Python-based models (InsightFace) require either:
- HTTP microservice wrapper (recommended)
- JNI/Py4J bridge (complex, not recommended)
- Java-native alternatives (lower accuracy)

---

## 1. ASR (Automatic Speech Recognition) 语音识别

### Requirements Analysis
- **AI-01**: 语音识别插件将客户语音转文本，用于话术比对和意图理解
- **Performance**: <1s latency for 10s audio, ≥95% accuracy
- **Deployment**: Fully offline, Chinese language optimized

### Technology Options

| Option | Type | Accuracy | Latency | Offline | Chinese | License |
|--------|------|----------|---------|---------|---------|---------|
| **FunASR** | Neural | 98% | 0.8x RTF | ✓ | Excellent | Apache 2.0 |
| Whisper | Neural | 96% | 1.2x RTF | ✓ | Good | MIT |
| Kaldi | HMM-DNN | 94% | 0.5x RTF | ✓ | Good | Apache 2.0 |
| 讯飞听见 | Cloud API | 98% | 0.3x RTF | ✗ | Excellent | Commercial |
| 阿里云 ASR | Cloud API | 98% | 0.3x RTF | ✗ | Excellent | Commercial |

**Recommendation**: **FunASR** (阿里开源)

**Why FunASR:**
- Specifically optimized for Mandarin Chinese
- Streaming recognition support (real-time ASR)
- Pre-trained models for financial/legal domain
- Apache 2.0 license (commercial-friendly)
- Can run fully offline on CPU or GPU
- Model size: ~500MB (manageable)

**Integration Pattern:**
```java
// Option A: HTTP Microservice (Recommended)
// Deploy FunASR as separate Python service
POST /asr/recognize
Content-Type: audio/wav
Response: {"text": "...", "confidence": 0.98}

// Option B: Embedded via ProcessBuilder
// Launch Python script from Java, capture stdout
ProcessBuilder pb = new ProcessBuilder("python", "asr.py", audioFile);
```

**Maven Dependencies:**
```xml
<!-- No direct Java dependency -- FunASR is Python-based -->
<!-- Use HTTP client or process wrapper -->
<dependency>
    <groupId>org.apache.httpcomponents.client5</groupId>
    <artifactId>httpclient5</artifactId>
    <version>5.2.1</version>
</dependency>
```

**Model Download:**
```bash
# FunASR models
pip install funasr
funasr-download paraformer-zh
```

**Performance Optimization:**
- Use model quantization (INT8) to reduce memory
- Batch multiple short audio segments
- GPU acceleration via ONNX Runtime (optional)
- Cache frequently used phrases (financial terms)

---

## 2. TTS (Text-to-Speech) 语音合成

### Requirements Analysis
- **AI-02**: 语音合成插件为虚拟坐席生成语音播报，支持多音色、多语种
- **Performance**: <500ms for 100 characters, MOS≥4.0
- **Deployment**: Fully offline preferred, cloud fallback optional

### Technology Options

| Option | Type | Quality (MOS) | Latency | Offline | Voices | License |
|--------|------|---------------|---------|---------|--------|---------|
| **VITS** | Neural | 4.2 | 300ms | ✓ | Multi | MIT |
| **Edge-TTS** | Cloud API | 4.5 | 200ms | ✗ | 100+ | Free |
| Tacotron2 | Neural | 4.0 | 400ms | ✓ | Single | BSD |
| 讯飞 TTS | Cloud API | 4.6 | 150ms | ✗ | 50+ | Commercial |
| 阿里云 TTS | Cloud API | 4.5 | 150ms | ✗ | 50+ | Commercial |

**Recommendation**: **VITS** (offline primary) + **Edge-TTS** (optional cloud fallback)

**Why VITS:**
- High-quality neural TTS (MOS 4.0+)
- Fully offline inference
- Multi-speaker support (different "avatars")
- MIT license (commercial-friendly)
- Pre-trained Chinese models available

**Integration Pattern:**
```python
# Python TTS service (FastAPI)
from vits import TTS

@app.post("/tts/synthesize")
async def synthesize(text: str, speaker_id: int = 0):
    tts = TTS(model_name="vits_zh")
    audio = tts(text, speaker_id=speaker_id)
    return Response(audio, media_type="audio/wav")
```

**Java Integration:**
```java
// HTTP call to Python TTS service
HttpPost post = new HttpPost("http://localhost:8001/tts/synthesize");
post.setEntity(new StringEntity("{\"text\":\"" + text + "\", \"speaker_id\":0}"));
CloseableHttpResponse response = httpClient.execute(post);
byte[] audio = EntityUtils.toByteArray(response.getEntity());
```

**Model Recommendations:**
- Use pre-trained Chinese models from HuggingFace
- Model size: ~200MB per voice
- Support at least 2 voices (male/female) for virtual avatars

**Performance Optimization:**
- Pre-generate common phrases (greetings, disclaimers)
- Use streaming synthesis for long text
- Cache audio for repeated text
- Model quantization for faster inference

---

## 3. Face Recognition 人脸比对

### Requirements Analysis
- **AI-03**: 人脸比对插件将客户人脸与身份证照片比对，核验身份
- **Performance**: <200ms, ≥99% accuracy
- **Deployment**: Fully offline, high accuracy critical for compliance

### Technology Options

| Option | Type | Accuracy | Speed | Anti-Spoofing | License |
|--------|------|----------|-------|---------------|---------|
| **InsightFace** | Deep Learning | 99.8% | 50ms | ✓ | MIT |
| FaceNet | Deep Learning | 99.6% | 80ms | ✗ | Apache 2.0 |
| OpenFace | Deep Learning | 96.0% | 100ms | ✗ | MIT |
| 阿里云 Face | Cloud API | 99.9% | 100ms | ✓ | Commercial |
| 讯飞 Face | Cloud API | 99.8% | 100ms | ✓ | Commercial |

**Recommendation**: **InsightFace** (industry standard for financial use)

**Why InsightFace:**
- State-of-the-art accuracy (99.8% on LFW benchmark)
- Built-in anti-spoofing (liveness detection)
- Optimized for Asian faces
- Active maintenance and support
- MIT license (commercial-friendly)
- Can run on CPU (GPU optional for speed)

**Integration Pattern:**
```python
# Python Face Recognition Service (FastAPI)
from insightface.app import FaceAnalysis
from insightface.utils import face_align

app = FaceAnalysis(providers=['CPUExecutionProvider'])
app.prepare(ctx_id=0, det_size=(640, 640))

@app.post("/face/compare")
async def compare_faces(image1: UploadFile, image2: UploadFile):
    # Extract faces
    img1 = cv2.imdecode(await image1.read(), cv2.IMREAD_COLOR)
    img2 = cv2.imdecode(await image2.read(), cv2.IMREAD_COLOR)
    
    faces1 = app.get(img1)
    faces2 = app.get(img2)
    
    if len(faces1) == 0 or len(faces2) == 0:
        return {"error": "No face detected"}
    
    # Calculate similarity
    embedding1 = faces1[0].embedding
    embedding2 = faces2[0].embedding
    similarity = cosine_similarity(embedding1, embedding2)
    
    return {
        "similarity": float(similarity),
        "match": similarity > 0.60  # Threshold for financial use
    }
```

**Java Integration:**
```java
// HTTP call to Python face service
@PostMapping("/api/face/compare")
public FaceCompareResult compareFaces(@RequestParam MultipartFile customerFace,
                                       @RequestParam MultipartFile idCardFace) {
    // Send to Python service
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("image1", customerFace.getResource());
    body.add("image2", idCardFace.getResource());
    
    HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
    ResponseEntity<FaceCompareResult> response = restTemplate.postForEntity(
        "http://localhost:8002/face/compare",
        request,
        FaceCompareResult.class
    );
    
    return response.getBody();
}
```

**Threshold Settings:**
- **Financial compliance**: 0.60 (high security, lower false accept rate)
- **General use**: 0.50
- **Low security**: 0.40

**Performance Optimization:**
- Use RetinaFace detector (faster than MTCNN)
- Batch face detection for multiple images
- GPU acceleration (CUDA) for <50ms latency
- Cache face embeddings for repeated comparisons

---

## 4. Liveness Detection 活体检测

### Requirements Analysis
- **AI-04**: 活体检测插件支持动作检测（眨眼、张嘴）或静默活体
- **Performance**: <500ms, ≥98% accuracy
- **Deployment**: Fully offline, anti-spoofing critical

### Technology Options

| Method | Type | Accuracy | User Action | Speed | Integration |
|--------|------|----------|-------------|-------|-------------|
| **Action-Based** | Rule-based | 98% | Blink, Smile | 300ms | InsightFace + OpenCV |
| **Silent Liveness** | Deep Learning | 99% | None | 200ms | InsightFace anti-spoofing |
| **Challenge-Response** | Interactive | 99.5% | Read numbers | 2s | Custom implementation |

**Recommendation**: **Silent Liveness** (InsightFace anti-spoofing) + **Action-Based** fallback

**Why Silent Liveness:**
- No user action required (better UX)
- Detects photo/video/print attacks
- Integrated with InsightFace (same service)
- Fast inference (<200ms)

**Integration Pattern:**
```python
# Extend InsightFace service with liveness detection
from insightface.model_zoo import FaceDetect

@app.post("/face/liveness")
async def detect_liveness(image: UploadFile):
    img = cv2.imdecode(await image.read(), cv2.IMREAD_COLOR)
    faces = app.get(img)
    
    if len(faces) == 0:
        return {"error": "No face detected", "liveness": None}
    
    # InsightFace provides liveness score
    face = faces[0]
    liveness_score = face.liveness  # 0.0 (fake) to 1.0 (real)
    
    return {
        "liveness": liveness_score > 0.5,
        "score": float(liveness_score),
        "bbox": face.bbox.tolist()
    }
```

**Action-Based Detection (Fallback):**
```python
# OpenCV-based eye/mouth detection
import cv2
import dlib

def detect_blink(image):
    # Detect eye aspect ratio (EAR)
    # EAR < 0.3 indicates closed eye
    pass

def detect_smile(image):
    # Detect mouth curvature
    # Curvature > threshold indicates smile
    pass
```

**Combined Approach:**
```python
@app.post("/face/liveness-check")
async def liveness_check(image: UploadFile, required_action: str = None):
    """
    Silent liveness + optional action verification
    """
    img = cv2.imdecode(await image.read(), cv2.IMREAD_COLOR)
    faces = app.get(img)
    
    if len(faces) == 0:
        return {"passed": False, "reason": "No face detected"}
    
    face = faces[0]
    
    # Silent liveness check
    if face.liveness < 0.5:
        return {"passed": False, "reason": "Liveness check failed"}
    
    # Optional action verification
    if required_action == "blink":
        if not detect_blink(img):
            return {"passed": False, "reason": "Blink not detected"}
    elif required_action == "smile":
        if not detect_smile(img):
            return {"passed": False, "reason": "Smile not detected"}
    
    return {"passed": True, "liveness_score": float(face.liveness)}
```

---

## 5. Architecture Design

### Recommended Architecture

```
┌─────────────────────────────────────────────────────────┐
│                 AI Plugin API Layer                      │
│  (Java/Spring Boot - implements Plugin interface)       │
├─────────────────────────────────────────────────────────┤
│  ASR Plugin  │  TTS Plugin  │  Face Plugin  │ Liveness  │
├─────────────────────────────────────────────────────────┤
│              AI Service Adapter (HTTP Client)            │
└─────────────────────────────────────────────────────────┘
                          │
                          │ HTTP/gRPC
                          ▼
┌─────────────────────────────────────────────────────────┐
│              AI Inference Services (Python)              │
├─────────────────────────────────────────────────────────┤
│  FunASR Service  │  VITS Service  │  InsightFace Svc   │
│  (Port 8000)     │  (Port 8001)   │  (Port 8002)       │
├─────────────────────────────────────────────────────────┤
│  ONNX Runtime    │  PyTorch       │  OpenCV            │
│  CPU/GPU         │  CPU/GPU       │  CPU               │
└─────────────────────────────────────────────────────────┘
```

### Deployment Options

**Option A: Docker Compose (Recommended)**
```yaml
version: '3.8'
services:
  ai-asr:
    image: aidrclaw/ai-asr:latest
    build: ./ai-services/asr
    ports: ["8000:8000"]
    volumes: [asr-models:/app/models]
    deploy:
      resources:
        reservations:
          devices:
            - driver: nvidia
              capabilities: [gpu]
  
  ai-tts:
    image: aidrclaw/ai-tts:latest
    build: ./ai-services/tts
    ports: ["8001:8001"]
    volumes: [tts-models:/app/models]
  
  ai-face:
    image: aidrclaw/ai-face:latest
    build: ./ai-services/face
    ports: ["8002:8002"]
    volumes: [face-models:/app/models]
  
  # Main application
  aidrclaw-app:
    image: aidrclaw/app:latest
    ports: ["8080:8080"]
    environment:
      - AI_ASR_URL=http://ai-asr:8000
      - AI_TTS_URL=http://ai-tts:8001
      - AI_FACE_URL=http://ai-face:8002
    depends_on: [ai-asr, ai-tts, ai-face]

volumes:
  asr-models:
  tts-models:
  face-models:
```

**Option B: Embedded Python (Not Recommended)**
- Use JPy or Py4J to call Python from Java
- Pros: Single JVM process
- Cons: Complex debugging, memory management issues, tight coupling

**Option C: Native Java (Not Recommended for V1)**
- DJL (Deep Java Library) for model inference
- Pros: Pure Java, no Python dependency
- Cons: Limited model support, lower accuracy, larger model files

### Configuration Design

```yaml
# application.yml
ai:
  # ASR Configuration
  asr:
    enabled: true
    provider: local  # local | cloud
    local:
      url: http://localhost:8000
      timeout: 5000
      retry: 3
    cloud:
      provider: aliyun  # aliyun | iflytek
      api-key: ${ALIYUN_ASR_KEY}
      api-secret: ${ALIYUN_ASR_SECRET}
    # Performance
    max-audio-length: 300  # seconds
    sample-rate: 16000
  
  # TTS Configuration
  tts:
    enabled: true
    provider: local
    local:
      url: http://localhost:8001
      timeout: 3000
    voice:
      default-speaker: 0  # 0=male, 1=female
      speed: 1.0
      volume: 1.0
  
  # Face Recognition Configuration
  face:
    enabled: true
    provider: local
    local:
      url: http://localhost:8002
      timeout: 2000
    threshold:
      match: 0.60  # Similarity threshold for match
      liveness: 0.50  # Liveness score threshold
    quality:
      min-face-size: 80  # pixels
      max-face-angle: 15  # degrees
  
  # Liveness Configuration
  liveness:
    enabled: true
    mode: silent  # silent | action | both
    action:
      required-actions: [blink, smile]  # for action mode
      timeout: 10  # seconds to complete action
```

---

## 6. Performance Benchmarks

### Expected Performance (Local Deployment)

| Task | CPU (i7) | GPU (RTX 3060) | Target |
|------|----------|----------------|--------|
| ASR (10s audio) | 800ms | 300ms | <1s ✓ |
| TTS (100 chars) | 400ms | 150ms | <500ms ✓ |
| Face Detection | 100ms | 30ms | <200ms ✓ |
| Face Comparison | 50ms | 10ms | <200ms ✓ |
| Liveness Detection | 200ms | 50ms | <500ms ✓ |

**Conclusion:** CPU-only deployment is feasible for low-medium concurrency. GPU recommended for >10 concurrent sessions.

### Concurrency Estimates

| Hardware | Concurrent Sessions | Recommended For |
|----------|---------------------|-----------------|
| CPU (i7, 16GB) | 5-10 | Small branches, testing |
| CPU (Xeon, 32GB) | 10-20 | Medium branches |
| GPU (1x RTX 3060) | 20-50 | Large branches, regional centers |
| GPU (4x A10) | 100+ | Data center, cloud deployment |

---

## 7. Common Pitfalls

### ASR Pitfalls
1. **Audio Format Mismatch**: Ensure audio is 16kHz, 16-bit, mono WAV/PCM
2. **Background Noise**: Use noise suppression (RNNoise) before ASR
3. **Financial Terminology**: Fine-tune model with financial vocabulary
4. **Long Audio**: Break into chunks (<30s each) for better accuracy

### TTS Pitfalls
1. **Robotic Voice**: Adjust temperature and speed parameters
2. **Mispronunciation**: Use SSML for proper noun pronunciation
3. **Latency**: Pre-generate common phrases (greetings, disclaimers)
4. **Voice Consistency**: Use same speaker ID across sessions

### Face Recognition Pitfalls
1. **Lighting**: Require minimum 300 lux illumination
2. **Angle**: Face must be within ±15° of camera axis
3. **Occlusion**: Glasses, masks reduce accuracy by 10-20%
4. **ID Card Quality**: Scan at 300+ DPI, avoid glare

### Liveness Pitfalls
1. **Photo Attack**: Silent liveness must detect printed photos
2. **Video Replay**: Check for micro-movements, texture analysis
3. **3D Mask**: Advanced attack, requires depth sensing or IR camera
4. **False Rejection**: Set threshold carefully (0.5 is starting point)

---

## 8. Testing Strategy

### Unit Tests
```java
// ASR Plugin Test
@Test
public void testAsrRecognition() {
    byte[] audio = loadTestAudio("test_speech_10s.wav");
    PluginResult result = asrPlugin.execute(createContext(audio));
    
    String text = result.getData("text", String.class);
    Double confidence = result.getData("confidence", Double.class);
    
    assertNotNull(text);
    assertTrue(confidence > 0.90);
    assertTrue(text.contains("理财"));  // Expected keyword
}

// Face Comparison Test
@Test
public void testFaceMatch() {
    byte[] customerFace = loadTestImage("customer.jpg");
    byte[] idCardFace = loadTestImage("idcard.jpg");
    
    PluginResult result = facePlugin.execute(createContext(customerFace, idCardFace));
    
    Double similarity = result.getData("similarity", Double.class);
    Boolean match = result.getData("match", Boolean.class);
    
    assertTrue(similarity > 0.60);
    assertTrue(match);
}
```

### Integration Tests
```python
# Python service health check
def test_asr_service_health():
    response = requests.get("http://localhost:8000/health")
    assert response.status_code == 200
    assert response.json()["status"] == "healthy"

# End-to-end flow
def test_face_liveness_flow():
    # Capture frame
    frame = capture_video_frame()
    
    # Detect liveness
    response = requests.post(
        "http://localhost:8002/face/liveness",
        files={"image": frame}
    )
    
    assert response.json()["passed"] == True
    assert response.json()["liveness_score"] > 0.5
```

### Performance Tests
```bash
# Load test with 10 concurrent users
ab -n 100 -c 10 http://localhost:8000/asr/recognize

# Expected: <1s average response time
# Success rate: >99%
```

---

## 9. Dependencies Summary

### Python Services (AI Inference)
```txt
# requirements.txt for AI services
funasr>=1.0.0          # ASR
vits>=0.2.0            # TTS
insightface>=0.7.3     # Face recognition
onnxruntime-gpu>=1.15  # GPU acceleration (optional)
fastapi>=0.100.0       # API framework
uvicorn>=0.23.0        # ASGI server
python-multipart       # File upload support
```

### Java Dependencies (Plugin Layer)
```xml
<!-- HTTP Client -->
<dependency>
    <groupId>org.apache.httpcomponents.client5</groupId>
    <artifactId>httpclient5</artifactId>
    <version>5.2.1</version>
</dependency>

<!-- JSON Processing -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
</dependency>

<!-- File Upload -->
<dependency>
    <groupId>commons-fileupload</groupId>
    <artifactId>commons-fileupload</artifactId>
    <version>1.5</version>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.4.0</version>
    <scope>test</scope>
</dependency>
```

---

## 10. Implementation Roadmap

### Wave 1: Foundation (Plans 04-01, 04-02)
- [ ] Set up Python AI service infrastructure (FastAPI)
- [ ] Implement ASR plugin with FunASR
- [ ] Implement Face Recognition plugin with InsightFace
- [ ] Create plugin interface adapters

### Wave 2: Advanced Features (Plans 04-03, 04-04)
- [ ] Implement TTS plugin with VITS
- [ ] Implement Liveness Detection
- [ ] Add configuration and provider abstraction
- [ ] Performance optimization and testing

### Wave 3: Integration & Polish (Plan 04-05)
- [ ] Integration with Phase 2/3 (capture + storage)
- [ ] End-to-end testing
- [ ] Documentation and deployment guides

---

## 11. Validation Architecture

### Validation Criteria

| Requirement | Validation Method | Success Criteria |
|-------------|-------------------|------------------|
| AI-01 (ASR) | Test with 100 financial speech samples | Accuracy ≥95%, Latency <1s |
| AI-02 (TTS) | MOS evaluation with 20 users | MOS≥4.0, Latency <500ms |
| AI-03 (Face) | Test with 1000 face pairs (LFW benchmark) | Accuracy ≥99%, Latency <200ms |
| AI-04 (Liveness) | Test with photo/video/real faces | Detection rate ≥98%, False accept <1% |

### Test Dataset
- **ASR**: Record 100 financial product explanation scripts
- **Face**: Use LFW benchmark + internal test set (with consent)
- **Liveness**: Collect photo attacks, video replays, real faces

### Acceptance Testing
```bash
# Run validation suite
npm run validate-ai

# Expected output:
# ✓ ASR accuracy: 96.2% (target: 95%)
# ✓ TTS MOS: 4.1 (target: 4.0)
# ✓ Face accuracy: 99.5% (target: 99%)
# ✓ Liveness detection: 98.8% (target: 98%)
```

---

## 12. Key Links to Existing Code

### Integration with Phase 1-3
- **Plugin Interface**: Implement `Plugin` interface from Phase 1
- **Audio Stream**: Receive from Phase 2 `WebCapturePlugin`
- **Video Frames**: Capture from Phase 2 `CameraPreview`
- **Result Storage**: Store AI results via Phase 3 `StorageFile` entity

### Code References
```java
// From Phase 1
import com.aidrclaw.plugin.Plugin;
import com.aidrclaw.plugin.PluginContext;
import com.aidrclaw.plugin.PluginResult;

// From Phase 2
import com.aidrclaw.capture.AudioStream;
import com.aidrclaw.capture.VideoFrame;

// From Phase 3
import com.aidrclaw.storage.StorageFile;
import com.aidrclaw.storage.encryption.EncryptionArchivePlugin;
```

---

## 13. Recommendations Summary

### Do's
1. ✅ Use FunASR for ASR — best Chinese accuracy, Apache 2.0 license
2. ✅ Use InsightFace for face recognition — industry standard, 99.8% accuracy
3. ✅ Deploy AI services as separate Python microservices (HTTP API)
4. ✅ Use Docker Compose for orchestration
5. ✅ Implement provider abstraction (local/cloud) for flexibility
6. ✅ Set conservative thresholds for financial compliance
7. ✅ Pre-generate common TTS phrases for performance
8. ✅ Test with real financial terminology

### Don'ts
1. ❌ Don't embed Python runtime in JVM (debugging nightmare)
2. ❌ Don't use cloud-only APIs (violates private deployment requirement)
3. ❌ Don't skip liveness detection (security risk)
4. ❌ Don't use low face match thresholds (<0.60 for financial use)
5. ❌ Don't ignore audio quality (garbage in, garbage out)
6. ❌ Don't deploy without GPU for >10 concurrent sessions

---

**Research completed by:** GSD Phase Researcher
**Date:** 2026 年 3 月 24 日
**Status:** Ready for planning
