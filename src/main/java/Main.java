import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.table.api.bridge.java.BatchTableEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.factories.DeserializationFormatFactory;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.connector.jdbc.internal.JdbcBatchingOutputFormat;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.*;
//import org.apache.flink.table.api.java;
import org.apache.flink.types.Row;

import static org.apache.flink.table.api.Expressions.*;

import java.util.Date;
import java.util.Properties;

public class Main {

    public static void main(String[] args) throws Exception {
        final String DDLCreateBase = "create table base ( \n"+
                "id int primary key, \n"+
                "location varchar(20) not null \n"+
                ")WITH ( \n"+
                "'connector' = 'kafka', \n"+
                "'topic' = 'example', \n"+
                "'properties.group.id' = 'testGroup', \n"+
                "'scan.startup.mode' = 'latest-offset', \n"+
                "'properties.bootstrap.servers' = 'localhost:9092', \n"+
                "'format' = 'canal-json', \n"+
                "'canal-json.ignore-parse-errors'='true'\n"+
                ")";
        final String DDLCreateStuff = "create table stuff(\n"+
                "id int primary key,\n"+
                "b_id int not null,\n"+
                "name varchar(20) not null\n"+
                ")WITH (\n"+
                "'connector' = 'kafka',\n"+
                "'topic' = 'example',\n"+
                "'properties.group.id' = 'testGroup',\n"+
                "'scan.startup.mode' = 'latest-offset',\n"+
                "'properties.bootstrap.servers' = 'localhost:9092',\n"+
                "'format' = 'canal-json', \n"+
                "'canal-json.ignore-parse-errors'='true'\n"+
                ")";
        final String DDLCreateWideStuff = "create table wide_stuff(\n"+
                "s_id int primary key,\n"+
                "b_id int,\n"+
                "b_location varchar(20),\n"+
                "s_name varchar(20)\n"+
                ") WITH (\n"+
                "'connector'  = 'jdbc',\n"+
                "'url'        = 'jdbc:mysql://127.0.0.1:4000/test',\n"+
                "'table-name' = 'wide_stuff',\n"+
                "'driver'     = 'com.mysql.cj.jdbc.Driver',\n"+
                "'username'   = 'root',\n"+
                "'password'   = ''\n"+
                ")";

        StreamTableEnvironment tEnv = StreamTableEnvironment.create(
                StreamExecutionEnvironment.getExecutionEnvironment(),
                EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build()
        );

        tEnv.executeSql(DDLCreateBase);
        tEnv.executeSql(DDLCreateStuff);
        tEnv.executeSql(DDLCreateWideStuff);

        printSource(tEnv, "base");
        printSource(tEnv, "stuff");

        tEnv.executeSql("CREATE TABLE print_wide_stuff WITH ('connector' = 'print') LIKE wide_stuff (EXCLUDING ALL)");

        Table t = tEnv.sqlQuery(
                "select distinct stuff.id s_id, base.id b_id, base.location, stuff.name\n" +
                "from stuff inner join base\n" +
                "on stuff.b_id = base.id\n" +
                "where location is not null"
        );
        t.executeInsert("wide_stuff");
        t.executeInsert("print_wide_stuff");
    }
    /**
     * 将源表的任何更改打印在屏幕上，原理是创建一个名为 print_${source}, sink = print 的表
     * @param tEnv 表环境
     * @param source 表名：必须已经被创建，必须是 source 表
     */
    static void printSource(StreamTableEnvironment tEnv, String source) {
        tEnv.executeSql("CREATE TABLE print_" + source + " WITH ('connector' = 'print') LIKE " + source + " (EXCLUDING ALL)");
        tEnv.from(source).executeInsert("print_" + source);
    }
}


/*
测试语句：

delete from base;
delete from stuff;

insert into base values (1, 'beijing');
insert into stuff values (1, 1, 'zz');
insert into stuff values (2, 1, 't');
insert into base values (2, 'shanghai');
insert into stuff values (3, 2, 'qq');
update stuff set name = 'qz' where id = 3;
delete from stuff where name = 't';
*/
