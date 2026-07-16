alter table workers
    add column availability_status varchar(40) not null default 'AVAILABLE',
    add column urgent_work boolean not null default false;
