alter table supplier_promotions
    add column if not exists price_unit varchar(20) not null default 'M2';
