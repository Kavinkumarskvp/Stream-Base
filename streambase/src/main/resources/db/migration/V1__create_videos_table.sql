CREATE TABLE public.videos
(
    id          bigserial               NOT NULL,
    title       varchar(255)            NOT NULL,
    description text NULL,
    url         varchar(500)            NOT NULL,
    uploaded_by varchar(100)            NOT NULL,
    created_at  timestamp DEFAULT now() NOT NULL,
    CONSTRAINT videos_pk PRIMARY KEY (id)
);