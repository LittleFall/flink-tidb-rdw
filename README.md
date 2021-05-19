# Flink TiDB RDW 

clickhouse test

1. 搭建一个 kafka-flink-clickhouse 的 docker compose。
2. 在 flink 中启动任务后，kafka 输入数据能在 clickhouse 中显示出来。

先跑一个 kafka-flink 任务吧！


flink

```sh
docker-compose exec jobmanager ./bin/sql-client.sh embedded -l ./connector-lib
```
```sql
drop table if exists Orders;

CREATE TABLE Orders (
    order_id    bigint primary key,
    data        char, 
    proctime AS PROCTIME()
) WITH (
    'connector' = 'datagen',
    'rows-per-second' = '1',
    'fields.order_id.min' = '1',
    'fields.order_id.max' = '5'
);

CREATE TABLE Orders (
    order_id    bigint,
    data        char, 
    proctime AS PROCTIME()
) WITH (
    'connector' = 'kafka',
    'format' = 'json',
    'topic' = 'orders',
    'properties.bootstrap.servers' = 'kafka:9092',
    'properties.group.id' = 'testGroup',
    'json.fail-on-missing-field' = 'false',
    'json.ignore-parse-errors' = 'true'
);

drop table mysql_Orders;
create table mysql_Orders (
    order_id    bigint,
    data        char, 
    proctime    timestamp
) WITH (
    'connector' = 'jdbc', 'driver' = 'com.mysql.cj.jdbc.Driver',
    'username' = 'root', 'password' = '',
    'url' = 'jdbc:mysql://mysql:3306/test?rewriteBatchedStatements=true',
    'table-name' = 'username'
);

insert into mysql_Orders
select 1,'1','1';

insert into mysql_Orders
SELECT order_id, data, proctime FROM (
  SELECT *, ROW_NUMBER() OVER (PARTITION BY order_id ORDER BY proctime DESC) AS row_num
  FROM Orders
) WHERE row_num = 1;
```

mysql
```sql
create database test; use test;
create table Orders (
    order_id    bigint,
    data        char, 
    proctime    timestamp
);
```

kafka
```sh
# docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh --list --zookeeper zookeeper:2181 
# docker-compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server kafka:9092 --topic orders --from-beginning
docker-compose exec kafka /opt/kafka/bin/kafka-console-producer.sh  --broker-list kafka:9092 --topic orders

{"order_id":"1", "data":"first1"}
{"order_id":"1", "data":"second1"}
```



如果不给 Orders 添加主键，那么在 desc 时，新纪录就会被 append 到后面???

现在的情况：
1. 如果是一个 update/delete 流，那么不支持 Deduplicate 操作。所以必须是 append-only 流。
[ERROR] Could not execute SQL statement. Reason:
org.apache.flink.table.api.TableException: Deduplicate doesn't support consuming update and delete changes which is produced by node TableSourceScan(table=[[default_catalog, default_database, base]], fields=[base_id, base_location])
2. 如果是一个 kafka json 格式，那么不支持定义主键。
[ERROR] Could not execute SQL statement. Reason:
org.apache.flink.table.api.ValidationException: The Kafka table 'default_catalog.default_database.Orders' with 'json' format doesn't support defining PRIMARY KEY constraint on the table, because it can't guarantee the semantic of primary key.
3. 如果没有主键，那么在 desc（保留最新时），新纪录就会被 append 到后面，不符合用户的行为（用户明确指出有主键）。
4. 是否存在一个 source connector，既支持 append-only, 又有主键？



问题：请确认下，如果下游是类似 CK 这样无法直接支持 UPSERT 的系统，而且这边写的是 DESC 排序，那么理论上下游应该要接到一个 UPDATE 才能逻辑等价，这部分 CK Connector 会如何处理？

看代码，是否产生回撤取决于 generateUpdateBefore

好像没啥问题？

写到 mysql 里试一下。