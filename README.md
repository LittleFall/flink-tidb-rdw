# TiDB Flink 实时数仓

## 使用方法

1. 拉起 docker-compose 集群

```bash
git clone https://github.com/LittleFall/flink-tidb-rdw && cd ./flink-tidb-rdw/

docker-compose up -d
```

2. 通过 Flink SQL Client 编写作业

```bash
docker-compose exec jobmanager ./bin/sql-client.sh embedded -l ./connector-lib
```

注册 Flink 表

```sql
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
	'connector'  = 'jdbc',
    'driver'     = 'com.mysql.cj.jdbc.Driver',
    'url'        = 'jdbc:mysql://tidb:4000/test?rewriteBatchedStatements=true',
    'table-name' = 'wide_stuff',
    'username'   = 'root',
    'password'   = ''
);

create table print_base LIKE base WITH ('connector' = 'print');

create table print_stuff LIKE stuff WITH ('connector' = 'print');

create table print_wide_stuff LIKE wide_stuff WITH ('connector' = 'print');
```

提交作业到 Flink 集群

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

在提交完成后，可以通过 localhost:8081 查看任务执行情况。

3. 在目标数据库 TiDB 中创建结果表

```bash
docker-compose exec db mysql -htidb -uroot -P4000
```

```sql
create table wide_stuff(
    stuff_id int primary key,
    base_id int,
    base_location varchar(20),
    stuff_name varchar(20)
);
```

4. 在 MySQL 中导入数据，进行测试

```bash
insert into base values (1, 'bj');
insert into stuff values (1, 1, 'zz');
insert into stuff values (2, 1, 'tmp');
insert into base values (2, 'sh');
insert into stuff values (3, 2, 'qq');
update stuff set stuff_name = 'qz' where stuff_id = 3;
delete from stuff where stuff_name = 'tmp';
```

## 解释
