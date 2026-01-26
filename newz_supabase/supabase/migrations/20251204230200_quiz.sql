-- Channels
CREATE TABLE IF NOT EXISTS public.channels
(
    id          uuid PRIMARY KEY     DEFAULT uuid_generate_v4(),
    name        text        NOT NULL UNIQUE,
    description text,
    created_at  timestamptz NOT NULL DEFAULT now(),
    updated_at  timestamptz NOT NULL DEFAULT now()
);

CREATE TRIGGER channels_moddatetime
    BEFORE UPDATE
    ON public.channels
    FOR EACH ROW
EXECUTE FUNCTION moddatetime('updated_at');


-- Tags
CREATE TABLE IF NOT EXISTS public.tags
(
    name       text PRIMARY KEY,
    created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_tags_created_at ON public.tags (created_at);


-- Quiz Items
CREATE TABLE IF NOT EXISTS public.quiz_items
(
    id          uuid PRIMARY KEY     DEFAULT uuid_generate_v4(),
    title       text,
    description text,
    image_id    text,
    question    text        NOT NULL,
    options     text[]      NOT NULL,
    key         smallint    NOT NULL,
    story       jsonb,
    created_at  timestamptz NOT NULL DEFAULT now(),
    updated_at  timestamptz NOT NULL DEFAULT now(),
    deleted_at  timestamptz,
    version     int         NOT NULL
);

CREATE INDEX idx_quiz_items_question ON public.quiz_items USING gin (to_tsvector('english', question));

CREATE TRIGGER quiz_items_moddatetime
    BEFORE UPDATE
    ON public.quiz_items
    FOR EACH ROW
EXECUTE FUNCTION moddatetime('updated_at');


-- quizzes table
CREATE TABLE IF NOT EXISTS public.quizzes
(
    id           uuid PRIMARY KEY     DEFAULT uuid_generate_v4(),
    title        text        NOT NULL,
    description  text,
    channel_id   uuid        NOT NULL REFERENCES public.channels (id) ON DELETE RESTRICT,
    author_id    uuid        REFERENCES public.user_profiles (id) ON DELETE SET NULL,
    created_at   timestamptz NOT NULL DEFAULT now(),
    updated_at   timestamptz NOT NULL DEFAULT now(),
    published_at timestamptz NOT NULL,
    deleted_at   timestamptz,
    version      int         NOT NULL
);

CREATE INDEX idx_quizzes_channel ON public.quizzes (channel_id);
CREATE INDEX idx_quizzes_title ON public.quizzes USING gin (to_tsvector('english', title));

CREATE TRIGGER quizzes_moddatetime
    BEFORE UPDATE
    ON public.quizzes
    FOR EACH ROW
EXECUTE FUNCTION moddatetime('updated_at');


-- quizzes_quiz_items (join table with ordering)
CREATE TABLE IF NOT EXISTS public.quizzes_quiz_items
(
    quiz_id    uuid        NOT NULL REFERENCES public.quizzes (id) ON DELETE CASCADE,
    item_id    uuid        NOT NULL REFERENCES public.quiz_items (id) ON DELETE RESTRICT,
    pos        smallint    NOT NULL, -- position in the quiz (1..n)
    created_at timestamptz NOT NULL DEFAULT now(),
    version    int         NOT NULL,
    PRIMARY KEY (quiz_id, item_id),
    UNIQUE (quiz_id, pos)
);

CREATE INDEX idx_quizzes_quiz_items_quiz_pos ON public.quizzes_quiz_items (quiz_id, pos);
CREATE INDEX idx_quizzes_quiz_items_item ON public.quizzes_quiz_items (item_id);


-- items_tags (many-to-many between items and tags)
CREATE TABLE IF NOT EXISTS public.items_tags
(
    item_id  uuid NOT NULL REFERENCES public.quiz_items (id) ON DELETE CASCADE,
    tag_name text NOT NULL REFERENCES public.tags (name) ON DELETE CASCADE,
    PRIMARY KEY (item_id, tag_name)
);

CREATE INDEX idx_items_tags_tag ON public.items_tags (tag_name);
