create table users (
    id bigserial primary key,
    email varchar(255) not null unique,
    password_hash varchar(500) not null,
    role varchar(40) not null,
    created_at timestamptz not null default now()
);

alter table workers
    add column user_id bigint references users (id);

create unique index ux_workers_user_id on workers (user_id) where user_id is not null;

create table worker_reference_images (
    id bigserial primary key,
    worker_id bigint not null references workers (id) on delete cascade,
    title varchar(180) not null,
    image_url varchar(500) not null,
    storage_path varchar(500) not null,
    created_at timestamptz not null default now()
);

create index idx_worker_reference_images_worker_id on worker_reference_images (worker_id);
