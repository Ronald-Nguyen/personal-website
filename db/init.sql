-- Schema for the portfolio backend. Executed once by the postgres image
-- on first start (empty data volume). Reset with: docker compose down -v

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS knowledge_chunk (
    id            BIGSERIAL PRIMARY KEY,
    source_type   TEXT        NOT NULL,              -- 'cv' | 'github'
    source_key    TEXT        NOT NULL,              -- e.g. 'cv:experience:gecko', 'github:repo-name'
    section       TEXT        NOT NULL,              -- human readable section, e.g. 'Experience'
    title         TEXT        NOT NULL,
    url           TEXT,
    chunk_index   INTEGER     NOT NULL DEFAULT 0,
    content       TEXT        NOT NULL,
    content_hash  TEXT        NOT NULL,
    embedding     vector,                            -- local TF-IDF embedding (dimension = vocabulary size)
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_chunk_source UNIQUE (source_key, chunk_index)
);

CREATE INDEX IF NOT EXISTS idx_chunk_source_type ON knowledge_chunk (source_type);
-- No ANN index: the vector dimension follows the corpus vocabulary and the
-- knowledge base holds a few hundred rows, so an exact scan is fast and precise.

CREATE TABLE IF NOT EXISTS github_repo (
    id           BIGINT      PRIMARY KEY,            -- GitHub repository id
    name         TEXT        NOT NULL,
    full_name    TEXT        NOT NULL,
    description  TEXT,
    html_url     TEXT        NOT NULL,
    homepage     TEXT,
    language     TEXT,
    stars        INTEGER     NOT NULL DEFAULT 0,
    forks        INTEGER     NOT NULL DEFAULT 0,
    topics       TEXT,                               -- comma separated
    fork         BOOLEAN     NOT NULL DEFAULT FALSE,
    pushed_at    TIMESTAMPTZ,
    updated_at   TIMESTAMPTZ,
    readme       TEXT,
    sort_order   INTEGER     NOT NULL DEFAULT 0,
    fetched_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
