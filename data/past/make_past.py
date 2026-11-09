"""Marca os verbos do banco e escreve o passado simples de cada um (campo "p").

Uso: python3 data/past/make_past.py app/src/main/assets/words.json > data/past/past.json
Regra: é verbo quando o primeiro sentido em português é um infinitivo (com as exceções abaixo).
"""
import json
import re
import sys

IRREGULAR = {
    'be': 'was/were', 'become': 'became', 'begin': 'began', 'break': 'broke', 'bring': 'brought',
    'build': 'built', 'burn': 'burned', 'buy': 'bought', 'can': 'could', 'catch': 'caught',
    'choose': 'chose', 'come': 'came', 'cost': 'cost', 'cut': 'cut', 'deal': 'dealt', 'do': 'did',
    'dream': 'dreamed', 'drink': 'drank', 'drive': 'drove', 'eat': 'ate', 'fall': 'fell',
    'feel': 'felt', 'fight': 'fought', 'find': 'found', 'fly': 'flew', 'forget': 'forgot',
    'forgive': 'forgave', 'get': 'got', 'give': 'gave', 'go': 'went', 'grow': 'grew',
    'hang': 'hung', 'have': 'had', 'hear': 'heard', 'hold': 'held', 'hurt': 'hurt',
    'keep': 'kept', 'know': 'knew', 'learn': 'learned', 'leave': 'left', 'lend': 'lent',
    'let': 'let', 'lose': 'lost', 'make': 'made', 'mean': 'meant', 'meet': 'met', 'pay': 'paid',
    'put': 'put', 'quit': 'quit', 'read': 'read', 'ride': 'rode', 'ring': 'rang', 'run': 'ran',
    'say': 'said', 'see': 'saw', 'sell': 'sold', 'send': 'sent', 'set': 'set', 'shake': 'shook',
    'show': 'showed', 'sing': 'sang', 'sink': 'sank', 'sit': 'sat', 'sleep': 'slept',
    'smell': 'smelled', 'speak': 'spoke', 'spell': 'spelled', 'spend': 'spent', 'stand': 'stood',
    'swim': 'swam', 'take': 'took', 'teach': 'taught', 'tell': 'told', 'think': 'thought',
    'throw': 'threw', 'understand': 'understood', 'wake': 'woke', 'wear': 'wore', 'win': 'won',
    'write': 'wrote', 'sweat': 'sweated', 'lie': 'lied',
}
# Sílaba final tônica: dobra a consoante (stop → stopped, prefer → preferred)
DOUBLE = {'stop', 'plan', 'prefer', 'admit', 'drop', 'rob', 'shop', 'plug', 'permit', 'regret',
          'transfer', 'wrap'}
# Primeiro sentido parece infinitivo, mas a palavra inglesa não é verbo aqui
NOT_VERB = {'dinner', 'place', 'spoon', 'sugar', 'woman', 'pair', 'despite', 'leader', 'private',
            'popular', 'sea', 'take your time', 'rather', 'floor', 'home',
            'anything', 'phone', 'score'}
EXTRA_VERB = {'go', 'slow down'}
INF = re.compile(r'^\(?([a-zçãâéêíóúô]+(ar|er|ir)|pôr|ir)(-se)?(\(-se\))?\b')


def past_one(v):
    if v in IRREGULAR:
        return IRREGULAR[v]
    if v in DOUBLE:
        return v + v[-1] + 'ed'
    if v.endswith('e'):
        return v + 'd'
    if re.search(r'[^aeiou]y$', v):
        return v[:-1] + 'ied'
    return v + 'ed'


def past(word):
    parts = word.split(' ')
    head = parts[0]
    if head == 'be':
        return 'was/were ' + ' '.join(parts[1:])
    return ' '.join([past_one(head)] + parts[1:])


def is_verb(x):
    w = x['w']
    if w in NOT_VERB:
        return False
    if w in EXTRA_VERB:
        return True
    first = re.split(r'[;,]', x['pt'])[0].strip()
    return bool(INF.match(first))


def main():
    words = json.load(open(sys.argv[1]))
    out = {x['w']: past(x['w']) for x in words if is_verb(x)}
    json.dump(out, sys.stdout, ensure_ascii=False, indent=0)


if __name__ == '__main__':
    main()
