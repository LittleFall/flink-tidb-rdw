# Flink-TiDB-RDW

A sample of Flink TiDB Realtime Datawarhouse.

[Tutorial Slides(In Chinese)](https://docs.google.com/presentation/d/1H3D_D6MKS3vT2-WMhpclG3xzIMA1_jR_fqwJ3KSzxrw/edit)

[Blog(In Chinese)](https://pingcap.com/blog-cn/when-tidb-and-flink-are-combined/)

Blog(In English) Publishing

Sincerely thanks to [TiDB](https://docs.pingcap.com/zh/tidb/stable) and [Apache Flink](https://flink.apache.org/)

## How to use

1. Docker-Compose Up

```bash
# Clone Project
git clone https://github.com/LittleFall/flink-tidb-rdw && cd ./flink-tidb-rdw/

# Reset Env
docker-compose down -v; rm -rf logs

# Startup
docker-compose up -d
```

You can use flink dashboard in [localhost:8081](localhost:8081).

2. Create Table in MySQL and TiDB.

```bash
docker-compose exec mysql mysql -uroot

DROP DATABASE IF EXISTS test;
CREATE DATABASE test; 
USE test;

create table base (
  base_id int primary key,
  base_location varchar(20)
);
create table stuff(
  stuff_id int primary key,
  stuff_base_id int,
  stuff_name varchar(20)
);
```

```bash
docker-compose exec mysql mysql -htidb -uroot -P4000

use test;
create table wide_stuff(
    stuff_id int primary key,
    base_id int,
    base_location varchar(20),
    stuff_name varchar(20)
);
```


3. Submit Task in Flink SQL Client.

```bash
docker-compose exec jobmanager ./bin/sql-client.sh embedded -l ./connector-lib
```

create Flink table

```sql
create table base (
    base_id int primary key,
    base_location varchar(20)
) WITH (
    'connector' = 'mysql-cdc',
    'hostname' = 'mysql',
    'port' = '3306',
    'username' = 'root',
    'password' = '',
    'database-name' = 'test',
    'table-name' = 'base'
);


create table stuff(
    stuff_id int primary key,
    stuff_base_id int,
    stuff_name varchar(20)
) WITH (
    'connector' = 'mysql-cdc',
    'hostname' = 'mysql',
    'port' = '3306',
    'username' = 'root',
    'password' = '',
    'database-name' = 'test',
    'table-name' = 'stuff'
); 

create table wide_stuff(
    stuff_id int primary key,
    base_id int,
    base_location varchar(20),
    stuff_name varchar(20)
) WITH (
	'connector'  = 'jdbc',
    'driver'     = 'com.mysql.cj.jdbc.Driver',
    'url'        = 'jdbc:mysql://tidb:4000/test?rewriteBatchedStatements=true',
    'table-name' = 'wide_stuff',
    'username'   = 'root',
    'password'   = ''
);

create table print_base WITH ('connector' = 'print') LIKE base (EXCLUDING ALL);

create table print_stuff WITH ('connector' = 'print') LIKE stuff (EXCLUDING ALL);

create table print_wide_stuff WITH ('connector' = 'print') LIKE wide_stuff (EXCLUDING ALL);
```

submit task to Flink Server

```sql
insert into wide_stuff
select stuff.stuff_id, base.base_id, base.base_location, stuff.stuff_name
from stuff inner join base
on stuff.stuff_base_id = base.base_id;

insert into print_base select * from base;

insert into print_stuff select * from stuff;

insert into print_wide_stuff
select stuff.stuff_id, base.base_id, base.base_location, stuff.stuff_name
from stuff inner join base
on stuff.stuff_base_id = base.base_id;
```

Then you can see four tasks in localhost:8081, they are:
- Print the changelog of `base` table in MySQL to standard output.
- Print the changelog of `stuff` table in MySQL to standard output.
- Join `base` and `stuff` to `wide_stuff`, Print the changelog of `wide_stuff` table to standard output.
- Wrint the changelog of `wide_stuff` table to TiDB.

You can use `docker-compose logs -f taskmanager` to see standard output.

4. Write data to MySQL for testing.

```bash
docker-compose exec mysql mysql -uroot

use test;
insert into base values (1, 'bj');
insert into stuff values (1, 1, 'zhangsan');
insert into stuff values (2, 1, 'lisi');
insert into base values (2, 'sh');
insert into stuff values (3, 2, 'wangliu');
update stuff set stuff_name = 'wangwu' where stuff_id = 3;
delete from stuff where stuff_name = 'lisi';
```

See result in standard output:

```bash
taskmanager_1   | +I(1,bj)
taskmanager_1   | +I(1,1,zhangsan)
taskmanager_1   | +I(2,sh)
taskmanager_1   | +I(2,1,lisi)
taskmanager_1   | +I(3,2,wangliu)
taskmanager_1   | -U(3,2,wangliu)
taskmanager_1   | +U(3,2,wangwu)
taskmanager_1   | +I(1,1,bj,zhangsan)
taskmanager_1   | +I(2,1,bj,lisi)
taskmanager_1   | +I(3,2,sh,wangliu)
taskmanager_1   | -U(3,2,sh,wangliu)
taskmanager_1   | +U(3,2,sh,wangwu)
taskmanager_1   | -D(2,1,lisi)
taskmanager_1   | -D(2,1,bj,lisi)
```

See result in TiDB:

```bash
docker-compose exec mysql mysql -htidb -uroot -P4000 -e"select * from test.wide_stuff";

+----------+---------+---------------+------------+
| stuff_id | base_id | base_location | stuff_name |
+----------+---------+---------------+------------+
|        1 |       1 | bj            | zhangsan   |
|        3 |       2 | sh            | wangwu     |
+----------+---------+---------------+------------+
```

## Note

1. It is recommended to adjust the available memory of docker compose to 8G or above.
2. Flink SQL client is designed for interactive execution. Currently, it does not support multiple statements input at a time. An available alternative is Apache Zeppelin.
3. If you want to connect to the outside in docker, use `host.docker.internal` as host.
4. You can use following command to check if Kafka received data.

```bash
docker-compose exec mysql mysql -uroot -e"insert into test.base values (1, 'bj')";
docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh --list --zookeeper zookeeper:2181  
docker-compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server kafka:9092 --topic test.base --from-beginning

docker-compose down -v && rm -rf ./logs && find ./config/canal-config -name "meta.dat"|xargs rm -f && docker-compose up -d
```

## Demo1: Datagen to Print

```sql
create table `source`(`a` int) with ('connector' = 'datagen', 'rows-per-second'='1');
create table `sink`(`a` int) with ('connector' = 'print');
insert into `sink` select * from `source`;
```

## Demo2: Datagen to MySQL

```bash
docker-compose exec mysql mysql -uroot # mysql
docker-compose exec mysql mysql -uroot -htidb -P4000 # tidb
docker-compose exec jobmanager ./bin/sql-client.sh embedded -l ./connector-lib
```

In MySQL
```sql
create database if not exists test; use test;
drop table if exists username;
create table username (
    id int primary key,
    name varchar(10),
    create_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);
```

In TiDB
```sql
create database if not exists test; use test;
drop table if exists username;
create table username (
    id int primary key,
    name varchar(10),
    mysql_create_time timestamp(6) NULL,
    tidb_create_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);
```

In Flink sql

```sql
create table source (
    id int primary key,
    name varchar(10),
    create_time timestamp(6)
) with (
    'connector' = 'mysql-cdc',
    'hostname' = 'mysql', 'port' = '3306',
    'username' = 'root', 'password' = '',
    'database-name' = 'test', 'table-name' = 'username'
);


https://github.com/ververica/flink-cdc-connectors/wiki/MySQL-CDC-Connector#connector-options

create table sink (
    id int primary key,
    name varchar(10),
    mysql_create_time timestamp(6)
) WITH (
	'connector' = 'jdbc', 'driver' = 'com.mysql.cj.jdbc.Driver',
    'username' = 'root', 'password' = '',
    'url' = 'jdbc:mysql://tidb:4000/test?rewriteBatchedStatements=true',
    'table-name' = 'username', 
    'sink.buffer-flush.max-rows' = '1', 'sink.buffer-flush.interval' = '0'
);
https://ci.apache.org/projects/flink/flink-docs-release-1.11/zh/dev/table/connectors/jdbc.html#connector-options

insert into sink select * from source;
```

You can use TiDB log to check the statements actually executed in TiDB.

```sql
-- Set TiDB slowlog threshold to see actually statements executed in TiDB.
set tidb_slow_log_threshold = 0;

-- Statements in MySQL
insert into `username`(`id`, `name`) values (1, 'a'), (2, 'b'), (3, 'c');
update username set name='d' where id=2; select * from username;
delete from username where id=1; select * from username;

-- Statements actually executed in TiDB
INSERT INTO `username`
(`id`, `name`, `mysql_create_time`) VALUES (1, 'a', '2020-09-14 12:44:24.581219') 
ON DUPLICATE KEY UPDATE `id`=VALUES(`id`), `name`=VALUES(`name`), `mysql_create_time`=VALUES(`mysql_create_time`);


INSERT INTO `username`(`id`, `name`) VALUES (1, 'c')  ON DUPLICATE KEY UPDATE `id`=VALUES(`id`), `name`=VALUES(`name`); -- batch execute
DELETE FROM `username` WHERE `id`=1;
```
## datagen to mysql

```sql
create table data_gen (
    id int primary key,
    name varchar(10)
) with (
    'connector' = 'datagen', 'rows-per-second'='100000',
    'fields.id.kind'='sequence', 'fields.id.start'='1', 'fields.id.end'='1000000',
    'fields.name.length'='10'
);

create table mysql_sink (
    id int primary key,
    name varchar(10)
) WITH (
	'connector' = 'jdbc', 'driver' = 'com.mysql.cj.jdbc.Driver',
    'username' = 'root', 'password' = '',
    'url' = 'jdbc:mysql://host.docker.internal:4000/test?rewriteBatchedStatements=true',
    'table-name' = 'username', 
    'sink.buffer-flush.max-rows' = '10000', 'sink.buffer-flush.interval' = '1'
);

insert into mysql_sink (id, name) select * from data_gen;
```

## Demo3: Stream Stream Join

In MySQL:
```sql
create database if not exists test; use test;
drop table if exists base;
create table base (
    id int primary key,
    location varchar(20)
);
drop table if exists stuff;
create table stuff(
    id int primary key,
    base_id int,
    name varchar(20)
);
```

In TiDB:
```sql
create database if not exists test; use test;

create table wide_stuff(
    stuff_id int primary key,
    base_id int,
    base_location varchar(20),
    stuff_name varchar(20)
);
```

In Flink SQL Client:
```sql
create table base (
    id int primary key,
    location varchar(20)
) with (
    'connector' = 'mysql-cdc',
    'hostname' = 'mysql', 'port' = '3306',
    'username' = 'root', 'password' = '',
    'database-name' = 'test', 'table-name' = 'base'
);

create table stuff(
    id int primary key,
    base_id int,
    name varchar(20)
) WITH (
    'connector' = 'mysql-cdc',
    'hostname' = 'mysql', 'port' = '3306',
    'username' = 'root', 'password' = '',
    'database-name' = 'test', 'table-name' = 'stuff'
);

create table wide_stuff(
    stuff_id int primary key,
    base_id int,
    base_location varchar(20),
    stuff_name varchar(20)
) WITH (
	'connector' = 'jdbc', 'driver' = 'com.mysql.cj.jdbc.Driver',
    'username' = 'root', 'password' = '',
    'url' = 'jdbc:mysql://tidb:4000/test?rewriteBatchedStatements=true',
    'table-name' = 'wide_stuff', 
    'sink.buffer-flush.max-rows' = '1', 'sink.buffer-flush.interval' = '0'
);

explain
insert into wide_stuff
select s.id, b.id, b.location, s.name
from stuff s, base b
where s.base_id = b.id;
```


Test

```sql
insert into base values (1, 'bj');
insert into stuff values (1, 1, 'zhangsan');
insert into stuff values (2, 1, 'lisi');
insert into base values (2, 'sh');
insert into stuff values (3, 2, 'wangliu');
update stuff set name = 'wangwu' where id = 3;
delete from stuff where name = 'lisi';
update base set location = 'gz' where location = 'bj';
```

## Demo4: Stream Table Join

Mysql and TiDB command is as same as above.

flink sql
```sql
create table stuff(
    id int primary key,
    base_id int,
    name varchar(20),
    proc_time as PROCTIME()
) WITH (
    'connector' = 'mysql-cdc',
    'hostname' = 'mysql', 'port' = '3306',
    'username' = 'root', 'password' = '',
    'database-name' = 'test', 'table-name' = 'stuff'
);

create table base (
    id int primary key,
    location varchar(20)
) WITH (
    'connector' = 'jdbc', 'driver' = 'com.mysql.cj.jdbc.Driver',
    'username' = 'root', 'password' = '',
    'url' = 'jdbc:mysql://mysql:3306/test', 'table-name' = 'base', 
    'lookup.cache.max-rows' = '10000', 'lookup.cache.ttl' = '5s'
);

create table wide_stuff(
    stuff_id int primary key,
    base_id int,
    base_location varchar(20),
    stuff_name varchar(20)
) WITH (
    'connector' = 'jdbc', 'driver' = 'com.mysql.cj.jdbc.Driver',
    'username' = 'root', 'password' = '',
    'url' = 'jdbc:mysql://tidb:4000/test?rewriteBatchedStatements=true',
    'table-name' = 'wide_stuff', 
    'sink.buffer-flush.max-rows' =  '10000', 'sink.buffer-flush.interval' = '1s'
);

insert into wide_stuff
select s.id, b.id, b.location, s.name
from stuff as s
join base FOR SYSTEM_TIME AS OF s.proc_time  b on s.base_id = b.id;
```

Test

```sql
insert into stuff values (1, 1, 'zhangsan');
insert into base values (1, 'bj');
insert into stuff values (2, 1, 'lisi');
insert into stuff values (3, 1, 'wangliu');
update base set location = 'gz' where location = 'bj';
insert into stuff values (4, 1, 'zhaoliu');
update stuff set name = 'wangwu' where name = 'wangliu';
```
