# TiDB with Flink in RDW

## Usage

Clone this repository, switch to the directory, execute the command below.

```bash
# If you have not installed the docker, you could run these commands
# curl -fsSL https://get.docker.com | bash -s docker --mirror Aliyun
# pip3 install docker-compose

git clone https://github.com/LittleFall/flink-tidb-rdw && cd ./flink-tidb-rdw/

# Clean the existed environment
docker-compose down
rm -rf ./logs
find ./config/canal-config -name "meta.dat"|xargs rm -f
find ./config/canal-config -name "h2.trace.db"|xargs rm -f
find ./config/canal-config -name "h2.mv.db"|xargs rm -f
docker-compose up

# Start the environment


# You can see information and the running job in Flink dashboard: http://localhost:8081/

# Import table structure to tidb.
while [ 0 -eq 0 ]
do
    docker-compose run tidb-initialize mysql -h tidb -u root -P 4000 -e 'source /initsql/tidb-init.sql'
    if [ $? -eq 0 ]; then
        break;
    else
        sleep 2
    fi
done

docker-compose exec jobmanager ./bin/flink run /opt/tasks/flink-tidb-rdw.jar --source_host kafka --dest_host tidb
# Prepare and run workload
docker-compose run go-tpc tpcc prepare -H db -P 3306 -U root -p example --warehouses 4 -D tpcc
docker-compose run go-tpc tpcc run -H db -P 3306 -U root -p example --warehouses 4 -D tpcc
```

When you start flink by yourself, and others by docker-compose.

```sh
./flink/bin/start-cluster.sh
./flink/bin/flink run ../flink-tidb-rdw/target/flink-tidb-rdw.jar
```

Test kafka:
```sh
./kafka/bin/kafka-topics.sh --list --zookeeper 127.0.0.1:2181
./kafka/bin/kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic test-base 

docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh --list --zookeeper zookeeper:2181
docker-compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server kafka:9092 --topic test-base

mysql -h127.0.0.1 -P3307 -uroot -pexample -Dtest -e"insert into base values (1, 'beijing')" 
mysql -h127.0.0.1 -P3307 -uroot -pexample -Dtest -e"delete from base"
```

## TODO

- [ ] implement the project on Kubernestes
