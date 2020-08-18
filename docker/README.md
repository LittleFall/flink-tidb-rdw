# TiDB with Flink in RDW

## Usage

Clone this repository, switch to the directory, execute the command below.

```bash
# If you have not installed the docker, you could run these commands
# curl -fsSL https://get.docker.com | bash -s docker --mirror Aliyun
# pip3 install docker-compose

git clone https://github.com/LittleFall/flink-tidb-rdw && cd ./flink-tidb-rdw/docker/

# Clean the existed environment
docker-compose down
rm -rf canal-server-logs/*
find ./canal-config -name "meta.dat"|xargs rm -f
find ./canal-config -name "h2.trace.db"|xargs rm -f
find ./canal-config -name "h2.mv.db"|xargs rm -f

# Start the environment
docker-compose up -d
docker-compose exec jobmanager ./bin/flink run /opt/tasks/flink-tidb-rdw.jar --dest_host db

# If you failed to create wide tables in TiDB, please retry this.
docker-compose run tidb-initialize mysql -h tidb -u root -P 4000 -e 'source /initsql/tidb-init.sql'

# Prepare and run workload
docker-compose run go-tpc tpcc prepare -H db -P 3306 -U root -p example --warehouses 4 -D tpcc
docker-compose run go-tpc tpcc run -H db -P 3306 -U root -p example --warehouses 4 -D tpcc
```

## TODO

- [ ] Deal with the source and the destination informations in Job files.
