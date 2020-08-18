# TiDB with Flink in RDW

## Usage

Clone this repository, switch to the directory, execute the command below.

```bash
# Clean the existed environment
docker-compose down
rm -rf canal-server-logs/*
find /root/nas-backups/flink/pyflink/playgrounds/canal-config -name "meta.dat"|xargs rm -f
find /root/nas-backups/flink/pyflink/playgrounds/canal-config -name "h2.trace.db"|xargs rm -f
find /root/nas-backups/flink/pyflink/playgrounds/canal-config -name "h2.mv.db"|xargs rm -f
# Start the environment
docker-compose up -d
# Modify the destination database ip according to your database
destination_host=<Destination Database Host>
docker-compose exec jobmanager ./bin/flink run /opt/tasks/flink-tidb-rdw.jar --dest_host $destination_host
/root/go-tpc tpcc -H 127.0.0.1 -P 3306 -U root -p example prepare --warehouses 4 -D tpcc
```

## TODO

- [ ] Deal with the source and the destination informations in Job files.
