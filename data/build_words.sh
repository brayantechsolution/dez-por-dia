#!/bin/sh
# Monta app/src/main/assets/words.json a partir dos blocos.
# 1) confere e grava as palavras (data/b*.jsonl)
# 2) confere e acrescenta o passado dos verbos e a frase no passado (data/past/),
#    usada quando as 10 palavras do dia se repetem
set -e
cd "$(dirname "$0")/.."
python3 data/check_words.py data --write app/src/main/assets/words.json
python3 data/past/make_past.py app/src/main/assets/words.json > data/past/past.json
python3 data/past/merge_past.py merge app/src/main/assets/words.json
