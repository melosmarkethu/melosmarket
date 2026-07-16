alter table customer_problems
    add column phone varchar(50);

create table problem_images (
    id bigserial primary key,
    problem_id bigint not null references customer_problems (id) on delete cascade,
    title varchar(180) not null,
    image_url varchar(500) not null,
    storage_path varchar(500) not null,
    created_at timestamptz not null default now()
);

create index idx_problem_images_problem_id on problem_images (problem_id);
