# Flink TiDB RDW 

clickhouse test

1. 搭建一个 kafka-flink-clickhouse 的 docker compose。
2. 在 flink 中启动任务后，kafka 输入数据能在 clickhouse 中显示出来。

先跑一个 kafka-flink 任务吧！

是否一定要 upsert 流？