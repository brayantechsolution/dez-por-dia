#!/usr/bin/env python3
"""Gera marketing/painel/dados.json a partir dos JSON exportados do painel do claude.ai.

Uso: python3 exportar.py <pasta-do-export>
A pasta é o out_dir usado no ArtifactData (list ... out_dir), com as subpastas
posts/, videos/, ideas/, guide/ e meta/.
"""
import json, sys, pathlib
from datetime import datetime, timezone, timedelta

src = pathlib.Path(sys.argv[1])
here = pathlib.Path(__file__).resolve().parent
mp4s = here.parent / "site" / "videos"

def load(col):
    d = src / col
    out = {}
    if d.is_dir():
        for f in sorted(d.glob("*.json")):
            out[f.stem] = json.loads(f.read_text(encoding="utf-8"))
    return out

posts = [{"id": k, **v} for k, v in load("posts").items()]
posts.sort(key=lambda p: p.get("date", p["id"]))

videos = {}
for k, v in load("videos").items():
    v = {kk: vv for kk, vv in v.items() if kk not in ("asset", "url")}
    v["id"] = k
    v["src"] = f"videos/{k}.mp4" if (mp4s / f"{k}.mp4").exists() else None
    videos[k] = v

ideas = [{"id": k, **v} for k, v in load("ideas").items()]
guide_all = load("guide")
setup = guide_all.pop("setup", None)
guide = sorted(({"id": k, **v} for k, v in guide_all.items()), key=lambda g: g.get("order", 99))
agent = load("meta").get("agent")

brt = timezone(timedelta(hours=-3))
data = {
    "updated": datetime.now(brt).strftime("%d/%m/%Y %H:%M"),
    "agent": agent, "posts": posts, "videos": videos,
    "ideas": ideas, "setup": setup, "guide": guide,
}
(here / "dados.json").write_text(json.dumps(data, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
print(f"dados.json: {len(posts)} posts, {len(videos)} vídeos ({sum(1 for v in videos.values() if v['src'])} com MP4), {len(ideas)} ideias")
