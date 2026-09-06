CREATE EXTENSION IF NOT EXISTS vector;
CREATE TABLE IF NOT EXISTS knowledge_document (id BIGSERIAL PRIMARY KEY,title TEXT NOT NULL,source TEXT NOT NULL,topic TEXT,version INT NOT NULL DEFAULT 1,content_hash VARCHAR(64) NOT NULL UNIQUE,created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE TABLE IF NOT EXISTS knowledge_chunk (id BIGSERIAL PRIMARY KEY,document_id BIGINT NOT NULL REFERENCES knowledge_document(id) ON DELETE CASCADE,chunk_index INT NOT NULL,section TEXT,content TEXT NOT NULL,metadata JSONB NOT NULL DEFAULT '{}',embedding vector(1536),UNIQUE(document_id,chunk_index));
CREATE INDEX IF NOT EXISTS idx_knowledge_chunk_embedding ON knowledge_chunk USING hnsw (embedding vector_cosine_ops);

