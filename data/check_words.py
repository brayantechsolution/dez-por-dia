"""Junta os blocos data/bNN.jsonl e confere o banco de palavras.

Uso: python3 check_words.py <pasta data> [--write <words.json>]
"""
import glob
import json
import re
import sys

# Formas irregulares aceitas na frase (palavra -> formas)
IRREGULAR = {
    "buy": ["bought"], "bring": ["brought"], "think": ["thought"], "teach": ["taught"],
    "catch": ["caught"], "fight": ["fought"], "seek": ["sought"], "go": ["went", "gone", "goes", "going"],
    "be": ["is", "are", "was", "were", "been", "am"], "have": ["has", "had"],
    "do": ["does", "did", "done"], "make": ["made"], "take": ["took", "taken"],
    "give": ["gave", "given"], "see": ["saw", "seen"], "come": ["came"], "get": ["got", "gotten"],
    "know": ["knew", "known"], "find": ["found"], "tell": ["told"], "say": ["said"],
    "leave": ["left"], "feel": ["felt"], "keep": ["kept"], "sleep": ["slept"],
    "meet": ["met"], "pay": ["paid"], "lose": ["lost"], "send": ["sent"], "spend": ["spent"],
    "build": ["built"], "lend": ["lent"], "sell": ["sold"], "hold": ["held"], "stand": ["stood"],
    "understand": ["understood"], "run": ["ran"], "begin": ["began", "begun"],
    "drink": ["drank", "drunk"], "swim": ["swam"], "sing": ["sang", "sung"], "ring": ["rang"],
    "write": ["wrote", "written"], "drive": ["drove", "driven"], "ride": ["rode", "ridden"],
    "eat": ["ate", "eaten"], "fall": ["fell", "fallen"], "forget": ["forgot", "forgotten"],
    "choose": ["chose", "chosen"], "speak": ["spoke", "spoken"], "break": ["broke", "broken"],
    "wake": ["woke", "woken"], "steal": ["stole", "stolen"], "freeze": ["froze", "frozen"],
    "wear": ["wore", "worn"], "tear": ["tore", "torn"], "grow": ["grew", "grown"],
    "throw": ["threw", "thrown"], "fly": ["flew", "flown", "flies"], "draw": ["drew", "drawn"],
    "hide": ["hid", "hidden"], "bite": ["bit", "bitten"], "shake": ["shook", "shaken"],
    "lie": ["lay", "lain", "lying"], "lay": ["laid"], "feed": ["fed"], "lead": ["led"],
    "hear": ["heard"], "mean": ["meant"], "dream": ["dreamt", "dreamed"],
    "light": ["lit"], "win": ["won"], "sit": ["sat"], "dig": ["dug"], "hang": ["hung"],
    "stick": ["stuck"], "strike": ["struck"], "shoot": ["shot"], "bleed": ["bled"],
    "child": ["children"], "person": ["people"], "man": ["men"], "woman": ["women"],
    "foot": ["feet"], "tooth": ["teeth"], "mouse": ["mice"], "good": ["better", "best"],
    "bad": ["worse", "worst"], "far": ["farther", "further"], "die": ["dying"], "cry": ["cries", "cried"], "try": ["tries", "tried"], "dry": ["dries", "dried"],
    "tie": ["tying"], "rise": ["rose", "risen"], "arise": ["arose"], "forgive": ["forgave", "forgiven"],
    "become": ["became"], "overcome": ["overcame"], "withdraw": ["withdrew", "withdrawn"],
    "undergo": ["underwent"], "deal": ["dealt"], "burn": ["burnt"], "learn": ["learnt"],
    "spell": ["spelt"], "bend": ["bent"], "bet": ["bet"], "split": ["split"],
    "knife": ["knives"], "leaf": ["leaves"], "wife": ["wives"], "life": ["lives"],
    "shelf": ["shelves"], "half": ["halves"], "thief": ["thieves"], "wolf": ["wolves"],
}

WORD_RE = re.compile(r"[a-zA-Z']+")


def tokens(s):
    return [t.lower().strip("'") for t in WORD_RE.findall(s)]


def has_form(word, sentence):
    """A palavra (ou cada parte dela) aparece na frase, com flexões comuns."""
    toks = tokens(sentence)
    parts = [p for p in re.split(r"[\s\-/]+", word.lower()) if p]
    for p in parts:
        p = p.strip("'")
        if p in ("one's", "someone", "something", "sb", "sth"):
            continue
        forms = set(IRREGULAR.get(p, []))
        base = p
        stems = {base}
        if len(base) > 3:
            if base.endswith("e"):
                stems.add(base[:-1])
            if base.endswith("y"):
                stems.add(base[:-1] + "i")
            stems.add(base)
        ok = False
        for t in toks:
            if t in forms or t == base:
                ok = True
                break
            for st in stems:
                if len(st) >= 3 and t.startswith(st) and len(t) - len(st) <= 4:
                    ok = True
                    break
            if ok:
                break
        if not ok:
            return False
    return True


def main():
    folder = sys.argv[1]
    files = sorted(glob.glob(folder + "/b*.jsonl"))
    entries = []
    problems = []
    for fn in files:
        with open(fn, encoding="utf-8") as f:
            for ln, line in enumerate(f, 1):
                line = line.strip()
                if not line:
                    continue
                try:
                    o = json.loads(line)
                except Exception as e:  # noqa: BLE001
                    problems.append(f"{fn}:{ln} JSON inválido: {e}")
                    continue
                o["_src"] = f"{fn.split('/')[-1]}:{ln}"
                entries.append(o)

    seen = {}
    seen_sentences = {}
    for i, o in enumerate(entries):
        day = i // 10 + 1
        src = o["_src"]
        for k in ("w", "pt", "s", "t", "e"):
            if not str(o.get(k, "")).strip():
                problems.append(f"{src} '{o.get('w')}' campo vazio: {k}")
        w = o.get("w", "").strip()
        key = w.lower()
        if key in seen:
            problems.append(f"{src} '{w}' repetida (já no dia {seen[key][0]})")
        else:
            seen[key] = (day, i)
        s, t = o.get("s", ""), o.get("t", "")
        sk = s.strip().lower()
        if sk in seen_sentences:
            problems.append(f"{src} '{w}' frase repetida (igual à de '{seen_sentences[sk]}')")
        else:
            seen_sentences[sk] = w
        for name, txt in (("s", s), ("t", t)):
            if txt and txt[-1] not in ".?!":
                problems.append(f"{src} '{w}' {name} sem pontuação final")
            if txt and not (txt[0].isupper() or txt[0] in "¿¡\"'0123456789"):
                problems.append(f"{src} '{w}' {name} não começa com maiúscula")
            if "  " in txt:
                problems.append(f"{src} '{w}' {name} com espaço duplo")
        if s and not has_form(w, s):
            problems.append(f"{src} '{w}' não aparece na frase: {s}")
        r = o.get("r", "").strip()
        if r:
            prev = seen.get(r.lower())
            if not prev or prev[1] >= i:
                problems.append(f"{src} '{w}' revisão '{r}' não é de uma palavra anterior")
            elif not has_form(r, s):
                problems.append(f"{src} '{w}' revisão '{r}' não aparece na frase: {s}")

    reviews = sum(1 for o in entries if o.get("r"))
    print(f"{len(entries)} palavras, {len(entries) // 10} dias completos, "
          f"{reviews} com revisão, {len(problems)} problema(s)")
    if len(entries) % 10:
        print(f"ATENÇÃO: sobram {len(entries) % 10} palavras para fechar o dia")
    for p in problems:
        print("  -", p)

    if "--write" in sys.argv and not problems:
        out = sys.argv[sys.argv.index("--write") + 1]
        clean = [{k: o.get(k, "") for k in ("w", "pt", "s", "t", "r", "e")} for o in entries]
        with open(out, "w", encoding="utf-8") as f:
            f.write("[\n")
            f.write(",\n".join(json.dumps(c, ensure_ascii=False) for c in clean))
            f.write("\n]\n")
        print("gravado em", out)


if __name__ == "__main__":
    main()
