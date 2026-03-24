from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import base64
import logging
from funasr import AutoModel

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="ASR Service", version="1.0.0")

model = None


class AsrRequest(BaseModel):
    audio: str
    sample_rate: int = 16000


class AsrResponse(BaseModel):
    text: str
    confidence: float
    success: bool


@app.on_event("startup")
async def load_model():
    global model
    try:
        model = AutoModel(model="paraformer-zh", device="cpu")
        logger.info("FunASR model loaded successfully")
    except Exception as e:
        logger.error(f"Failed to load model: {e}")
        raise


@app.post("/asr/recognize", response_model=AsrResponse)
async def recognize(request: AsrRequest):
    try:
        audio_data = base64.b64decode(request.audio)

        res = model.generate(input=audio_data)

        text = res[0]["text"] if res else ""
        confidence = res[0].get("confidence", 0.95) if res else 0.0

        logger.info(f"ASR result: {text[:50]}... (confidence: {confidence})")

        return AsrResponse(text=text, confidence=confidence, success=True)

    except Exception as e:
        logger.error(f"ASR failed: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/health")
async def health():
    return {"status": "healthy", "model_loaded": model is not None}


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)
