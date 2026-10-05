create table suppliers (
    id bigserial primary key,
    user_id bigint not null references users (id) on delete cascade,
    business_name varchar(180) not null,
    email varchar(255) not null unique,
    phone varchar(50),
    website varchar(500),
    profile_image_url varchar(500),
    profile_image_storage_path varchar(500),
    cover_image_url varchar(500),
    cover_image_storage_path varchar(500),
    description text,
    address varchar(255),
    city varchar(120),
    county varchar(80),
    map_location varchar(500),
    opening_hours text,
    product_categories text,
    delivery_available boolean not null default false,
    max_delivery_distance_km integer,
    delivery_area text,
    delivery_info text,
    additional_services text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create unique index ux_suppliers_user_id on suppliers (user_id);
create index idx_suppliers_county on suppliers (county);
create index idx_suppliers_city on suppliers (city);

create table supplier_promotions (
    id bigserial primary key,
    supplier_id bigint not null references suppliers (id) on delete cascade,
    title varchar(180) not null,
    description text,
    current_price varchar(80) not null,
    original_price varchar(80),
    image_url varchar(500),
    image_storage_path varchar(500),
    start_date date,
    expiration_date date,
    active boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index idx_supplier_promotions_supplier_id on supplier_promotions (supplier_id);
create index idx_supplier_promotions_active_expiration on supplier_promotions (active, expiration_date);

create table supplier_quote_requests (
    id bigserial primary key,
    supplier_id bigint not null references suppliers (id) on delete cascade,
    customer_email varchar(255) not null,
    customer_phone varchar(50),
    materials text not null,
    message text,
    status varchar(40) not null default 'NEW',
    created_at timestamptz not null default now()
);

create index idx_supplier_quote_requests_supplier_id on supplier_quote_requests (supplier_id);
