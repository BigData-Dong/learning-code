import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Auther: admin
 * @Date: 2019/3/11 15:12
 * @Description:
 */
public class KafkaConsumerDemo extends Thread{

    public static void main(String[] args) throws ParseException {


        Properties props = new Properties();
        props.put("bootstrap.servers", "test01:9092,test02:9092,test03:9092");
        props.put("group.id", "2019-3-11");
        props.setProperty("auto.offset.reset", "earliest");
//        props.put("enable.auto.commit", "true");
//        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("person"));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                final String value = record.value();
                final Map<String,String> map = JSONObject.parseObject(value, Map.class);
                String date = map.get("timesId");
                if(date == null || date.isEmpty()){
                    System.out.println(date+"-------->"+map.toString());
                    continue;
                }
                //将timestamp 转换为 data
//                String date = "2019-03-11T07:59:51.000Z";
                date = date.replace("Z", " UTC");
                System.out.println(date);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
                Date d = format.parse(date);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss:SSS");
                final String format1 = simpleDateFormat.format(d);

                map.put("timesId",format1);

                System.out.printf("offset = %d, key = %s, value = %s\n", record.offset(), record.key(),map.toString());
            }

        }



    }
}
