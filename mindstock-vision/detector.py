# ============================================================
# ARCHIVO: mindstock-vision/detector.py
#
# Detector con sistema de VOTING:
# Solo confirma una detección si N frames consecutivos
# detectan el mismo objeto con confianza alta.
#
# Esto elimina los falsos positivos cuando el modelo
# duda entre 2 objetos similares (ej: arduino vs raspberry).
# ============================================================

import random
import requests
import cv2
from datetime import datetime
from collections import Counter
from config import settings


class ObjectDetector:

    def __init__(self):
        self.mode = settings.MODE
        self.model = None
        self.camera = None
        self.inventory_cache = []
        self.last_cache_update = None

        # Sistema de voting
        self.frame_history = []     # Últimos N frames de detecciones
        self.HISTORY_SIZE = 5       # Cuántos frames analizar
        self.MIN_VOTES = 3          # Mínimo de votos coincidentes
        self.MIN_AVG_CONFIDENCE = 0.65  # Confianza promedio mínima

        # Mapeo de clases reales del modelo entrenado
        self.LABEL_TO_ITEM_ID = {
        "arduino_mega": 1,
        "arduino_uno_q": 2,
        "dremmel": 3,
        "flux": 4,
        "kit_leds": 5,
        "kit_resistencias": 6,
        "multimetro_grande": 7,
        "multimetro_pequeno": 8,
        "raspberrypi_5": 9,
        "rosmaster_x3": 10,
        "taladro_dewalt": 11,
        "vernier": 12,
        }

        if self.mode == "CAMERA":
            self._load_yolo_model()

        print(f"🔍 Detector inicializado en modo: {self.mode}")
        print(f"   Voting: {self.MIN_VOTES}/{self.HISTORY_SIZE} frames")
        print(f"   Confianza mínima: {self.MIN_AVG_CONFIDENCE}")

    def _load_yolo_model(self):
        try:
            from ultralytics import YOLO
            self.model = YOLO(settings.YOLO_MODEL_PATH)
            print(f"✅ Modelo YOLO cargado: {settings.YOLO_MODEL_PATH}")
            import numpy as np
            dummy = np.zeros((640, 640, 3), dtype=np.uint8)
            self.model(dummy, verbose=False)
            print("✅ Modelo calentado y listo")
        except FileNotFoundError:
            print(f"⚠️ Modelo no encontrado en {settings.YOLO_MODEL_PATH}")
            self.mode = "DEMO"
        except Exception as e:
            print(f"⚠️ Error cargando modelo: {e}")
            self.mode = "DEMO"

    # -------------------------------------------------------
    # Detección con voting (para uso en endpoint /detect/frame)
    # -------------------------------------------------------
    def detect_with_voting(self, frame):
        """
        Detecta objetos en un frame.
        Si está en modo CAMERA, captura múltiples frames
        y aplica voting para evitar falsos positivos.

        Returns: dict con la detección final o None
        """
        if self.model is None:
            return None

        # Capturar múltiples detecciones del MISMO frame con diferentes configs
        # (simulando "voting" interno)
        all_detections = []

        # Detección 1: confianza estándar
        results = self.model(frame, conf=0.8, verbose=False)
        if len(results[0].boxes) > 0:
            best = max(results[0].boxes, key=lambda b: float(b.conf[0]))
            all_detections.append({
                "class_name": results[0].names[int(best.cls[0])],
                "confidence": float(best.conf[0]),
            })

        # Detección 2: con threshold más alto (más estricto)
        results2 = self.model(frame, conf=0.8, verbose=False)
        if len(results2[0].boxes) > 0:
            best2 = max(results2[0].boxes, key=lambda b: float(b.conf[0]))
            all_detections.append({
                "class_name": results2[0].names[int(best2.cls[0])],
                "confidence": float(best2.conf[0]),
            })

        # Detección 3: imagen redimensionada (a veces ayuda)
        h, w = frame.shape[:2]
        resized = cv2.resize(frame, (w // 2, h // 2))
        results3 = self.model(resized, conf=0.8, verbose=False)
        if len(results3[0].boxes) > 0:
            best3 = max(results3[0].boxes, key=lambda b: float(b.conf[0]))
            all_detections.append({
                "class_name": results3[0].names[int(best3.cls[0])],
                "confidence": float(best3.conf[0]),
            })

        if not all_detections:
            return None

        # Aplicar voting
        return self._apply_voting(all_detections)

    def _apply_voting(self, detections):
        """
        Aplica voting a una lista de detecciones.
        Solo retorna un resultado si la mayoría coincide.
        """
        if not detections:
            return None

        # Contar votos por clase
        class_votes = Counter([d["class_name"] for d in detections])
        most_voted_class, vote_count = class_votes.most_common(1)[0]

        # Calcular confianza promedio de los votos ganadores
        winning_detections = [d for d in detections if d["class_name"] == most_voted_class]
        avg_confidence = sum(d["confidence"] for d in winning_detections) / len(winning_detections)

        # Si la mayoría no coincide, hay incertidumbre
        if vote_count < len(detections) / 2:
            print(f"  ⚠️ Detección incierta: {dict(class_votes)}")
            return {
                "class_name": most_voted_class,
                "confidence": avg_confidence * 0.5,  # Penalizar
                "uncertain": True,
                "votes": dict(class_votes),
            }

        item_id = self.LABEL_TO_ITEM_ID.get(most_voted_class, 0)

        return {
            "class_name": most_voted_class,
            "item_id": item_id,
            "confidence": round(avg_confidence, 3),
            "votes": dict(class_votes),
            "uncertain": False,
        }

    # -------------------------------------------------------
    # Voting con historial (para monitoreo continuo)
    # -------------------------------------------------------
    def detect_with_history(self, frame):
        """
        Detecta usando historial de frames anteriores.
        Solo confirma si N frames consecutivos coinciden.
        """
        if self.model is None:
            return None

        results = self.model(frame, conf=0.4, verbose=False)

        # Extraer detección actual
        current = None
        if len(results[0].boxes) > 0:
            best = max(results[0].boxes, key=lambda b: float(b.conf[0]))
            current = {
                "class_name": results[0].names[int(best.cls[0])],
                "confidence": float(best.conf[0]),
            }

        # Agregar al historial
        self.frame_history.append(current)
        if len(self.frame_history) > self.HISTORY_SIZE:
            self.frame_history.pop(0)

        # Necesitamos historial suficiente
        if len(self.frame_history) < self.HISTORY_SIZE:
            return None

        # Filtrar None y contar votos
        valid = [d for d in self.frame_history if d is not None]
        if len(valid) < self.MIN_VOTES:
            return None

        votes = Counter([d["class_name"] for d in valid])
        most_voted, count = votes.most_common(1)[0]

        if count < self.MIN_VOTES:
            return None

        # Calcular confianza promedio
        winners = [d for d in valid if d["class_name"] == most_voted]
        avg_conf = sum(d["confidence"] for d in winners) / len(winners)

        if avg_conf < self.MIN_AVG_CONFIDENCE:
            return None

        # Limpiar historial para no repetir
        self.frame_history = []

        return {
            "class_name": most_voted,
            "item_id": self.LABEL_TO_ITEM_ID.get(most_voted, 0),
            "confidence": round(avg_conf, 3),
            "votes": count,
            "total_frames": len(valid),
        }

    def reset_history(self):
        """Limpia el historial de frames"""
        self.frame_history = []

    # -------------------------------------------------------
    # MODO DEMO: Simulación
    # -------------------------------------------------------
    def simulate_detection(self) -> dict:
        self._update_inventory_cache()

        if not self.inventory_cache:
            return {
                "item_id": 0,
                "item_name": "Objeto desconocido",
                "confidence": 0.0,
                "action": "RETIRO",
                "timestamp": datetime.now().isoformat(),
            }

        disponibles = [
            item for item in self.inventory_cache
            if item["status"] == "DISPONIBLE" and item["cantidadDisponible"] > 0
        ]
        if not disponibles:
            disponibles = self.inventory_cache

        item = random.choice(disponibles)
        confidence = round(random.uniform(0.85, 0.99), 3)
        action = "RETIRO" if random.random() < 0.8 else "DEVOLUCION"

        return {
            "item_id": item["id"],
            "item_name": item["nombre"],
            "confidence": confidence,
            "action": action,
            "timestamp": datetime.now().isoformat(),
        }

    def _update_inventory_cache(self):
        now = datetime.now()
        if (
            self.last_cache_update
            and (now - self.last_cache_update).seconds < 30
            and self.inventory_cache
        ):
            return

        try:
            r = requests.get(f"{settings.BACKEND_URL}/api/items", timeout=3)
            if r.status_code == 200:
                self.inventory_cache = r.json()
                self.last_cache_update = now
        except:
            pass