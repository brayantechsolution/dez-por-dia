"""Segunda frase de cada palavra.

  python3 alt_sentences.py reuse  <words.json> <out reuse.jsonl> <out missing.txt>
  python3 alt_sentences.py check  <words.json> <pasta com reuse.jsonl e alt_*.jsonl>
  python3 alt_sentences.py merge  <words.json> <pasta>     (grava s2/t2 no words.json)
"""
import glob
import json
import re
import sys

import os
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
from check_words import IRREGULAR, has_form  # noqa: E402


def tok(s):
    return [t.lower().strip("'") for t in re.findall(r"[A-Za-z']+", s)]


def forms(w):
    w = w.lower()
    f = {w, w + "s", w + "es", w + "ed", w + "d", w + "ing", w + "'s"}
    if w.endswith("e"):
        f.add(w[:-1] + "ing")
    if w.endswith("y"):
        f |= {w[:-1] + "ies", w[:-1] + "ied"}
    if re.search(r"[^aeiou][aeiou][bdgmnprt]$", w):
        f |= {w + w[-1] + "ing", w + w[-1] + "ed"}
    f |= set(IRREGULAR.get(w, []))
    return f


def strict_has(w, s):
    if " " in w or "-" in w:
        return re.search(r"\b" + re.escape(w.lower()) + r"\b", s.lower()) is not None
    return bool(forms(w) & set(tok(s)))


def load(p):
    return json.load(open(p, encoding="utf-8"))


def reuse(words_p, out_p, miss_p):
    d = load(words_p)
    used = set()
    with open(out_p, "w", encoding="utf-8") as out, open(miss_p, "w", encoding="utf-8") as miss:
        for i, o in enumerate(d):
            cands = [p for j, p in enumerate(d) if j != i and strict_has(o["w"], p["s"]) and p["s"] not in used]
            if not cands:
                miss.write(json.dumps({"w": o["w"], "pt": o["pt"], "s": o["s"]}, ensure_ascii=False) + "\n")
                continue
            # prefere a frase em que a palavra é a revisão (escolhida a dedo), depois a mais curta
            cands.sort(key=lambda p: (p.get("r", "").lower() != o["w"].lower(), len(p["s"])))
            best = cands[0]
            used.add(best["s"])
            out.write(json.dumps({"w": o["w"], "s2": best["s"], "t2": best["t"]}, ensure_ascii=False) + "\n")


def collect(folder):
    alt = {}
    for fn in sorted(glob.glob(folder + "/reuse.jsonl")) + sorted(glob.glob(folder + "/alt_*.jsonl")):
        for n, line in enumerate(open(fn, encoding="utf-8"), 1):
            if line.strip():
                o = json.loads(line)
                o["_"] = f"{fn.split('/')[-1]}:{n}"
                if o["w"] in alt:
                    o["_dup"] = alt[o["w"]]["_"]
                alt[o["w"]] = o
    return alt


def check(words_p, folder):
    d = load(words_p)
    alt = collect(folder)
    probs = []
    names = {o["w"] for o in d}
    for k in alt:
        if k not in names:
            probs.append(f"{alt[k]['_']} '{k}' não existe no banco")
        if "_dup" in alt[k]:
            probs.append(f"{alt[k]['_']} '{k}' repetida (também em {alt[k]['_dup']})")
    for o in d:
        a = alt.get(o["w"])
        if not a:
            probs.append(f"'{o['w']}' sem segunda frase")
            continue
        s, t = a.get("s2", ""), a.get("t2", "")
        if not s or not t:
            probs.append(f"{a['_']} '{o['w']}' campo vazio")
            continue
        if s.strip().lower() == o["s"].strip().lower():
            probs.append(f"{a['_']} '{o['w']}' igual à primeira frase")
        if not has_form(o["w"], s):
            probs.append(f"{a['_']} '{o['w']}' não aparece na frase: {s}")
        for nm, txt in (("s2", s), ("t2", t)):
            if txt[-1] not in ".?!":
                probs.append(f"{a['_']} '{o['w']}' {nm} sem pontuação final")
            if not (txt[0].isupper() or txt[0] in "\"'0123456789¿¡"):
                probs.append(f"{a['_']} '{o['w']}' {nm} sem maiúscula")
            if "  " in txt or re.search(r"\s[,.!?]", txt):
                probs.append(f"{a['_']} '{o['w']}' {nm} espaçamento")
        for m in re.finditer(r"\b([Aa]n?)\s+([A-Za-z'\-]+)", s):
            art, nx = m.group(1).lower(), m.group(2).lower()
            vow = nx[0] in "aeiou"
            if art == "a" and vow and nx not in {"university", "useful", "user", "uniform", "one", "one-way", "usual", "european"}:
                probs.append(f"{a['_']} '{o['w']}' a + vogal: {s}")
            if art == "an" and not vow and nx not in {"hour", "honest", "hourly"}:
                probs.append(f"{a['_']} '{o['w']}' an + consoante: {s}")
        if len(s) > 66:
            probs.append(f"{a['_']} '{o['w']}' frase longa ({len(s)})")
    print(f"{len(alt)} segundas frases, {len(probs)} problema(s)")
    for p in probs[:80]:
        print("  -", p)
    return probs


def merge(words_p, folder):
    if check(words_p, folder):
        sys.exit("corrija antes de gravar")
    d = load(words_p)
    alt = collect(folder)
    for o in d:
        o["s2"], o["t2"] = alt[o["w"]]["s2"], alt[o["w"]]["t2"]
    with open(words_p, "w", encoding="utf-8") as f:
        f.write("[\n" + ",\n".join(json.dumps(o, ensure_ascii=False) for o in d) + "\n]\n")
    print("gravado", words_p)


if __name__ == "__main__":
    cmd = sys.argv[1]
    {"reuse": reuse, "check": check, "merge": merge}[cmd](*sys.argv[2:])
