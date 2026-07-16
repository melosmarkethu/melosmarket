alter table workers
    add column top_worker boolean not null default false,
    add column many_references boolean not null default false,
    add column hundred_jobs boolean not null default false,
    add column fast_responder boolean not null default false,
    add column available_today boolean not null default false;
