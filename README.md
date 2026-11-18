# personal-website

Personal portfolio with an integrated **"Ask my CV" assistant**. The assistant uses retrieval-augmented generation (RAG): it answers questions about background, projects and skills, grounded in his CV and GitHub repositories, with numbered citations.

- **Frontend:** React 19, TypeScript, Vite, Tailwind CSS 4 and shadcn/ui (Base UI), served by Nginx.
- **Backend:** Jakarta EE 10 on Java 21 (JAX-RS, CDI, JSON-B, JPA with Hibernate 6), running on Open Liberty.
- **Database:** PostgreSQL 16 with `pgvector`.
- **LLM:** Mistral (`ministral-14b`, falling back to `ministral-8b` and `ministral-3b`) writes the answers. It receives the complete CV, all project summaries and the README passages that local retrieval ranks highest, and must cite them. Without a `MISTRAL_API_KEY`, or when the usage limits are reached, the assistant falls back to a fully local extractive answer.

## Quick start

Requirements: Docker with Docker Compose v2.

```bash
echo "MISTRAL_API_KEY=your-key" > .env   # optional – without it, answers are extractive
docker compose up --build
```

| URL | What |
| --- | --- |
| http://localhost:3000 | Portfolio website |
| http://localhost:3000/api/status | Backend status (knowledge base size, last GitHub sync) |
| http://localhost:9080/health/ready | MicroProfile readiness check |

On the first start, the following happens in order:
1. Postgres runs `db/init.sql`, which enables `vector` and creates the tables.
2. The backend seeds the CV into the knowledge base and builds the embeddings.
3. The backend fetches the GitHub repositories and indexes their descriptions and READMEs.
4. The frontend starts once the backend reports *ready*.

The first build takes a few minutes, because Maven, npm and the Liberty features are downloaded.

## Architecture

```
 Browser ──► frontend (Nginx :80, published as :3000)
               ├── /          static React build
               └── /api/*  ──► backend (Open Liberty :9080)
                                 ├── GET  /api/projects   GitHub repos (cached in Postgres, refreshed every 6h)
                                 ├── POST /api/chat       RAG: pgvector search → context → Mistral answer with citations
                                 ├── GET  /api/profile    structured CV (content/profile.json)
                                 ├── GET  /api/status
                                 └── /health/{live,ready}
                                        │
                                        ▼
                                 db (pgvector/pgvector:pg16, volume pgdata)
                                   knowledge_chunk(content, embedding vector, …)
                                   github_repo(…cached repositories + READMEs…)
                                        ▲
                     api.github.com ────┘  (users/Ronald-Nguyen/repos?sort=updated&per_page=10)
```

### How the RAG pipeline works

1. **Knowledge base.**
   - `content/profile.json` is turned into self-contained prose chunks: one per job, degree, thesis and skill category, plus about, languages and contact.
   - GitHub repositories add a summary chunk each (description, language, topics, stars) plus up to 4 cleaned README chunks.
   - Chunks are upserted by content hash, and chunks that no longer exist are deleted.
2. **Embeddings.**
   - Each chunk becomes a TF-IDF vector over word unigrams and bigrams.
   - The tokenizer understands terms like `C#`, `C++`, `ASP.NET` and `full-stack`, and filters English and German stopwords.
   - Every vocabulary term gets its own dimension, so there are no hash collisions. Vectors are L2-normalised and stored in the `vector` column.
   - When the corpus changes, all chunks are re-embedded, which takes milliseconds.
3. **Retrieval.**
   - The question is tokenized and expanded with related terms. For example, *thesis* expands to *Bachelorarbeit* and *C#* to *.NET, ASP.NET*.
   - pgvector then ranks chunks by cosine distance: `ORDER BY embedding <=> CAST(? AS vector)`.
4. **Generated answer (Mistral).**
   - Every question is sent to Mistral together with the whole CV, all project summaries and up to 5 matching README passages, as numbered sources.
   - The model may combine, summarise and calculate, but only from the sources, and must cite them as `[n]`. Off-topic requests (code, essays, …) are declined.
   - The answer is validated server-side: foreign links and Markdown are stripped, long answers without citations are rejected, and citations are renumbered to the sources actually used.
5. **Fallback (no LLM).**
   - The most relevant sentences of the top chunks are extracted, grouped per source and tagged with `[n]` citations.
   - The response then carries a `notice` explaining why the AI did not answer.

### Abuse protection

- The API key lives only in `.env` (ignored by Git and Docker) and is never sent to the browser or logged.
- Server-side limits: calls per minute per model, calls and tokens per day, calls per visitor per hour, at most 3 concurrent calls; identical questions are cached for 6 hours.
- The backend port is bound to `127.0.0.1`, so visitor IPs (`X-Forwarded-For`) can only come from the Nginx proxy.

## API

```bash
curl -X POST http://localhost:3000/api/chat \
     -H 'Content-Type: application/json' \
     -d '{"question":"What was Ronald'\''s Bachelor thesis about?"}'
```

```json
{
  "answer": "Here's what I found in Ronald's CV and GitHub projects:\n\n… [1]\n\n… [2]",
  "found": true,
  "generated": true,
  "notice": null,
  "sources": [
    { "index": 1, "section": "Education", "title": "Bachelor's Thesis", "snippet": "…", "score": 0.27 }
  ]
}
```

- `GET /api/projects` returns `[{ id, name, description, url, homepage, language, stars, forks, topics, fork, pushedAt }]`. Fields that are `null` are omitted.
- Errors are always JSON: `{ "status": 400, "error": "…" }`.
- Questions are limited to 500 characters. The chat is rate-limited per client (`CHAT_RATE_LIMIT_PER_MINUTE`, default 10).

**Which repositories are shown:** the 10 most recently updated public repos. A fork is only included if it has at least 3 stars, or has commits pushed more than a day after it was forked.

## Configuration (`.env`)

| Variable | Default | Purpose |
| --- | --- | --- |
| `DB_NAME` / `DB_USER` / `DB_PASSWORD` | `portfolio` | PostgreSQL credentials |
| `GITHUB_USERNAME` | `Ronald-Nguyen` | Whose repositories to show |
| `GITHUB_TOKEN` | *(empty)* | Optional token; raises the GitHub rate limit (60 → 5000 req/h) |
| `GITHUB_REPO_LIMIT` | `10` | Number of repositories fetched |
| `GITHUB_EXCLUDE_REPOS` | `Ronald-Nguyen` | Comma-separated repository names that are never shown or indexed |
| `GITHUB_REFRESH_HOURS` | `6` | Refresh interval |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:3000` | Origins allowed to call the API directly |
| `CHAT_RATE_LIMIT_PER_MINUTE` | `10` | Chat requests per client per minute |
| `MISTRAL_API_KEY` | *(empty)* | Enables LLM answers; never commit it |
| `MISTRAL_MODELS` | `ministral-14b-latest,ministral-8b-latest,ministral-3b-latest` | Models in order of preference |
| `LLM_REQUESTS_PER_MINUTE` | `2` | Per model, all visitors together |
| `LLM_REQUESTS_PER_DAY` / `LLM_TOKENS_PER_DAY` | `300` / `1500000` | Daily caps |
| `LLM_REQUESTS_PER_CLIENT_HOUR` | `15` | Per visitor |
| `FRONTEND_PORT` / `BACKEND_PORT` | `3000` / `9080` | Host ports |

## Updating the CV content

All CV content lives in **`content/profile.json`**. The React site renders it, and the backend bundles it as the knowledge base. After editing it, run:

```bash
docker compose up --build
```

The backend detects changed chunks by their hash and re-indexes automatically.

## Local development without Docker

```bash
# 1. Database
docker compose up db          # (uncomment the db "ports" mapping in docker-compose.yml)

# 2. Backend – Java 21 + Maven; hot reload on http://localhost:9080
cd backend
mvn liberty:dev               # DB_HOST/DB_PORT/... env vars override the defaults in server.xml

# 3. Frontend – Node 20; http://localhost:5173, proxies /api to :9080
cd frontend
npm install
npm run dev
```

Run the backend tests with `cd backend && mvn test`. `RagPipelineTest` runs the whole retrieval and answer pipeline on the real CV data, without a database.

## Project layout

```
content/profile.json      CV data (single source of truth)
db/init.sql               pgvector extension + schema
backend/                  Jakarta EE 10 / Open Liberty (pom.xml, server.xml, persistence.xml, Java sources)
frontend/                 React + Vite + Tailwind + shadcn/ui, nginx.conf
docker-compose.yml        db + backend + frontend with healthchecks and a persistent volume
```

## Operations

- **Reset everything**, including the database volume, with `docker compose down -v`.
- **Logs:** `docker compose logs -f backend`
- **Inspect the knowledge base:** `docker compose exec db psql -U portfolio -c "select section, title from knowledge_chunk order by id;"`
