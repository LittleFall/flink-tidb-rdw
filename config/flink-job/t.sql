create table base (
    base_id int primary key,
    base_location varchar(20)
) WITH (
    'connector' = 'kafka',
    'properties.bootstrap.servers' = 'kafka:9092',
    'properties.group.id' = 'test',
    'topic' = 'test-base',
    'scan.startup.mode' = 'latest-offset',
    'format' = 'canal-json',
    'canal-json.ignore-parse-errors'='true'
);

create table stuff(
    stuff_id int primary key,
    stuff_base_id int,
    stuff_name varchar(20)
) WITH (
    'connector' = 'kafka',
    'properties.bootstrap.servers' = 'kafka:9092',
    'properties.group.id' = 'test',
    'topic' = 'test-stuff',
    'scan.startup.mode' = 'latest-offset',
    'format' = 'canal-json',
    'canal-json.ignore-parse-errors'='true'
);

create table wide_stuff(
    stuff_id int primary key,
    base_id int,
    base_location varchar(20),
    stuff_name varchar(20)
) WITH (
    'driver'     = 'com.mysql.cj.jdbc.Driver',
    'url'        = 'jdbc:mysql://tidb:4000/test?rewriteBatchedStatements=true',
    'table-name' = 'wide_stuff',
    'username'   = 'root',
    'password'   = ''
);

insert into wide_stuff
select stuff.stuff_id, base.base_id, base.base_location, stuff.stuff_name
from stuff inner join base
on stuff.stuff_base_id = base.base_id;


create table print_base LIKE base WITH ('connector' = 'print');
create table print_stuff LIKE stuff WITH ('connector' = 'print');
create table print_wide_stuff LIKE wide_stuff WITH ('connector' = 'print');

insert into print_base select * from base;
insert into print_stuff select * from stuff;

insert into print_wide_stuff
select stuff.stuff_id, base.base_id, base.base_location, stuff.stuff_name
from stuff inner join base
on stuff.stuff_base_id = base.base_id;