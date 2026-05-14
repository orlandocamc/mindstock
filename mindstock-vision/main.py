# ============================================================
# ARCHIVO: mindstock-vision/main.py
#
# MindStock Vision - Microservicio de Visión Artificial
# Se comunica con el backend Spring Boot via HTTP
#
# Modos de operación:
# 1. DEMO: Simula detecciones para pruebas sin cámara
# 2. CAMERA: Usa cámara real + YOLOv8 (cuando tengan el modelo)
# ============================================================

from fastapi import FastAPI, HTTPException, File, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional
import requests
import time
from datetime import datetime
import numpy as np
import cv2

# Importar nuestros módulos
from detector import ObjectDetector
from config import settings

app = FastAPI(
    title="MindStock Vision",
    description="Microservicio de detección de objetos para inventario inteligente",
    version="1.0.0"
)

# CORS para que el frontend pueda conectarse
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# Instancia del detector
detector = ObjectDetector()


# -------------------------------------------------------
# Modelos de datos (Pydantic)
# -------------------------------------------------------
class DetectionResult(BaseModel):
    item_id: int
    item_name: str
    confidence: float
    action: str  # "RETIRO" o "DEVOLUCION"
    timestamp: str


class ManualDetectionRequest(BaseModel):
    item_id: int
    action: str  # "RETIRO" o "DEVOLUCION"


class LoanConfirmRequest(BaseModel):
    user_id: int
    item_id: int
    cantidad: int = 1
    confidence_score: Optional[float] = None


class StatusResponse(BaseModel):
    status: str
    mode: str
    camera_active: bool
    last_detection: Optional[str] = None
    backend_url: str


# -------------------------------------------------------
# Estado del servicio
# -------------------------------------------------------
service_state = {
    "camera_active": False,
    "last_detection": None,
    "detections_count": 0,
}


# -------------------------------------------------------
# Endpoints
# -------------------------------------------------------

@app.get("/", response_model=StatusResponse)
def get_status():
    """Estado general del servicio de visión"""
    return StatusResponse(
        status="online",
        mode=settings.MODE,
        camera_active=service_state["camera_active"],
        last_detection=service_state["last_detection"],
        backend_url=settings.BACKEND_URL,
    )


@app.get("/health")
def health_check():
    """Health check - para verificar que el servicio está vivo"""
    # Verificar conexión con el backend
    backend_ok = False
    try:
        r = requests.get(f"{settings.BACKEND_URL}/api/items/stats", timeout=3)
        backend_ok = r.status_code == 200
    except:
        pass

    return {
        "vision_service": "ok",
        "backend_connection": "ok" if backend_ok else "error",
        "mode": settings.MODE,
        "timestamp": datetime.now().isoformat(),
    }


@app.post("/detect/simulate", response_model=DetectionResult)
def simulate_detection():
    """
    MODO DEMO: Simula una detección de objeto.
    Útil para probar el flujo completo sin cámara.
    """
    result = detector.simulate_detection()

    service_state["last_detection"] = result["timestamp"]
    service_state["detections_count"] += 1

    return DetectionResult(**result)


@app.post("/detect/manual")
def manual_detection(request: ManualDetectionRequest):
    """
    Detección manual: el laboratorista indica qué objeto fue
    detectado y qué acción se realizó (retiro o devolución).
    """
    # Obtener info del item desde el backend
    try:
        r = requests.get(
            f"{settings.BACKEND_URL}/api/items/{request.item_id}",
            timeout=5
        )
        if r.status_code != 200:
            raise HTTPException(status_code=404, detail="Item no encontrado en el backend")
        item = r.json()
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="No se pudo conectar con el backend Spring Boot")

    result = {
        "item_id": request.item_id,
        "item_name": item["nombre"],
        "confidence": 1.0,  # Manual = 100% confianza
        "action": request.action,
        "timestamp": datetime.now().isoformat(),
    }

    service_state["last_detection"] = result["timestamp"]
    service_state["detections_count"] += 1

    return {
        "detection": result,
        "message": f"Detección manual registrada: {request.action} de {item['nombre']}",
    }


@app.post("/detect/confirm-loan")
def confirm_loan(request: LoanConfirmRequest):
    """
    Confirma un préstamo después de la detección.
    Envía la solicitud al backend Spring Boot.
    """
    payload = {
        "userId": request.user_id,
        "itemId": request.item_id,
        "cantidad": request.cantidad,
        "detectedBy": "AI_VISION",
        "confidenceScore": request.confidence_score,
    }

    try:
        r = requests.post(
            f"{settings.BACKEND_URL}/api/loans/requests",
            json=payload,
            timeout=5,
        )

        if r.status_code == 201:
            return {
                "success": True,
                "message": "Solicitud de préstamo creada exitosamente",
                "loan_request": r.json(),
            }
        else:
            error = r.json()
            raise HTTPException(status_code=r.status_code, detail=error.get("error", "Error desconocido"))

    except requests.exceptions.ConnectionError:
        raise HTTPException(
            status_code=503,
            detail="No se pudo conectar con el backend Spring Boot. ¿Está corriendo en el puerto 8080?",
        )


@app.get("/detect/inventory")
def get_inventory():
    """
    Obtiene el inventario actual del backend.
    Útil para mostrar qué objetos puede detectar el sistema.
    """
    try:
        r = requests.get(f"{settings.BACKEND_URL}/api/items", timeout=5)
        if r.status_code == 200:
            items = r.json()
            return {
                "total_items": len(items),
                "items": [
                    {
                        "id": item["id"],
                        "nombre": item["nombre"],
                        "categoria": item["categoryNombre"],
                        "disponibles": item["cantidadDisponible"],
                        "status": item["status"],
                    }
                    for item in items
                ],
            }
        else:
            raise HTTPException(status_code=r.status_code, detail="Error obteniendo inventario")
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="No se pudo conectar con el backend")


@app.post("/camera/start")
def start_camera():
    """Inicia la captura de cámara (cuando se implemente YOLO)"""
    if settings.MODE == "DEMO":
        return {
            "message": "Modo DEMO activo. La cámara se simula.",
            "mode": "DEMO",
            "camera_active": True,
        }

    service_state["camera_active"] = True
    return {
        "message": "Cámara iniciada",
        "camera_active": True,
    }


@app.post("/camera/stop")
def stop_camera():
    """Detiene la captura de cámara"""
    service_state["camera_active"] = False
    return {
        "message": "Cámara detenida",
        "camera_active": False,
    }


@app.get("/stats")
def get_vision_stats():
    """Estadísticas del servicio de visión"""
    return {
        "mode": settings.MODE,
        "total_detections": service_state["detections_count"],
        "camera_active": service_state["camera_active"],
        "last_detection": service_state["last_detection"],
        "uptime": "running",
    }

# ============================================================
# REEMPLAZA el endpoint /detect/frame en main.py con este:
# ============================================================

@app.post("/detect/frame")
async def detect_from_frame(file: UploadFile = File(...)):
    """
    Recibe un frame y aplica voting para reducir falsos positivos.
    """
    contents = await file.read()
    nparr = np.frombuffer(contents, np.uint8)
    frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

    if frame is None:
        raise HTTPException(status_code=400, detail="No se pudo leer la imagen")

    # Modo DEMO
    if detector.model is None or detector.mode == "DEMO":
        result = detector.simulate_detection()
        return {"detections": [result], "mode": "DEMO"}

    # Detección con voting
    result = detector.detect_with_voting(frame)
    

    if result is None:
        raise HTTPException(status_code=404, detail="No se detectó ningún objeto")

    if result.get("uncertain"):
        raise HTTPException(
            status_code=409,
            detail=f"Detección incierta. Votos: {result.get('votes')}. Reposiciona el objeto e intenta de nuevo."
        )

    # Buscar info del item en el backend
    item_name = result["class_name"]
    try:
        r = requests.get(
            f"{settings.BACKEND_URL}/api/items/{result['item_id']}",
            timeout=3,
        )
        if r.status_code == 200:
            item_name = r.json()["nombre"]
    except:
        pass

    detection = {
        "item_id": result["item_id"],
        "item_name": item_name,
        "confidence": result["confidence"],
        "action": "RETIRO",
        "timestamp": datetime.now().isoformat(),
        "votes": result.get("votes", {}),
    }

    return {
        "detections": [detection],
        "best": detection,
        "total_detected": 1,
        "mode": "CAMERA",
    }
# -------------------------------------------------------
# Iniciar servidor
# -------------------------------------------------------
if __name__ == "__main__":
    import uvicorn

    print("=" * 50)
    print("🧠 MindStock Vision - Microservicio de IA")
    print(f"📡 Modo: {settings.MODE}")
    print(f"🔗 Backend: {settings.BACKEND_URL}")
    print(f"🚀 Iniciando en http://localhost:{settings.VISION_PORT}")
    print("=" * 50)

    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=settings.VISION_PORT,
        reload=True,
    )