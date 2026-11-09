"""Gera a foto de perfil (1080x1080) e o banner do YouTube (2560x1440) do 10 por Dia."""
import os
from playwright.sync_api import sync_playwright
HERE = os.path.dirname(os.path.abspath(__file__))
FONT = "<link href='https://fonts.googleapis.com/css2?family=Inter:wght@500;700;800;900&display=swap' rel='stylesheet'>"
FOTO = FONT + """<body style="margin:0;width:1080px;height:1080px;background:#C6F432;display:grid;place-items:center;font-family:Inter">
<div style="font:900 560px/1 Inter;color:#0F1B2D;letter-spacing:-40px;margin-left:-40px;margin-top:-30px">10</div></body>"""
BANNER = FONT + """<body style="margin:0;width:2560px;height:1440px;background:#0F1B2D;font-family:Inter;color:#fff;position:relative;overflow:hidden">
<div style="position:absolute;inset:0;background:radial-gradient(900px 500px at 50% 50%,rgba(198,244,50,.16),transparent 70%)"></div>
<div style="position:absolute;left:507px;top:508px;width:1546px;height:423px;display:flex;align-items:center;gap:70px">
  <div style="flex:none;width:300px;height:300px;border-radius:78px;background:#C6F432;color:#0F1B2D;display:grid;place-items:center;font:900 170px/1 Inter;letter-spacing:-10px">10</div>
  <div>
    <div style="font:900 128px/1 Inter;letter-spacing:-4px">10 por Dia</div>
    <div style="font:700 62px/1.25 Inter;color:#C6F432;margin-top:22px">10 palavras de inglês por dia<br>na sua tela de bloqueio</div>
    <div style="font:500 40px Inter;color:#A9B4C4;margin-top:20px">Grátis · Sem propaganda · Android</div>
  </div>
</div></body>"""
with sync_playwright() as p:
    b = p.chromium.launch()
    for name, html, w, h in [("foto-perfil.png", FOTO, 1080, 1080), ("banner-youtube.png", BANNER, 2560, 1440)]:
        pg = b.new_page(viewport={"width": w, "height": h})
        pg.set_content(html); pg.wait_for_timeout(1500)
        pg.screenshot(path=os.path.join(HERE, name)); print(name)
    b.close()
