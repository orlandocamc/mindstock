# ============================================================
# ARCHIVO: mindstock-vision/download_images.py
#
# Versión con manejo de rate limit:
# - Pausa larga entre clases (15 segundos)
# - Reintenta hasta 3 veces si DuckDuckGo bloquea
# - Salta clases que ya tienen suficientes fotos
# ============================================================

import os
import sys
import time
import requests
from PIL import Image
from io import BytesIO

try:
    from ddgs import DDGS
except ImportError:
    from duckduckgo_search import DDGS

SEARCH_QUERIES = {
    "arduino_mega": [
        "arduino mega 2560 board",
        "arduino mega top view",
        "arduino mega blue pcb",
    ],
    "arduino_uno_q": [
        "arduino uno r3 board",
        "arduino uno top view",
        "arduino uno blue board",
    ],
    "dremmel": [
        "dremel rotary tool",
        "dremel multi tool handheld",
        "dremel cordless rotary",
    ],
    "flux": [
        "soldering flux paste",
        "rosin flux solder",
        "flux pen electronics",
    ],
    "kit_leds": [
        "led kit assortment electronics",
        "led pack different colors",
        "led component kit",
    ],
    "kit_resistencias": [
        "resistor kit assorted",
        "resistor pack box",
        "1/4w resistor kit",
    ],
    "multimetro_grande": [
        "fluke multimeter benchtop",
        "digital multimeter large",
        "multimeter true rms",
    ],
    "multimetro_pequeno": [
        "pocket multimeter handheld",
        "small digital multimeter",
        "compact multimeter",
    ],
    "raspberrypi_5": [
        "raspberry pi 5 board",
        "raspberry pi 5 top view",
        "raspberry pi 5 single board computer",
    ],
    "rosmaster_x3": [
        "rosmaster x3 robot",
        "yahboom rosmaster",
        "ros mecanum robot car",
    ],
    "taladro_dewalt": [
        "dewalt cordless drill",
        "dewalt drill yellow black",
        "dewalt power drill 20v",
    ],
    "vernier": [
        "digital vernier caliper",
        "digital caliper measurement",
        "vernier caliper 150mm",
    ],
}

OUTPUT_DIR = "dataset/internet"
IMAGES_PER_QUERY = 10
MIN_SIZE = 200
MAX_SIZE = 1920
TARGET_PER_CLASS = 25      # Si ya hay 25+ imágenes, salta esa clase
PAUSE_BETWEEN_CLASSES = 15 # Segundos entre clases
PAUSE_BETWEEN_QUERIES = 5  # Segundos entre queries
MAX_RETRIES = 3            # Reintentos si rate-limit


def download_image(url, save_path, timeout=10):
    try:
        headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        }
        response = requests.get(url, timeout=timeout, headers=headers)
        if response.status_code != 200:
            return False
        img = Image.open(BytesIO(response.content))
        if img.width < MIN_SIZE or img.height < MIN_SIZE:
            return False
        if img.mode != "RGB":
            img = img.convert("RGB")
        if max(img.size) > MAX_SIZE:
            ratio = MAX_SIZE / max(img.size)
            new_size = (int(img.width * ratio), int(img.height * ratio))
            img = img.resize(new_size, Image.LANCZOS)
        img.save(save_path, "JPEG", quality=88)
        return True
    except Exception:
        return False


def search_with_retry(query, max_results):
    """Busca con reintentos si hay rate limit"""
    for attempt in range(MAX_RETRIES):
        try:
            with DDGS() as ddgs:
                results = list(ddgs.images(
                    query,
                    max_results=max_results,
                    safesearch="moderate",
                    size="Medium",
                ))
            return results
        except Exception as e:
            error_msg = str(e)
            if "Ratelimit" in error_msg or "403" in error_msg:
                wait = 30 * (attempt + 1)
                print(f"    ⏳ Rate limit detectado, esperando {wait}s... (intento {attempt + 1}/{MAX_RETRIES})")
                time.sleep(wait)
            else:
                print(f"    ⚠️ Error: {error_msg[:80]}")
                return []
    print(f"    ❌ Falló después de {MAX_RETRIES} intentos")
    return []


def download_class(class_name, queries):
    output_dir = os.path.join(OUTPUT_DIR, class_name)
    os.makedirs(output_dir, exist_ok=True)

    existing = len([f for f in os.listdir(output_dir) if f.lower().endswith(('.jpg', '.jpeg', '.png'))])
    print(f"\n📂 {class_name} (ya tenía {existing} imágenes)")

    if existing >= TARGET_PER_CLASS:
        print(f"  ⏭️  Ya tiene {existing} imágenes, saltando...")
        return 0

    downloaded = 0
    failed = 0

    for query in queries:
        print(f"  🔍 Buscando: '{query}'")
        results = search_with_retry(query, IMAGES_PER_QUERY)

        if not results:
            time.sleep(PAUSE_BETWEEN_QUERIES)
            continue

        for i, result in enumerate(results):
            url = result.get("image")
            if not url:
                continue

            filename = f"web_{class_name}_{int(time.time())}_{i:02d}.jpg"
            save_path = os.path.join(output_dir, filename)

            if download_image(url, save_path):
                downloaded += 1
                print(f"    ✅ {filename}")
            else:
                failed += 1

            time.sleep(0.3)

        # Pausa entre queries para no irritar a DuckDuckGo
        time.sleep(PAUSE_BETWEEN_QUERIES)

    print(f"  📊 {class_name}: {downloaded} descargadas, {failed} fallidas")
    return downloaded


def main():
    print("=" * 60)
    print("🌐 DESCARGA DE IMÁGENES DE INTERNET (con anti-ratelimit)")
    print("=" * 60)

    os.makedirs(OUTPUT_DIR, exist_ok=True)

    if len(sys.argv) > 1:
        target_class = sys.argv[1]
        if target_class not in SEARCH_QUERIES:
            print(f"❌ Clase desconocida: {target_class}")
            print(f"   Disponibles: {list(SEARCH_QUERIES.keys())}")
            return
        classes_to_download = {target_class: SEARCH_QUERIES[target_class]}
    else:
        classes_to_download = SEARCH_QUERIES

    total = 0
    classes_list = list(classes_to_download.items())

    for idx, (class_name, queries) in enumerate(classes_list):
        total += download_class(class_name, queries)

        # Pausa larga entre clases (excepto la última)
        if idx < len(classes_list) - 1:
            print(f"\n  ⏸️  Pausa de {PAUSE_BETWEEN_CLASSES}s antes de siguiente clase...")
            time.sleep(PAUSE_BETWEEN_CLASSES)

    print("\n" + "=" * 60)
    print(f"✅ TOTAL DESCARGADO EN ESTA EJECUCIÓN: {total} imágenes")
    print("=" * 60)

    # Resumen de cuántas hay por clase
    print("\n📊 RESUMEN POR CLASE:")
    for class_name in SEARCH_QUERIES.keys():
        path = os.path.join(OUTPUT_DIR, class_name)
        if os.path.exists(path):
            count = len([f for f in os.listdir(path) if f.lower().endswith(('.jpg', '.jpeg', '.png'))])
            status = "✅" if count >= TARGET_PER_CLASS else "⚠️" if count > 0 else "❌"
            print(f"  {status} {class_name}: {count} imágenes")

    print(f"\n💡 TIP: Si una clase quedó incompleta, ejecuta:")
    print(f"   python download_images.py <nombre_clase>")
    print(f"   Ejemplo: python download_images.py arduino_mega")


if __name__ == "__main__":
    main()