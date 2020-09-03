# TiDB with Flink in RDW

## Usage

Clone this repository, switch to the directory, execute the command below.

```bash
# If you have not installed the docker, you could run these commands
# curl -fsSL https://get.docker.com | bash -s docker --mirror Aliyun
# pip3 install docker-compose

git clone https://github.com/LittleFall/flink-tidb-rdw && cd ./flink-tidb-rdw/

# Stop the environment
docker-compose down -v
rm -rf ./logs
find ./config/canal-config -name "meta.dat"|xargs rm -f
find ./config/canal-config -name "h2.trace.db"|xargs rm -f
find ./config/canal-config -name "h2.mv.db"|xargs rm -f

# Start the environment
docker-compose up -d

# You can see information and the running job in Flink dashboard: http://localhost:8081/

# Import table structure to tidb.
docker-compose run tidb-initialize bash /initsql/tidb-init.sh

docker-compose exec jobmanager ./bin/flink run /opt/tasks/flink-tidb-rdw.jar --source_host kafka --dest_host tidb
# Prepare and run workload
docker-compose run go-tpc tpcc prepare -Hdb -P3306 -Uroot -pexample --warehouses 4 -D tpcc
docker-compose run go-tpc tpcc run -Hdb -P3306 -Uroot -pexample --warehouses 4 -D tpcc
```

To watch the workload, there is a cheetsheat for you:

```bash
# watch tidb
watch -n 1 "mysql -h 127.0.0.1 -P 4000 -e 'select count(*) from  tpcc.wide_order_line_district'"
# watch resources
docker stats
```

Test commands:

```bash
mysql -h127.0.0.1 -P3307 -uroot -pexample -Dtest -e"insert into base values (1, 'beijing')" 
mysql -h127.0.0.1 -P3307 -uroot -pexample -Dtest -e"insert into stuff values (1, 1, 'zz')" 
mysql -h127.0.0.1 -P4000 -uroot -Dtest -e"select * from wide_stuff" 
mysql -h127.0.0.1 -P3307 -uroot -pexample -Dtest -e"delete from base"
mysql -h127.0.0.1 -P3307 -uroot -pexample -Dtest -e"delete from stuff"
mysql -h127.0.0.1 -P4000 -uroot -Dtest -e"select * from wide_stuff" 

docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh --list --zookeeper zookeeper:2181
docker-compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server kafka:9092 --topic test-base --from-beginning
```

## TODO

- [ ] implement the project on Kubernestes for production
