"""Renderiza um vídeo (HTML com render(t)) em MP4 9:16, ou só alguns quadros para conferir.

Uso:
  python3 render.py v1.html out.mp4            # vídeo completo
  python3 render.py v1.html frames 1.0 4.0 9.5 # quadros JPEG nesses segundos
"""
import os
import subprocess
import sys

from playwright.sync_api import sync_playwright

FPS = 30
HERE = os.path.dirname(os.path.abspath(__file__))


def main():
    html, target = sys.argv[1], sys.argv[2]
    with sync_playwright() as p:
        browser = p.chromium.launch()
        page = browser.new_page(viewport={"width": 1080, "height": 1920}, device_scale_factor=1)
        page.goto("file://" + os.path.join(HERE, html))
        page.wait_for_timeout(400)
        duration = page.evaluate("DURATION")

        if target == "frames":
            os.makedirs(os.path.join(HERE, "frames"), exist_ok=True)
            base = os.path.splitext(html)[0]
            for s in sys.argv[3:]:
                page.evaluate(f"render({float(s)})")
                out = os.path.join(HERE, "frames", f"{base}_{s}.jpg")
                page.screenshot(path=out, type="jpeg", quality=90)
                print(out)
            browser.close()
            return

        os.makedirs(os.path.dirname(os.path.abspath(target)), exist_ok=True)
        tmp = target[:-4] + ".part.mp4"
        cmd = [
            "ffmpeg", "-y", "-loglevel", "error",
            "-f", "image2pipe", "-framerate", str(FPS), "-c:v", "mjpeg", "-i", "-",
            "-f", "lavfi", "-i", "anullsrc=r=44100:cl=stereo",
            "-c:v", "libx264", "-crf", "18", "-preset", "veryfast", "-pix_fmt", "yuv420p",
            "-c:a", "aac", "-b:a", "96k", "-shortest", "-movflags", "+faststart", tmp,
        ]
        ff = subprocess.Popen(cmd, stdin=subprocess.PIPE)
        frames = int(round(duration * FPS))
        for i in range(frames):
            page.evaluate(f"render({i / FPS})")
            ff.stdin.write(page.screenshot(type="jpeg", quality=92))
        ff.stdin.close()
        code = ff.wait()
        browser.close()
        if code != 0:
            sys.exit(f"ffmpeg falhou ({code})")
        os.replace(tmp, target)
        print("ok", target, frames, "quadros")


if __name__ == "__main__":
    main()
