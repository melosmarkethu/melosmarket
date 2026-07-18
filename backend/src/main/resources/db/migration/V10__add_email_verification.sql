alter table users
    add column email_verified boolean not null default true,
    add column email_verified_at timestamptz;

create table email_verification_tokens (
    id bigserial primary key,
    user_id bigint not null references users (id) on delete cascade,
    token varchar(120) not null unique,
    expires_at timestamptz not null,
    used_at timestamptz,
    created_at timestamptz not null default now()
);

create index idx_email_verification_tokens_user_id on email_verification_tokens (user_id);
create index idx_email_verification_tokens_token on email_verification_tokens (token);
