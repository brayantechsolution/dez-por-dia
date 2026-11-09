# Agente de conteúdo do 10 por Dia

Este arquivo é o roteiro da tarefa diária que cria um vídeo novo e mantém o painel de conteúdo atualizado.
Cada execução começa do zero. Tudo o que ela precisa saber está aqui.

## Onde fica cada coisa

- **Painel** (calendário, vídeos, ideias, regras): https://claude.ai/artifact/KAhEhCXakcYkLnJRcyoUr9
  - O banco de dados é lido e escrito com a ferramenta `ArtifactData` (passe sempre essa `url`).
  - Os vídeos MP4 são anexados com a ferramenta `Artifact` usando `action: "publish"`, `url` do painel, `asset: true` e `file_path`.
  - Nunca republique a página do painel (o `painel.html` não está aqui). Só mexa nos dados e nos anexos.
- **Kit de vídeo**: `marketing/videos/`
  - `lib.js` traz as cores, a animação, o celular e as legendas.
  - `render.py` gera o MP4 (1080×1920, 30 fps) ou só alguns quadros para conferir.
  - Os vídeos de exemplo são `v1.html` a `v5.html`. Cada vídeo é um HTML com `DURATION` e uma função `render(t)`.
- **Palavras do app**: `app/src/main/assets/words.json`, com 1.300 palavras.
  - Campos: `w` (palavra), `pt` (tradução), `s`/`t` (frase e tradução), `s2`/`t2` (frase no passado, mostrada quando as 10 palavras repetem), `p` (passado do verbo, só nos verbos) e `e` (emoji).
  - No `lockScreen()` do kit, passe `p` na palavra para mostrar o passado ao lado (borrow → borrowed) e `{ round2: true }` para a rodada "REVISÃO NO PASSADO".
  - Use palavras e frases daqui sempre que o vídeo mostrar o app.

## Coleções do banco do painel

- `posts/<AAAA-MM-DD>` guarda um dia do calendário.
  - Campos: `date`, `video` (id do vídeo), `title`, `hook` (frase de abertura), `note`.
  - `platforms.tiktok`, `platforms.instagram` e `platforms.youtube` têm, cada um, `time`, `text`, `tags`, `status` e `views`.
  - `text` é a legenda (no YouTube, é o título). `status` é `pendente` ou `postado`. `views` é o número que a pessoa anota.
- `videos/<vN>` guarda um vídeo.
  - Campos: `title`, `hook`, `brief` (o que o vídeo mostra), `duration`, `order`, `date` e `status` (`a produzir` ou `pronto`).
  - Quando o vídeo está pronto, também `asset`, `url` (`/_blob/<asset>`) e `file`.
- `ideas/<id>` guarda uma ideia.
  - Campos: `title`, `hook`, `format`, `status` (`ideia`, `agendada` ou `feito`), `date`, `video` e `by` (`claude` ou `voce`).
  - As ideias com `by: "voce"` foram escritas pelo dono do app. Dê prioridade a elas.
- `guide/*` traz as regras das plataformas. Só leia.
- `meta/agent` tem os campos `when` e `message` e aparece no topo do painel. Atualize ao final de cada execução.

Toda escrita em documento que já existe leva `if_version` com a versão lida. Se der conflito, leia de novo e refaça.

## O que fazer em cada execução

1. **Escolher o vídeo.** Liste `videos` e pegue o de menor `date` com `status: "a produzir"`. Leia o `posts/<date>` dele.
2. **Escrever o vídeo** em `marketing/videos/<vN>.html`, no mesmo estilo dos exemplos.
   - Fundo escuro do app, destaque verde-limão e fonte Inter.
   - A frase de abertura (`hook`) fica na tela já no primeiro segundo.
   - Legendas grandes com `caption()`/`popCaption()`.
   - Termine com "Siga pra saber quando lança 🚀".
   - Duração de 12 a 25 s; use o `duration` do brief.
3. **Conferir antes de gerar.**
   - Rode `python3 render.py <vN>.html frames 0.5 <meio> <fim-1>` e olhe os quadros com Read.
   - Confira se o texto está legível, se nada encosta nas bordas e se o emoji combina com a palavra.
   - Confira o inglês e o português: tradução, acentos e respostas de quiz certas. Na dúvida, troque a palavra por uma do `words.json`.
4. **Gerar** com `python3 render.py <vN>.html out/<arquivo>.mp4`.
   - Nome do arquivo: `<vN>-<titulo-curto>.mp4`.
   - Se faltar fonte, instale com `apt-get install -y fonts-inter fonts-noto-color-emoji` ou peça ajuda no fim da execução.
5. **Anexar ao painel** com `Artifact`, `asset: true` e a `url` do painel.
   - Depois atualize `videos/<vN>` com `status: "pronto"`, `asset`, `url: "/_blob/<asset>"` e `file`.
   - Se o vídeo tiver alguma ideia ligada (`ideas` com esse `video`), mude essa ideia para `status: "feito"`.
   - Copie também o MP4 para `marketing/site/videos/<vN>.mp4` (só o id, ex.: `v8.mp4`). É dessa pasta pública que o Instagram puxa o vídeo (veja "Postagem no Instagram").
6. **Manter o calendário com pelo menos 7 dias à frente.**
   - Se houver menos de 7 dias depois de hoje, crie o próximo dia, usando a primeira ideia com `status: "ideia"` (as de `by: "voce"` primeiro).
   - Crie um `posts/<data>` com as três legendas, no mesmo formato dos dias existentes:
     - Horários: seg a sex TikTok 20:00, Reels 19:00, Shorts 12:00. Sáb e dom 18:00, 11:00 e 11:00.
     - A legenda do TikTok termina com uma pergunta. A do Instagram termina com "Manda pra quem…". As duas fecham com "Siga pra saber quando o app lança 🚀".
     - Hashtags: 4 fixas mais 1 do tema. No YouTube, `#aprenderingles #ingles #shorts`.
   - Crie também o `videos/<próximo vN>` com `status: "a produzir"` e marque a ideia como `agendada`.
   - Se acabarem as ideias, escreva 5 novas no mesmo estilo antes de agendar.
7. **Sempre que tiver uma boa ideia nova**, acrescente em `ideas` com `status: "ideia"` e `by: "claude"`. Bons formatos para este app:
   - quiz de palavras;
   - falsos cognatos;
   - POV;
   - desafio de 130 dias;
   - palavras por tema (trabalho, viagem, comida);
   - demonstração do app.
8. **Atualizar `meta/agent`**: `when` recebe a data em português curto (ex.: "10 nov") e `message` uma frase dizendo o que foi feito.
9. **Salvar no GitHub** o `.html` novo e o `marketing/site/videos/<vN>.mp4`. Nenhum outro MP4 (nada de `out/` ou `frames/`).
   - Use um commit em português e faça `git push` na `main`.
   - Pastas em `marketing/` não disparam a geração do APK.

## Postagem no Instagram (tarefa "Postar no Instagram")

O Instagram @10.pordia está ligado ao conector **Windsor.ai for Social Media** (ferramentas `mcp__Windsor_ai_for_Social_Media__*`, conta `17841459702457257`).
TikTok (@10.pordia) e YouTube (canal "10 por dia") estão ligados só para leitura: o Windsor não publica neles, quem posta é o dono do app.

1. Leia `posts/<hoje>` no painel. Só siga se `platforms.instagram.status` for `pendente` e o horário (`time`) for nas próximas 2 horas ou já tiver passado hoje.
2. O vídeo precisa estar em `marketing/site/videos/<video>.mp4` na `main`. O link público é
   `https://cdn.jsdelivr.net/gh/brayantechsolution/dez-por-dia@<sha-do-commit>/marketing/site/videos/<video>.mp4`
   (use o sha do último commit da main, não `@main`, para fugir do cache). Confira com `curl -sI` que responde 200 e `video/mp4`.
3. Se o horário ainda não chegou, espere com `sleep` até ele (no máximo 2 horas).
4. Poste com `execute_action`, `connector: "instagram"`, `action: "create_video_post"`, `params: {video_url, caption, share_to_feed: true}`.
   A legenda é o `text` do Instagram, uma linha em branco e as `tags` separadas por espaço.
5. Atualize `posts/<hoje>`: `platforms.instagram.status = "postado"` (sempre com `if_version`).
6. **Visualizações:** com `get_data` (sempre `get_fields` antes), leia as views dos últimos 14 dias:
   - Instagram: `media_caption`, `media_views` por post; ache o dia pelo começo da legenda.
   - TikTok e YouTube: views por vídeo; ache o dia pelo título ou pela legenda.
   - Grave em `platforms.<rede>.views` de cada dia. No TikTok e no YouTube, se achar o vídeo, mude também `status` para `postado`.
7. Atualize `meta/agent` com uma frase (ex.: "Reel de 15/11 postado; views atualizadas.").
8. Se a postagem falhar, registre o erro em `meta/agent`, não tente de novo mais de uma vez e avise o dono.

## Regras

- Poste só no Instagram, só o vídeo e a legenda do calendário do dia, e só pela tarefa de postagem. Não curta, não siga, não comente nem responda ninguém.
- TikTok e YouTube: só leitura.
- Nunca peça nem use senhas.
- Não mude o app (`app/`), só `marketing/`.
- Não apague nada do painel: posts, vídeos e ideias antigos ficam como histórico.
- Se algo der errado (render falhou, painel inacessível), registre em `meta/agent` o que aconteceu e pare.
