// Biblioteca comum dos vídeos: tempo, animação e peças do app (telas do celular).
const C = { navy: '#0F1B2D', lime: '#C6F432', grey: '#A9B4C4', line: '#2A3A52', ground: '#F4F5F0', ink: '#0F1B2D', muted: '#5A6270' };
const GROUNDS = ['#0F1B2D', '#0E2A2A', '#2A1630', '#142616', '#2D1418', '#18213F'];

const clamp = (x, a = 0, b = 1) => Math.max(a, Math.min(b, x));
const ease = (x) => { x = clamp(x); return x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2; };
const out = (x) => 1 - Math.pow(1 - clamp(x), 3);
const back = (x) => { x = clamp(x); const c1 = 1.70158, c3 = c1 + 1; return 1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2); };
// 0 -> 1 entre a e b
const prog = (t, a, b) => clamp((t - a) / (b - a));
// opacidade de uma janela [a, b] com entrada/saída de d segundos
const win = (t, a, b, d = 0.25) => Math.min(prog(t, a, a + d), 1 - prog(t, b - d, b));

function el(tag, css, html, parent) {
  const e = document.createElement(tag);
  if (css) e.style.cssText = css;
  if (html !== undefined) e.innerHTML = html;
  (parent || document.body).appendChild(e);
  return e;
}

// Legenda estilo TikTok: frase branca grossa, palavra destacada em fundo verde-limão
function caption(text, top) {
  const html = text.replace(/\*(.+?)\*/g,
    '<span style="background:#C6F432;color:#0F1B2D;padding:0 14px;border-radius:14px;box-decoration-break:clone;-webkit-box-decoration-break:clone">$1</span>');
  return el('div', `position:absolute;left:70px;right:70px;top:${top}px;text-align:center;
    font:800 74px/1.18 Inter;color:#fff;letter-spacing:-1px;text-shadow:0 4px 18px rgba(0,0,0,.35);opacity:0`, html);
}
function show(e, o, dy = 0, scale = 1) {
  e.style.opacity = o;
  e.style.transform = `translateY(${dy}px) scale(${scale})`;
}
// legenda entra com um "pop" e sai rápido
function popCaption(e, t, a, b) {
  const o = win(t, a, b, 0.18);
  const s = 0.85 + 0.15 * back(prog(t, a, a + 0.35));
  show(e, o, (1 - out(prog(t, a, a + 0.3))) * 30, s);
}

// Celular: moldura + tela; devolve a tela para desenhar dentro
function phone(left, top, w, h) {
  const body = el('div', `position:absolute;left:${left}px;top:${top}px;width:${w}px;height:${h}px;
    background:#05070B;border-radius:${w * 0.13}px;padding:${w * 0.03}px;box-sizing:border-box;
    box-shadow:0 40px 90px rgba(0,0,0,.55), inset 0 0 0 3px #2B3240`);
  const screen = el('div', `position:relative;width:100%;height:100%;border-radius:${w * 0.105}px;overflow:hidden;background:#000`, '', body);
  el('div', `position:absolute;top:${h * 0.018}px;left:50%;width:${w * 0.09}px;height:${w * 0.09}px;margin-left:-${w * 0.045}px;
    border-radius:50%;background:#05070B;z-index:50`, '', screen);
  return { body, screen, w: w - w * 0.06, h: h - w * 0.06 };
}

// Tela de bloqueio do app (igual ao papel de parede que o app desenha)
function lockScreen(parent, sw, sh) {
  const u = sw / 390;
  const root = el('div', `position:absolute;inset:0;font-family:Inter;color:#fff;transition:none`, '', parent);
  const bg = el('div', `position:absolute;inset:0;background:${C.navy}`, '', root);
  const date = el('div', `position:absolute;top:${70 * u}px;width:100%;text-align:center;font:400 ${15 * u}px Inter;color:#E6EAF0`, 'Sex, 6 de novembro', root);
  const clock = el('div', `position:absolute;top:${88 * u}px;width:100%;text-align:center;font:600 ${80 * u}px/1 Inter;letter-spacing:-${2 * u}px`, '08:42', root);
  const halo = el('div', `position:absolute;left:50%;top:${300 * u}px;width:${200 * u}px;height:${200 * u}px;margin:-${100 * u}px 0 0 -${100 * u}px;
    border-radius:50%;background:rgba(255,255,255,.07)`, '', root);
  const emoji = el('div', `position:absolute;left:0;right:0;top:${300 * u}px;margin-top:-${62 * u}px;text-align:center;font-size:${112 * u}px;line-height:${124 * u}px;font-family:'Noto Color Emoji'`, '', root);
  const block = el('div', `position:absolute;left:${28 * u}px;right:${28 * u}px;top:${430 * u}px`, '', root);
  const chip = el('div', `display:inline-block;padding:${5 * u}px ${12 * u}px;border-radius:999px;background:${C.lime};color:${C.navy};font:700 ${11 * u}px Inter;letter-spacing:${1 * u}px`, '', block);
  const word = el('div', `margin-top:${14 * u}px;font:800 ${50 * u}px/1 Inter;color:${C.lime};letter-spacing:-${1 * u}px`, '', block);
  const pt = el('div', `margin-top:${6 * u}px;font:400 ${17 * u}px Inter;color:${C.grey}`, '', block);
  el('div', `margin:${16 * u}px 0;height:1px;background:${C.line}`, '', block);
  const sen = el('div', `font:700 ${21 * u}px/1.25 Inter`, '', block);
  const senPt = el('div', `margin-top:${6 * u}px;font:400 ${15 * u}px/1.35 Inter;color:${C.grey}`, '', block);
  const review = el('div', `margin-top:${12 * u}px;font:400 ${14 * u}px Inter;color:${C.lime};display:none`, '', block);
  const dots = el('div', `display:flex;gap:${6 * u}px;margin-top:${18 * u}px`, '', block);
  const ds = [];
  for (let i = 0; i < 10; i++) ds.push(el('span', `width:${24 * u}px;height:${6 * u}px;border-radius:${3 * u}px;background:${C.line}`, '', dots));
  // opts: { round2: true } troca para a segunda frase e o selo REVISÃO (como o app faz quando as 10 repetem);
  //       { review: 'palavra' } mostra a linha "Revisão: palavra" (palavra de um dia anterior dentro da frase)
  function set(w, n, seen, gi, opts = {}) {
    bg.style.background = GROUNDS[(gi ?? n - 1) % GROUNDS.length];
    const r2 = !!opts.round2;
    emoji.textContent = w.e; chip.textContent = `${r2 ? 'REVISÃO' : 'PALAVRA'} ${n} DE 10`; word.textContent = w.w;
    pt.textContent = w.pt; sen.textContent = r2 ? w.s2 : w.s; senPt.textContent = r2 ? w.t2 : w.t;
    const rv = r2 ? '' : (opts.review || '');
    review.textContent = rv ? 'Revisão: ' + rv : ''; review.style.display = rv ? 'block' : 'none';
    ds.forEach((d, i) => { d.style.background = i < seen ? C.lime : C.line; });
  }
  return { root, set, clock, block, emoji, halo, date, chip, sen, senPt, review, u };
}

// Frame inicial/final com o ícone do app
function endCard(parent) {
  const root = el('div', `position:absolute;inset:0;background:${C.navy};opacity:0;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:34px;font-family:Inter`, '', parent);
  const icon = el('div', `width:250px;height:250px;border-radius:64px;background:${C.lime};display:flex;align-items:center;justify-content:center;
    font:900 140px/1 Inter;color:${C.navy};letter-spacing:-6px;box-shadow:0 30px 80px rgba(198,244,50,.25)`, '10', root);
  el('div', `font:800 104px/1 Inter;color:#fff;letter-spacing:-3px;margin-top:10px`, '10 por Dia', root);
  el('div', `font:600 52px/1.25 Inter;color:${C.grey};text-align:center;max-width:860px`, '1.300 palavras em inglês<br>em 6 meses', root);
  const cta = el('div', `margin-top:30px;padding:26px 48px;border-radius:999px;background:${C.lime};color:${C.navy};font:800 46px Inter`, 'Siga pra saber quando lança 🚀', root);
  return { root, icon, cta };
}
function playEnd(card, t, a) {
  const p = prog(t, a, a + 0.4);
  card.root.style.opacity = p;
  card.icon.style.transform = `scale(${0.6 + 0.4 * back(prog(t, a + 0.1, a + 0.6))}) rotate(${(1 - out(prog(t, a + 0.1, a + 0.6))) * -12}deg)`;
  card.cta.style.transform = `scale(${1 + 0.04 * Math.sin((t - a) * 6) * prog(t, a + 1, a + 1.4)})`;
}

const W = {
  available: { w: 'available', pt: 'disponível', s: 'Is the doctor available this afternoon?', t: 'O médico está disponível hoje à tarde?', e: '✅' },
  schedule: { w: 'schedule', pt: 'agenda, cronograma', s: 'Can we check the schedule before the meeting?', t: 'Podemos ver a agenda antes da reunião?', e: '📅' },
  reliable: { w: 'reliable', pt: 'confiável', s: 'My old car is slow but reliable.', t: 'Meu carro velho é lento, mas confiável.', e: '🛡️' },
  deadline: { w: 'deadline', pt: 'prazo', s: 'The deadline is next Monday, so check your schedule.', t: 'O prazo é na próxima segunda, então veja sua agenda.', e: '⏳' },
  borrow: { w: 'borrow', pt: 'pegar emprestado', s: 'Can I borrow your pen for a minute?', t: 'Posso pegar sua caneta emprestada por um minuto?', e: '🤝' },
  hungry: { w: 'hungry', pt: 'com fome', s: "I'm hungry. Is the kitchen still open?", t: 'Estou com fome. A cozinha ainda está aberta?', e: '🍔' },
};
