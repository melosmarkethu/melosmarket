create table customers (
    id bigserial primary key,
    full_name varchar(160) not null,
    email varchar(255) not null unique,
    phone varchar(50),
    created_at timestamptz not null default now()
);

create table workers (
    id bigserial primary key,
    business_name varchar(180) not null,
    contact_name varchar(160) not null,
    email varchar(255) not null unique,
    phone varchar(50),
    trade varchar(80) not null,
    service_area varchar(160),
    description text,
    created_at timestamptz not null default now()
);

create table customer_problems (
    id bigserial primary key,
    customer_id bigint references customers (id),
    title varchar(180) not null,
    description text not null,
    trade varchar(80),
    location varchar(180),
    status varchar(40) not null default 'OPEN',
    created_at timestamptz not null default now()
);

create index idx_workers_trade on workers (trade);
create index idx_customer_problems_trade_status on customer_problems (trade, status);

