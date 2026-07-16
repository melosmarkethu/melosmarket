alter table workers
    add column trial_started_at timestamp with time zone,
    add column trial_ends_at timestamp with time zone,
    add column subscription_status varchar(40) not null default 'TRIALING';

update workers
set trial_started_at = created_at,
    trial_ends_at = created_at + interval '30 days'
where trial_started_at is null
   or trial_ends_at is null;

alter table workers
    alter column trial_started_at set not null,
    alter column trial_ends_at set not null;
