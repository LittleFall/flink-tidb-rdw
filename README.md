# TiDB Flink 实时数仓

## 使用方法

1. 拉起 docker-compose 集群

```bash
# 克隆项目
git clone https://github.com/LittleFall/flink-tidb-rdw && cd ./flink-tidb-rdw/

# 重置环境
docker-compose down -v
rm -rf ./logs
find ./config/canal-config -name "meta.dat"|xargs rm -f

# 启动集群
docker-compose up -d
```

在集群启动后，可以通过 localhost:8081 查看 flink dashboard。

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

create table print_base WITH ('connector' = 'print') LIKE base (EXCLUDING ALL);

create table print_stuff WITH ('connector' = 'print') LIKE stuff (EXCLUDING ALL);

create table print_wide_stuff WITH ('connector' = 'print') LIKE wide_stuff (EXCLUDING ALL);
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

此时可以在 localhost:8081 看到一共注册了 4 个任务，分别是：
- 将 mysql 中 base 表的修改打印在标准输出中。
- 将 mysql 中 stuff 表的修改打印标准输出中。
- 将 base 表和 stuff 表 join 成 wide_stuff 表，将 wide_stuff 表的修改打印在标准输出中。
- 将 wide_stuff 表的修改写入到 tidb 中。

标准输出可以在本目录下执行 `docker-compose logs -f taskmanager` 持续查看。

3. 在目标数据库 TiDB 中创建结果表

```bash
docker-compose exec mysql-server mysql -htidb -uroot -P4000

use test;
create table wide_stuff(
    stuff_id int primary key,
    base_id int,
    base_location varchar(20),
    stuff_name varchar(20)
);
```

4. 在 MySQL 中写入数据，进行测试

```bash
docker-compose exec mysql-server mysql -uroot

use test;
insert into base values (1, 'bj');
insert into stuff values (1, 1, 'zhangsan');
insert into stuff values (2, 1, 'lisi');
insert into base values (2, 'sh');
insert into stuff values (3, 2, 'wangliu');
update stuff set stuff_name = 'wangwu' where stuff_id = 3;
delete from stuff where stuff_name = 'lisi';
```

此时可以在标准输出中看到对应的变化：

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

也可以在 TiDB 中查看结果：

```bash
docker-compose exec mysql-server mysql -htidb -uroot -P4000 -e"select * from test.wide_stuff";

+----------+---------+---------------+------------+
| stuff_id | base_id | base_location | stuff_name |
+----------+---------+---------------+------------+
|        1 |       1 | bj            | zhangsan         |
|        3 |       2 | sh            | wangwu         |
+----------+---------+---------------+------------+
```

## 注意点

1. Flink 需要内存较大，请将 docker-compose 集群可用的内存调大，建议 6G 及以上。
2. Flink SQL Client 设计为交互式执行，目前不支持同时执行多条语句，一个可用的替代方案是 apache zeppelin。