# flink-tidb-rdw

flink-tidb 实时数仓(realtime data warehouse) 的单机 demo.

将数据从 mysql 实时同步到 tidb，同步过程中使用流式 join 将它们拼成一张宽表，以便查询。

举例来说，mysql 中有两张表：
```sql
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

对它们的任何修改都会实时同步到 tidb 中的这张表上：
```sql
create table wide_stuff(
	stuff_id int primary key,
	base_id int,
	base_location varchar(20),
	stuff_name varchar(20)
);
```

## 依赖

mysql, canal, zookeeper, kafka, flink, tidb

这同时也是数据流向。

## 配置

1. mysql 开启 binlog.
2. canal config
```config
canal.instance.master.address=127.0.0.1:3306
canal.zkServers = 127.0.0.1:2181
canal.instance.filter.regex=test\.stuff
canal.mq.topic=test-stuff

canal.serverMode = kafka
canal.mq.servers = 127.0.0.1:9092
canal.destinations = test-base, test-stuff
```
3. zoo.cfg
```config
tickTime=2000
initLimit=10
syncLimit=5
dataDir=/home/littlefall/flinks/zookeeper/logs/data
dataLogDir=/home/littlefall/flinks/zookeeper/logs/log
clientPort=2181
```
4. kafka server.properties
```config
listeners=PLAINTEXT://:9092
advertised.listeners=PLAINTEXT://127.0.0.1:9092
```

## 常用命令
```sh
# 停止
./canal/bin/stop.sh
./zookeeper/bin/zkServer.sh stop
./kafka/bin/kafka-server-stop.sh
killall -9 tidb-server

# 启动
./canal/bin/startup.sh
./zookeeper/bin/zkServer.sh start
./kafka/bin/kafka-server-start.sh -daemon ./kafka/config/server.properties
./tidb/bin/tidb-server --log-file=./tidb/logs/tidb-server.log &

# kafka 查看 topics, 查看消息
./kafka/bin/kafka-topics.sh --list --zookeeper 127.0.0.1:2181
./kafka/bin/kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092  --topic test-base # --from-beginning

# 用于调试 flink sql
./flink/bin/start-cluster.sh
./flink/bin/sql-client.sh embedded
SELECT 'Hello World';
```

## 测试及结果
```sql
delete from base;
delete from stuff;

insert into base values (1, 'beijing');
5> +I(1,beijing)
insert into stuff values (1, 1, 'zz');
8> +I(1,1,zz)
6> +I(1,1,beijing,zz)
insert into stuff values (2, 1, 't');
8> +I(2,1,t)
6> +I(2,1,beijing,t)
insert into base values (2, 'shanghai');
5> +I(2,shanghai)
insert into stuff values (3, 2, 'qq');
8> +I(3,2,qq)
6> +I(3,2,shanghai,qq)
update stuff set stuff_name = 'qz' where stuff_id = 3;
8> -U(3,2,qq)
8> +U(3,2,qz)
6> -U(3,2,shanghai,qq)
6> +U(3,2,shanghai,qz)
delete from stuff where stuff_name = 't';
8> -D(2,1,t)
6> -D(2,1,beijing,t)
```
