# Postagem automática no Instagram (@10.pordia)

Roteiro da tarefa agendada que posta o Reel do dia. Cada execução começa do zero e segue estes passos.
TikTok e YouTube quem posta é o dono do app. Esta tarefa cuida só do Instagram.

## Onde fica cada coisa

- **Painel:** https://claude.ai/artifact/KAhEhCXakcYkLnJRcyoUr9
  - Os dados se leem e escrevem com `ArtifactData`.
  - O post do dia fica em `posts/<AAAA-MM-DD>` e o vídeo em `videos/<id>`. O campo `asset` é o id do arquivo MP4 anexado ao painel.
- **Instagram:** conector "Windsor.ai for Social Media".
  - connector: `instagram`
  - account: `17841459702457257` (@10.pordia)
- **Link público do vídeo:**
  - O Instagram baixa o vídeo de um link público, e os anexos do painel são privados.
  - Por isso o MP4 vai para o ramo `midia` do repositório e é servido pelo jsDelivr: `https://cdn.jsdelivr.net/gh/brayantechsolution/dez-por-dia@<commit>/<id>.mp4`.

## Passos

1. Pegue a data de hoje no fuso de Brasília e leia `posts/<data>`.
   - Se não existir, ou se `platforms.instagram.status` já for `postado`, pare sem fazer nada.
2. Leia `videos/<post.video>`.
   - Se o status não for `pronto` ou não tiver `asset`, registre em `meta/agent` que o vídeo do dia não está pronto e pare.
3. Baixe o MP4 com a ferramenta `Artifact`: `action: "read"`, a `url` do painel e `path: <asset>`.
4. Publique o arquivo no ramo `midia`:
   - Rode `git clone --depth 1 --branch midia <remote> midia`, copie o arquivo como `<id>.mp4`, faça commit e `git push`.
   - Os commits terminam com as linhas de autoria que a tarefa indicar.
   - Anote o hash do commit, que vai no link.
5. Poste com `execute_action`:
   - connector `instagram`, account acima, action `create_video_post`.
   - `video_url`: o link do jsDelivr com o hash do commit.
   - `caption`: o `text` de `platforms.instagram`, uma linha em branco e as `tags` separadas por espaço.
   - `share_to_feed: true`.
6. **Se postou:**
   - Em `posts/<data>`, mude `platforms.instagram.status` para `postado`, com `if_version`.
   - Em `meta/agent`, `when` recebe a data curta (ex.: "9 nov") e `message` algo como "Reel do dia postado no Instagram às 19h".
7. **Se deu erro:**
   - Tente uma vez de novo, se o erro sugerir que vale (por exemplo, o vídeo ainda processando).
   - Se falhar de novo, registre o erro em `meta/agent` e pare. Nunca poste duas vezes o mesmo vídeo.

## Regras

- Poste só o que está no painel para o dia, com a legenda do painel. Não invente legenda.
- Não apague nem edite posts já publicados, e não responda comentários sem o dono pedir.
- Nunca peça nem use senhas.
- Se o repositório virar privado, o link do jsDelivr para de funcionar. Nesse caso, registre o problema em `meta/agent` e pare.

## Painel no Netlify

No fim, se algo mudou no painel (postagem ou views), siga a seção "Atualizar o painel do Netlify" do `marketing/AGENTE.md` e faça push do `marketing/painel/dados.json`.
