# Painel no Netlify

Cópia só de leitura do painel de conteúdo, publicada pelo Netlify a partir deste repositório.

- `index.html`: a página. Lê `dados.json`.
- `dados.json`: calendário, vídeos, ideias e regras. Gerado pelo agente com `exportar.py` e enviado ao GitHub a cada execução.
- `netlify.toml`: no Netlify, use **Base directory = `marketing/painel`**. Ele copia os MP4 de `marketing/site/videos/` e só publica de novo quando o painel ou os vídeos mudam.

Para editar (marcar postado, anotar views, adicionar ideias), use o painel do claude.ai. O Netlify mostra a cópia na próxima execução do agente.
