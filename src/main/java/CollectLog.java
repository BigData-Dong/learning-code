import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import kafka.serializer.StringEncoder;

import java.io.*;
import java.util.Properties;

public class CollectLog {
   public static void main(String[] args){
     Properties properties = new Properties();
     properties.setProperty("metadata.broker.list","test01:9092,test02:9092,test03:9092");
     //消息传递到broker时的序列化方式
      properties.setProperty("serializer.class",StringEncoder.class.getName());
      //zk的地址
      properties.setProperty("zookeeper.connect","test01:2181,test02:2181,test03:2181");
      //是否反馈消息 0是不反馈消息 1是反馈消息
      properties.setProperty("request.required.acks","1");
      ProducerConfig producerConfig = new ProducerConfig(properties);
      Producer<String,String> producer = new Producer<String,String>(producerConfig);
      try {
         BufferedReader bf = new BufferedReader(new FileReader(new File("C:\\Users\\admin\\Desktop\\资料\\项目三\\mobile\\flumeLoggerapp4.log.20170412")));
         String line = null;
         while((line=bf.readLine())!=null){
            System.out.println("line:"+line);
           KeyedMessage<String,String> keyedMessage = new KeyedMessage<String,String>("JsonData",line);
           Thread.sleep(20);
            producer.send(keyedMessage);
         }
         bf.close();
         producer.close();
         System.out.println("已经发送完毕");

      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
