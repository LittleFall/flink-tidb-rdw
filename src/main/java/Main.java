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
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(
                StreamExecutionEnvironment.getExecutionEnvironment(),
                EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build()
        );

        tEnv.executeSql("CREATE TABLE mysql_t (" +
                "id INT, " +
                "val INT " +
                ") WITH (" +
                "'connector' = 'kafka'," +
                "'topic' = 'example'," +
                "'properties.group.id' = 'test'," +
                "'scan.startup.mode' = 'earliest-offset'," +
                "'properties.bootstrap.servers' = '127.0.0.1:9092'," +
                "'format' = 'canal-json'  " +
                ")"
        );
        tEnv.executeSql("CREATE TABLE print_table WITH ('connector' = 'print')" +
        "LIKE mysql_t (EXCLUDING ALL)");
//        tEnv.executeSql("CREATE TABLE tidb_t (\n" +
//                "id int, " +
//                "val int " +
//                ") WITH (" +
//                "   'connector'  = 'jdbc',\n" +
//                "   'url'        = 'jdbc:mysql://mysql:4000/test',\n" +
//                "   'table-name' = 't',\n" +
//                "   'driver'     = 'com.mysql.jdbc.Driver',\n" +
//                "   'username'   = 'root',\n" +
//                "   'password'   = ''\n" +
//                ")"
//        );

        Table t = tEnv.from("mysql_t");
        t.executeInsert("print_table");
    }
}