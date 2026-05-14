# ============================================================
# ARCHIVO: mindstock-vision/config.py
#
# Configuración del microservicio de visión
# ============================================================


class Settings:
    # Modo de operación: "DEMO" o "CAMERA"
    MODE: str = "CAMERA"

    # URL del backend Spring Boot
    BACKEND_URL: str = "http://localhost:8080"

    # Puerto del microservicio de visión
    VISION_PORT: int = 8081

    # Confianza mínima para aceptar una detección
    MIN_CONFIDENCE: float = 0.80

    # Ruta al modelo YOLO entrenado (para modo CAMERA)
    YOLO_MODEL_PATH: str = "models/mindstock_model.pt"

    # ID de la cámara (0 = webcam default)
    CAMERA_ID: int = 1

    # Resolución de captura
    CAMERA_WIDTH: int = 1280
    CAMERA_HEIGHT: int = 720


settings = Settings()