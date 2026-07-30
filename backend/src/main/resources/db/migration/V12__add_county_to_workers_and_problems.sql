alter table workers
    add column county varchar(80);

alter table customer_problems
    add column county varchar(80);

create index idx_workers_county on workers (county);
create index idx_customer_problems_county_status on customer_problems (county, status);
