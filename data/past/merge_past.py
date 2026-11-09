"""Frase no passado (rodada de revisão) e passado dos verbos.

  python3 data/past/merge_past.py check <words.json>
  python3 data/past/merge_past.py merge <words.json>   (grava p, s2 e t2 no words.json)

Arquivos lidos desta pasta:
  past.json      palavra -> passado simples (só verbos; gerado por make_past.py)
  frases_*.tsv   palavra <TAB> frase no passado <TAB> tradução
"""
import glob
import json
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, ".."))
from check_words import has_form  # noqa: E402

AUX = {"was", "were", "wasn't", "weren't", "did", "didn't", "had", "hadn't", "could", "couldn't",
       "would", "wouldn't", "used", "hit", "put", "cut", "let", "set", "cost", "hurt", "quit",
       "read", "shut", "spread", "bet", "fit", "hid", "slid", "bit", "lit", "fed", "led", "stole",
       "froze", "dug", "rose", "shot", "tore", "wept", "bent", "fled", "laid", "lay", "sought", "shone",
       "stuck", "struck", "swore", "swept", "forgot", "upset", "spilled", "spilt", "burnt", "rode", "dove", "bled", "sped", "drew"}


MODAL_PAST = {"afford"}


def tok(s):
    return [t.lower() for t in re.findall(r"[A-Za-z']+", s)]


def load_frases():
    out = {}
    probs = []
    for fn in sorted(glob.glob(os.path.join(HERE, "frases_*.tsv"))):
        for n, line in enumerate(open(fn, encoding="utf-8"), 1):
            if not line.strip():
                continue
            parts = line.rstrip("\n").split("\t")
            where = f"{os.path.basename(fn)}:{n}"
            if len(parts) != 3:
                probs.append(f"{where} precisa de 3 colunas")
                continue
            w, s, t = parts
            if w in out:
                probs.append(f"{where} '{w}' repetida (também em {out[w]['_']})")
            out[w] = {"s2": s.strip(), "t2": t.strip(), "_": where}
    return out, probs


def has_past(word, past, s):
    """O passado do verbo aparece na frase (no phrasal verb, o verbo e a partícula)."""
    words = tok(s)
    pw = past.split(" ")
    head = pw[0]
    heads = head.split("/") if "/" in head else [head]
    if not any(h in words for h in heads):
        return False
    return all(p.lower() in words for p in pw[1:] if p not in {"your"})


def check(words_p):
    d = json.load(open(words_p, encoding="utf-8"))
    past = json.load(open(os.path.join(HERE, "past.json"), encoding="utf-8"))
    fr, probs = load_frases()
    names = {o["w"] for o in d}
    all_past = {v.split(" ")[0] for v in past.values()} | AUX
    for k in fr:
        if k not in names:
            probs.append(f"{fr[k]['_']} '{k}' não existe no banco")
    for k in past:
        if k not in names:
            probs.append(f"past.json: '{k}' não existe no banco")
    for o in d:
        w = o["w"]
        a = fr.get(w)
        if not a:
            probs.append(f"'{w}' sem frase no passado")
            continue
        s, t, at = a["s2"], a["t2"], a["_"]
        if not s or not t:
            probs.append(f"{at} '{w}' campo vazio")
            continue
        if s.lower() == o["s"].strip().lower():
            probs.append(f"{at} '{w}' igual à primeira frase")
        if w in past:
            # "afford" quase sempre aparece como "couldn't afford" no passado
            modal_ok = w in MODAL_PAST and ({"could", "couldn't"} & set(tok(s))) and has_form(w, s)
            if not modal_ok and not has_past(w, past[w], s):
                probs.append(f"{at} '{w}' sem o passado '{past[w]}': {s}")
        elif not has_form(w, s):
            probs.append(f"{at} '{w}' não aparece na frase: {s}")
        words = set(tok(s))
        if not (words & all_past or any(x.endswith("ed") for x in words)):
            probs.append(f"{at} '{w}' não parece estar no passado: {s}")
        for nm, txt in (("frase", s), ("tradução", t)):
            if txt[-1] not in ".?!":
                probs.append(f"{at} '{w}' {nm} sem pontuação final")
            if not (txt[0].isupper() or txt[0] in "\"'0123456789¿¡"):
                probs.append(f"{at} '{w}' {nm} sem maiúscula")
            if "  " in txt or re.search(r"\s[,.!?]", txt):
                probs.append(f"{at} '{w}' {nm} espaçamento")
        for m in re.finditer(r"\b([Aa]n?)\s+([A-Za-z'\-]+)", s):
            art, nx = m.group(1).lower(), m.group(2).lower()
            vow = nx[0] in "aeiou"
            if art == "a" and vow and nx not in {"university", "useful", "user", "uniform", "one", "one-way", "usual", "european"}:
                probs.append(f"{at} '{w}' a + vogal: {s}")
            if art == "an" and not vow and nx not in {"hour", "honest", "hourly"}:
                probs.append(f"{at} '{w}' an + consoante: {s}")
        if len(s) > 66:
            probs.append(f"{at} '{w}' frase longa ({len(s)}): {s}")
        if len(t) > 80:
            probs.append(f"{at} '{w}' tradução longa ({len(t)}): {t}")
    print(f"{len(fr)} frases no passado, {len(past)} verbos, {len(probs)} problema(s)")
    for p in probs[:120]:
        print("  -", p)
    return probs


def merge(words_p):
    if check(words_p):
        sys.exit("corrija antes de gravar")
    d = json.load(open(words_p, encoding="utf-8"))
    past = json.load(open(os.path.join(HERE, "past.json"), encoding="utf-8"))
    fr, _ = load_frases()
    for o in d:
        o["s2"] = fr[o["w"]]["s2"]
        o["t2"] = fr[o["w"]]["t2"]
        if o["w"] in past:
            o["p"] = past[o["w"]]
        else:
            o.pop("p", None)
    with open(words_p, "w", encoding="utf-8") as f:
        json.dump(d, f, ensure_ascii=False, separators=(",", ":"))
    print("gravado:", words_p)


if __name__ == "__main__":
    cmd, words_p = sys.argv[1], sys.argv[2]
    if cmd == "check":
        check(words_p)
    elif cmd == "merge":
        merge(words_p)
    else:
        sys.exit(__doc__)
