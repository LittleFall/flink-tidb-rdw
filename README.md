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
	stuff_id int not null primary key,
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
canal.mq.topic=example
canal.zkServers = 127.0.0.1:2181

canal.serverMode = kafka
canal.mq.servers = 127.0.0.1:9092
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
tickTime=2000
initLimit=10
syncLimit=5
dataDir=/home/littlefall/flinks/zookeeper/logs/data
dataLogDir=/home/littlefall/flinks/zookeeper/logs/log
clientPort=2181
```

## 常用命令
```sh
./canal/bin/startup.sh # 如果启动失败，就先 ./canal/bin/stop.sh
./zookeeper/bin/zkServer.sh start 
./kafka/bin/kafka-server-start.sh  -daemon  ./kafka/config/server.properties  # 如果启动失败，就再试一次
./tidb/bin/tidb-server --log-file=./tidb/logs/tidb-server.log &
./kafka/bin/kafka-topics.sh --list --zookeeper 127.0.0.1:2181 # 可以看到 example
./kafka/bin/kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092  --topic example --from-beginning # 查看历史消息
./kafka/bin/kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092  --topic example # 查看新产生的消息

# 用于直接测试
./flink/bin/start-cluster.sh
./flink/bin/sql-client.sh embedded
```
