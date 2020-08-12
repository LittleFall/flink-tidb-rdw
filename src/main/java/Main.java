import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

import java.util.Date;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("group.id", "test");

        FlinkKafkaConsumer<String> myConsumer = new FlinkKafkaConsumer<>("example", new SimpleStringSchema(), properties);
        myConsumer.setStartFromEarliest();     // 尽可能从最早的记录开始

        DataStream<String> stream = env.addSource(myConsumer);

        stream.print();

        env.execute();
    }
}