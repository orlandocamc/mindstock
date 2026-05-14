# ============================================================
# ARCHIVO: mindstock-vision/test_model.py
#
# Versión con matching inteligente de nombres:
# - "arduino" en carpeta acepta "arduino_mega" o "arduino_uno_q"
# - "raspberry_pi" acepta "raspberrypi_5"
# - "rosmasterx3" acepta "rosmaster_x3"
# - etc.
# ============================================================

import sys
import os
import cv2
from ultralytics import YOLO

MODEL_PATH = "models/mindstock_model.pt"
CONFIDENCE = 0.25


# Mapeo: nombre de carpeta → posibles clases del modelo que se aceptan
FOLDER_TO_VALID_CLASSES = {
    "arduino":             ["arduino_mega", "arduino_uno_q"],
    "arduino_mega":        ["arduino_mega"],
    "arduinounoq":         ["arduino_uno_q"],
    "arduino_uno_q":       ["arduino_uno_q"],
    "dremel":              ["dremmel"],
    "dremmel":             ["dremmel"],
    "flux":                ["flux"],
    "kit_leds":            ["kit_leds"],
    "kit_resistencias":    ["kit_resistencias"],
    "multimetro":          ["multimetro_grande"],
    "multimetro_grande":   ["multimetro_grande"],
    "multimetro_portable": ["multimetro_pequeno"],
    "multimetro_pequeno":  ["multimetro_pequeno"],
    "raspberry_pi":        ["raspberrypi_5"],
    "raspberrypi_5":       ["raspberrypi_5"],
    "rosmasterx3":         ["rosmaster_x3"],
    "rosmaster_x3":        ["rosmaster_x3"],
    "taladro":             ["taladro_dewalt"],
    "taladro_dewalt":      ["taladro_dewalt"],
    "vernier":             ["vernier"],
}


def is_correct_detection(folder_name, detected_class):
    """Verifica si la clase detectada es válida para esa carpeta"""
    valid_classes = FOLDER_TO_VALID_CLASSES.get(folder_name, [folder_name])
    return detected_class in valid_classes


def load_model():
    if not os.path.exists(MODEL_PATH):
        print(f"❌ No se encontró el modelo en {MODEL_PATH}")
        sys.exit(1)
    print(f"📦 Cargando modelo: {MODEL_PATH}")
    model = YOLO(MODEL_PATH)
    print(f"✅ Modelo cargado")
    print(f"📋 Clases que conoce: {list(model.names.values())}\n")
    return model


def test_with_camera(model):
    print("=" * 60)
    print("🎥 PRUEBA EN VIVO CON CÁMARA")
    print("=" * 60)
    print("  ESPACIO → Pausar/Continuar | S → Guardar frame | Q → Salir")
    print("=" * 60)

    cap = cv2.VideoCapture(1)
    if not cap.isOpened():
        cap = cv2.VideoCapture(0)
    cap.set(cv2.CAP_PROP_FRAME_WIDTH, 1280)
    cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 720)

    paused = False
    frame_count = 0

    while cap.isOpened():
        if not paused:
            ret, frame = cap.read()
            if not ret:
                break
            frame_count += 1

        results = model(frame, conf=CONFIDENCE, verbose=False)
        annotated = results[0].plot()

        h, w = annotated.shape[:2]
        cv2.rectangle(annotated, (0, 0), (w, 60), (20, 20, 20), -1)
        cv2.putText(annotated, "MindStock - Test del Modelo YOLO",
                    (15, 25), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 150, 255), 2)
        cv2.putText(annotated, f"Confianza minima: {CONFIDENCE} | {'PAUSADO' if paused else 'EN VIVO'}",
                    (15, 50), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (180, 180, 180), 1)

        if not paused and results[0].boxes is not None and len(results[0].boxes) > 0:
            print(f"\n--- Frame {frame_count} ---")
            for box in results[0].boxes:
                cls_id = int(box.cls[0])
                conf = float(box.conf[0])
                name = results[0].names[cls_id]
                print(f"  • {name}: {conf:.1%}")

        cv2.imshow("MindStock - Test", annotated)

        key = cv2.waitKey(1) & 0xFF
        if key == ord('q'):
            break
        elif key == ord(' '):
            paused = not paused
        elif key == ord('s'):
            filename = f"test_frame_{frame_count}.jpg"
            cv2.imwrite(filename, frame)
            print(f"\n💾 Frame guardado: {filename}")

    cap.release()
    cv2.destroyAllWindows()


def test_with_dataset(model):
    print("=" * 60)
    print("📸 PRUEBA CON FOTOS DEL DATASET (matching inteligente)")
    print("=" * 60)

    raw_dir = "dataset/raw"
    if not os.path.exists(raw_dir):
        print(f"❌ No existe la carpeta {raw_dir}")
        return

    correct = 0
    total = 0
    confusions = {}
    no_detection = 0

    for class_folder in sorted(os.listdir(raw_dir)):
        folder_path = os.path.join(raw_dir, class_folder)
        if not os.path.isdir(folder_path):
            continue

        images = [f for f in os.listdir(folder_path)
                  if f.lower().endswith(('.jpg', '.jpeg', '.png'))]

        if not images:
            continue

        # Mostrar qué se considera "válido" para esta carpeta
        valid_classes = FOLDER_TO_VALID_CLASSES.get(class_folder, [class_folder])
        valid_str = " o ".join(valid_classes)
        print(f"\n📂 {class_folder} ({len(images)} fotos) → acepta: {valid_str}")

        folder_correct = 0
        folder_total = 0

        for img_name in images:
            img_path = os.path.join(folder_path, img_name)
            results = model(img_path, conf=CONFIDENCE, verbose=False)
            total += 1
            folder_total += 1

            if len(results[0].boxes) == 0:
                print(f"  ⚠️  {img_name}: SIN DETECCIÓN")
                no_detection += 1
                continue

            best_box = max(results[0].boxes, key=lambda b: float(b.conf[0]))
            detected = results[0].names[int(best_box.cls[0])]
            conf = float(best_box.conf[0])

            if is_correct_detection(class_folder, detected):
                correct += 1
                folder_correct += 1
                print(f"  ✅ {img_name}: {detected} ({conf:.1%})")
            else:
                print(f"  ❌ {img_name}: detectó '{detected}' ({conf:.1%})")
                key = f"{class_folder} → {detected}"
                confusions[key] = confusions.get(key, 0) + 1

        # Resumen por carpeta
        if folder_total > 0:
            pct = folder_correct / folder_total * 100
            print(f"  📊 {class_folder}: {folder_correct}/{folder_total} = {pct:.0f}%")

    # Resumen final
    print("\n" + "=" * 60)
    print("📊 RESULTADOS GLOBALES")
    print("=" * 60)
    if total > 0:
        accuracy = correct / total * 100
        print(f"  ✅ Aciertos: {correct}/{total} = {accuracy:.1f}%")
        if no_detection > 0:
            print(f"  ⚠️  Sin detección: {no_detection}")

    if confusions:
        print(f"\n  Confusiones reales:")
        for k, v in sorted(confusions.items(), key=lambda x: -x[1]):
            print(f"    • {k}: {v} veces")
    else:
        print(f"\n  🎉 Sin confusiones — el modelo está PERFECTO")
    print("=" * 60)


def test_single_image(model, image_path):
    if not os.path.exists(image_path):
        print(f"❌ No existe: {image_path}")
        return

    print(f"🔍 Analizando: {image_path}")
    results = model(image_path, conf=CONFIDENCE, verbose=False)

    if len(results[0].boxes) == 0:
        print("  ❌ Sin detecciones")
        return

    print(f"\n📋 Detectados ({len(results[0].boxes)}):")
    for box in results[0].boxes:
        cls_id = int(box.cls[0])
        conf = float(box.conf[0])
        name = results[0].names[cls_id]
        print(f"  • {name}: {conf:.1%}")

    annotated = results[0].plot()
    output_path = "test_result.jpg"
    cv2.imwrite(output_path, annotated)
    print(f"\n💾 Resultado guardado: {output_path}")

    cv2.imshow("Resultado", annotated)
    print("  Presiona cualquier tecla para cerrar")
    cv2.waitKey(0)
    cv2.destroyAllWindows()


def main():
    model = load_model()
    args = sys.argv[1:]

    if "--images" in args:
        test_with_dataset(model)
    elif "--image" in args:
        idx = args.index("--image")
        if idx + 1 < len(args):
            test_single_image(model, args[idx + 1])
        else:
            print("❌ Falta el path de la imagen")
    else:
        test_with_camera(model)


if __name__ == "__main__":
    main()