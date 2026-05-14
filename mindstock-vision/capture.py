# ============================================================
# ARCHIVO: mindstock-vision/capture.py
#
# Captura SOLO 10-15 fotos por objeto.
# El script de augmentación se encarga del resto.
#
# USO: python capture.py
#
# CONTROLES:
#   ESPACIO  → Tomar foto
#   N        → Siguiente objeto
#   P        → Objeto anterior
#   Q        → Salir
#
# TIPS:
#   Solo necesitas 10-15 fotos por objeto, pero varía:
#   - 3-4 fotos de frente
#   - 2-3 fotos de lado
#   - 2-3 fotos de arriba
#   - 2-3 fotos con la mano sosteniéndolo
#   - 1-2 fotos de lejos
# ============================================================

import cv2
import os
from datetime import datetime

OBJECTS = [
    {"id": 1,  "name": "taladro",        "label": "Taladro Dewalt"},
    {"id": 2,  "name": "multimetro",           "label": "Multímetro Fluke 117"},
    {"id": 3,  "name": "arduino",              "label": "Arduino Mega 2560"},
    {"id": 4,  "name": "dremel",   "label": "Multiherramienta Modular Dremmel"},
    {"id": 5,  "name": "kit_resistencias",     "label": "Kit de resistencias 1/4W"},
    {"id": 6,  "name": "kit_leds",         "label": "Kit de Leds - Varios Colores 500 piezas"},
    {"id": 7,  "name": "multimetro_portable",           "label": "Multimetro Portable AC Clamp"},
    {"id": 8,  "name": "flux",          "label": "Flux Pasta para Soldar"},
    {"id": 9,  "name": "arduinounoq",       "label": "Arduino Uno Q 4GB"},
    {"id": 10, "name": "rosmasterx3",    "label": "Robot ROSMASTERX3 Omnidireccional"},
    {"id": 11, "name": "vernier",              "label": "Vernier digital 150mm"},
    {"id": 12, "name": "raspberry_pi",         "label": "Raspberry Pi 4 Model B 8GB"},
]

BASE_DIR = "dataset/raw"


def create_directories():
    for obj in OBJECTS:
        os.makedirs(os.path.join(BASE_DIR, obj["name"]), exist_ok=True)
    print(f"📁 Carpetas creadas en: {BASE_DIR}/")


def capture_photos():
    cap = cv2.VideoCapture(1)
    cap.set(cv2.CAP_PROP_FRAME_WIDTH, 1280)
    cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 720)

    if not cap.isOpened():
        print("❌ No se pudo abrir la cámara")
        return

    current_idx = 0
    photo_counts = {}
    for obj in OBJECTS:
        path = os.path.join(BASE_DIR, obj["name"])
        if os.path.exists(path):
            photo_counts[obj["name"]] = len([f for f in os.listdir(path) if f.endswith(('.jpg', '.png'))])
        else:
            photo_counts[obj["name"]] = 0

    print("\n" + "=" * 60)
    print("📷 MindStock - Captura rápida (10-15 fotos por objeto)")
    print("=" * 60)
    print("  ESPACIO → Foto  |  N → Siguiente  |  P → Anterior  |  Q → Salir")
    print("=" * 60)

    while True:
        obj = OBJECTS[current_idx]
        count = photo_counts.get(obj["name"], 0)

        ret, frame = cap.read()
        if not ret:
            break

        display = frame.copy()
        h, w = frame.shape[:2]

        # Header
        cv2.rectangle(display, (0, 0), (w, 70), (20, 20, 20), -1)
        cv2.putText(display, f"[{current_idx+1}/{len(OBJECTS)}] {obj['label']}",
                    (20, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 150, 255), 2)

        # Progreso
        target = 12
        progress_text = f"Fotos: {count}/{target}"
        color = (0, 255, 0) if count >= target else (0, 200, 255)
        cv2.putText(display, progress_text, (20, 58), cv2.FONT_HERSHEY_SIMPLEX, 0.6, color, 2)

        if count >= target:
            cv2.putText(display, "LISTO! Presiona N para siguiente",
                        (300, 58), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 1)

        # Centro de referencia
        cv2.rectangle(display, (w//4, h//4), (3*w//4, 3*h//4), (0, 150, 255), 2)

        cv2.imshow("MindStock Capture", display)
        key = cv2.waitKey(1) & 0xFF

        if key == ord(' '):
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S_%f")
            filename = f"{obj['name']}_{timestamp}.jpg"
            filepath = os.path.join(BASE_DIR, obj["name"], filename)
            cv2.imwrite(filepath, frame)
            count += 1
            photo_counts[obj["name"]] = count
            print(f"  📸 [{obj['name']}] Foto {count}")

        elif key == ord('n'):
            current_idx = (current_idx + 1) % len(OBJECTS)
            print(f"\n→ {OBJECTS[current_idx]['label']}")

        elif key == ord('p'):
            current_idx = (current_idx - 1) % len(OBJECTS)
            print(f"\n← {OBJECTS[current_idx]['label']}")

        elif key == ord('q'):
            break

    cap.release()
    cv2.destroyAllWindows()

    # Resumen
    print("\n" + "=" * 60)
    print("📊 RESUMEN")
    print("=" * 60)
    total = 0
    for obj in OBJECTS:
        count = photo_counts.get(obj["name"], 0)
        status = "✅" if count >= 10 else "⚠️" if count > 0 else "❌"
        print(f"  {status} {obj['label']}: {count} fotos")
        total += count
    print(f"\n  Total: {total} fotos")
    print(f"\n  Siguiente paso:")
    print(f"  1. Comprime la carpeta dataset/raw en un ZIP")
    print(f"  2. Súbelo a Google Drive")
    print(f"  3. Abre el notebook de Colab para entrenar")
    print("=" * 60)


if __name__ == "__main__":
    create_directories()
    capture_photos()