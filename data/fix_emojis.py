"""Troca as ilustrações que não combinavam com a palavra (revisão de 07/11)."""
import glob
import json
import sys

FIX = {
    # bloco 1
    "appointment": "🗓️", "busy": "🤹", "bill": "🧾", "change": "🔄", "ready": "🚀",
    "already": "✔️", "almost": "😅", "forget": "🤦", "need": "❗", "have": "👜",
    "wait": "⏳", "today": "📅", "schedule": "🗒️", "study": "📚", "read": "📖",
    "new": "🆕", "start": "▶️", "begin": "▶️", "head": "💆", "bathroom": "🛁",
    "let": "🙌", "name": "🏷️", "weekday": "💼", "luggage": "🛄", "trip": "🧭",
    "carry": "🎒", "behind": "🫣", "understand": "💡", "late": "⌚",
    "Monday": "🌙", "Tuesday": "🌮", "Wednesday": "🐫", "Thursday": "⚡",
    "Friday": "🎉", "Saturday": "🪐", "Sunday": "☀️",
    # bloco 2
    "client": "🧑‍💼", "manager": "👩‍💼", "short": "🩳", "room": "🛏️", "quit": "🙅‍♀️",
    "lamp": "🪔", "tip": "💰", "ride": "🚴", "something": "📦", "nothing": "0️⃣",
    "list": "📝", "dirty": "🦠", "then": "⏩", "shift": "🌗", "if": "❔", "when": "🕐",
    "other": "🔃", "instead": "🔀", "pick up": "🚗", "update": "🆙", "account": "🏧",
    "transfer": "📲", "promise": "🤝", "reservation": "🍽️", "happen": "⁉️",
    "miss": "🥲", "liter": "💧", "recover": "💪",
    # bloco 3
    "season": "🌦️", "sport": "🏀", "player": "⛹️", "hold": "✊", "important": "🔑",
    "vacuum": "🌀", "boring": "🥱", "therefore": "👉", "colleague": "🧑‍💻",
    "citizen": "🇧🇷", "man": "👨", "woman": "👩", "pretty": "💄", "degree": "🌡️",
    "fine": "👮", "library": "📚", "keep": "🗃️", "boyfriend": "💙", "girlfriend": "💖",
    "anniversary": "💝", "exchange": "💱", "advise": "🦉",
    # bloco 4
    "language": "🗣️", "country": "🗺️", "collect": "🃏", "hesitate": "😶", "leader": "👑",
    "soft": "🪶", "on the other hand": "🫲", "heal": "❤️‍🩹", "butcher": "🥩", "shop": "🛍️",
    "cashier": "🧮", "speed": "💨", "story": "🧚", "translate": "🌐", "lawyer": "🧑‍⚖️",
    "driver": "🚌", "mechanic": "🧑‍🔧", "employee": "🪪", "thirsty": "🥤", "weak": "🪫",
    "desk": "💻", "pay attention": "👀",
}

folder = sys.argv[1]
found = set()
changes = []
for fn in sorted(glob.glob(folder + "/b*.jsonl")):
    out = []
    for line in open(fn, encoding="utf-8"):
        if not line.strip():
            continue
        o = json.loads(line)
        if o["w"] in FIX:
            found.add(o["w"])
            if o["e"] != FIX[o["w"]]:
                changes.append((o["w"], o["e"], FIX[o["w"]]))
                o["e"] = FIX[o["w"]]
        out.append(json.dumps(o, ensure_ascii=False))
    open(fn, "w", encoding="utf-8").write("\n".join(out) + "\n")

missing = set(FIX) - found
if missing:
    sys.exit(f"palavras não encontradas: {sorted(missing)}")
for w, a, b in changes:
    print(f"{w}: {a} -> {b}")
print(len(changes), "ilustrações trocadas")
