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

# Start the environment
docker-compose up -d
docker-compose exec jobmanager ./bin/flink run /opt/tasks/flink-tidb-rdw.jar --source_host kafka --dest_host tidb

# If you failed to create wide tables in TiDB, please retry this.
docker-compose run tidb-initialize mysql -htidb -uroot -P4000 -e'source /initsql/tidb-init.sql'

# Prepare and run workload
docker-compose run go-tpc tpcc prepare -Hmysql -P3306 -Uroot -pexample --warehouses 4 -Dtpcc
docker-compose run go-tpc tpcc run -Hmysql -P3306 -Uroot -pexample --warehouses 4 -Dtpcc
```

## TODO

- [ ] implement the project on Kubernestes
