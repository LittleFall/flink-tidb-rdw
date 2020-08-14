package main;

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

import static main.Sqls.getTPCCSourceWith;
import static org.apache.flink.table.api.Expressions.*;

import java.util.Date;
import java.util.Properties;

import static main.Sqls.*;

public class Main {

    public static void main(String[] args) throws Exception {
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(
                StreamExecutionEnvironment.getExecutionEnvironment(),
                EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build()
        );

        String[] tpccTableNames = {"customer", "district", "history", "item", "new_order", "order_line", "orders", "stock", "warehouse"};
        for(String tableName: tpccTableNames) {
//            System.out.println(createTPCCTable(tableName) + getTPCCSourceWith(tableName));
            tEnv.executeSql(createTPCCTable(tableName) + getTPCCSourceWith(tableName));
            printSource(tEnv, tableName);
        }
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
update stuff set stuff_name = 'qz' where stuff_id = 3;
delete from stuff where stuff_name = 't';
*/
