create table worker_reviews (
    id bigserial primary key,
    reviewer_worker_id bigint not null references workers (id) on delete cascade,
    target_worker_id bigint not null references workers (id) on delete cascade,
    rating integer not null check (rating between 1 and 5),
    text varchar(1000) not null,
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now(),
    constraint uk_worker_reviews_reviewer_target unique (reviewer_worker_id, target_worker_id)
);
