create table daily_page_views (
    view_date date primary key,
    view_count bigint not null default 0 check (view_count >= 0)
);
