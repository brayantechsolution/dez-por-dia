#!/bin/sh
# Monta app/src/main/assets/words.json a partir dos blocos.
# 1) confere e grava as palavras (data/b*.jsonl)
# 2) confere e acrescenta a segunda frase de cada palavra (data/alt/)
set -e
cd "$(dirname "$0")/.."
python3 data/check_words.py data --write app/src/main/assets/words.json
python3 data/alt/alt_sentences.py merge app/src/main/assets/words.json data/alt
