CREATE TABLE IF NOT EXISTS public.cache_kv (
    id bigserial,
    id_cache   text NOT NULL,
    id_key     text NOT NULL,
    value      text NOT NULL,
    expire_at  timestamp with time zone NOT NULL,
    created_at timestamp with time zone NOT NULL DEFAULT now(),
    updated_at timestamp with time zone NOT NULL DEFAULT now(),
    deleted_at timestamp with time zone,
    CONSTRAINT cache_kv_pkey PRIMARY KEY (id)
);

-- Most important: enforces unique constraint and a composite index for the main lookup pattern
CREATE UNIQUE INDEX idx_cache_kv_cache_key ON public.cache_kv (id_cache, id_key);

-- Other useful secondary indexes

CREATE INDEX idx_cache_kv_cache_id
    ON public.cache_kv USING btree (id_cache);

CREATE INDEX idx_cache_kv_deleted_at
    ON public.cache_kv USING btree (deleted_at)
    WHERE (deleted_at IS NOT NULL);

CREATE INDEX idx_cache_kv_expire_at
    ON public.cache_kv USING btree (expire_at);

-- Trigger to auto-update updated_at column
CREATE TRIGGER public_cache_kv_moddatetime
BEFORE UPDATE ON public.cache_kv
FOR EACH ROW
EXECUTE FUNCTION extensions.moddatetime('updated_at');
