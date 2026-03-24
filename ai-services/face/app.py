from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import base64
import logging
import numpy as np
from insightface.app import FaceAnalysis

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Face Recognition Service", version="1.0.0")

face_app = None


class FaceCompareRequest(BaseModel):
    image1: str
    image2: str


class FaceCompareResponse(BaseModel):
    similarity: float
    match: bool
    threshold: float = 0.6
    success: bool


class LivenessRequest(BaseModel):
    image: str
    actions: list = ["blink", "nod"]


class LivenessResponse(BaseModel):
    passed: bool
    detected_actions: list
    success: bool


@app.on_event("startup")
async def load_model():
    global face_app
    try:
        face_app = FaceAnalysis(name="buffalo_l")
        face_app.prepare(ctx_id=0, det_size=(640, 640))
        logger.info("InsightFace model loaded successfully")
    except Exception as e:
        logger.error(f"Failed to load model: {e}")
        raise


@app.post("/face/compare", response_model=FaceCompareResponse)
async def compare_faces(request: FaceCompareRequest):
    try:
        img1_data = base64.b64decode(request.image1)
        img2_data = base64.b64decode(request.image2)

        img1 = np.frombuffer(img1_data, np.uint8)
        img2 = np.frombuffer(img2_data, np.uint8)

        faces1 = face_app.get(img1)
        faces2 = face_app.get(img2)

        if not faces1 or not faces2:
            return FaceCompareResponse(similarity=0.0, match=False, success=False)

        embedding1 = faces1[0].embedding
        embedding2 = faces2[0].embedding

        similarity = np.dot(embedding1, embedding2) / (
            np.linalg.norm(embedding1) * np.linalg.norm(embedding2)
        )
        similarity = (similarity + 1) / 2

        threshold = 0.6
        match = similarity > threshold

        logger.info(f"Face compare: similarity={similarity:.4f}, match={match}")

        return FaceCompareResponse(
            similarity=similarity, match=match, threshold=threshold, success=True
        )

    except Exception as e:
        logger.error(f"Face compare failed: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/face/liveness", response_model=LivenessResponse)
async def check_liveness(request: LivenessRequest):
    try:
        img_data = base64.b64decode(request.image)
        img = np.frombuffer(img_data, np.uint8)

        faces = face_app.get(img)

        if not faces:
            return LivenessResponse(passed=False, detected_actions=[], success=False)

        face = faces[0]

        detected_actions = []

        if hasattr(face, "landmarks_2d") and face.landmarks_2d is not None:
            left_eye = face.landmarks_2d[0]
            right_eye = face.landmarks_2d[1]
            mouth = face.landmarks_2d[3]

            eye_aspect_ratio = abs(left_eye[1] - right_eye[1])
            if eye_aspect_ratio > 10:
                detected_actions.append("eyes_open")
            else:
                detected_actions.append("blink")

            if mouth[1] > left_eye[1] + 20:
                detected_actions.append("mouth_open")

        passed = len(detected_actions) > 0

        logger.info(f"Liveness check: passed={passed}, actions={detected_actions}")

        return LivenessResponse(
            passed=passed, detected_actions=detected_actions, success=True
        )

    except Exception as e:
        logger.error(f"Liveness check failed: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/health")
async def health():
    return {"status": "healthy", "model_loaded": face_app is not None}


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8001)
