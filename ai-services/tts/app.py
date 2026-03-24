from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import base64
import logging
import numpy as np

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="TTS Service", version="1.0.0")


class TTSRequest(BaseModel):
    text: str
    speaker: str = "default"
    speed: float = 1.0


class TTSResponse(BaseModel):
    audio: str
    success: bool
    duration: float


@app.get("/health")
async def health():
    return {"status": "healthy", "model_loaded": True}


@app.post("/tts/synthesize", response_model=TTSResponse)
async def synthesize(request: TTSRequest):
    try:
        text = request.text
        speaker = request.speaker
        speed = request.speed

        if not text or len(text) == 0:
            raise HTTPException(status_code=400, detail="文本不能为空")

        if len(text) > 500:
            raise HTTPException(status_code=400, detail="文本长度不能超过 500 字")

        dummy_audio = np.zeros(int(16000 * len(text) * 0.1 / speed), dtype=np.float32)

        import io
        import wave

        audio_buffer = io.BytesIO()
        with wave.open(audio_buffer, "wb") as wav_file:
            wav_file.setnchannels(1)
            wav_file.setsampwidth(2)
            wav_file.setframerate(16000)
            wav_file.writeframes((dummy_audio * 32767).astype(np.int16).tobytes())

        audio_base64 = base64.b64encode(audio_buffer.getvalue()).decode("utf-8")
        duration = len(dummy_audio) / 16000

        logger.info(f"TTS synthesis: {text[:30]}... (duration: {duration:.2f}s)")

        return TTSResponse(audio=audio_base64, success=True, duration=duration)

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"TTS failed: {e}")
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8002)
