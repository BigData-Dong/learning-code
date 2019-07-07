import com.alibaba.fastjson.JSON
import com.google.common.eventbus.Subscribe
import com.typesafe.config.ConfigFactory
import kafka.common.TopicAndPartition
import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka.{HasOffsetRanges, KafkaCluster, KafkaUtils}
import org.apache.spark.streaming.{Duration, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import scalikejdbc.{DB, SQL}
import scalikejdbc.config.DBs

object LogAnalyser {

  def main(args: Array[String]): Unit = {

    //参数准备
    val conf = new SparkConf()
      .setAppName(s"${this.getClass.getSimpleName}")
      .setMaster("local[*]")
    val ssc = new StreamingContext(conf, Duration(5))
    //开启jdbc连接
    DBs.setup()

    //加载配置文件
    val load = ConfigFactory.load()

    val topic = load.getString("kafka.topics").split(",").toSet
    //配置kafka的参数
    val kafkaParam = Map(
      "metadata.broker.list" -> load.getString("kafka.broker.list"),
      "group.id" -> load.getString("kafka.group.id"),
      "auto.offset.reset" -> kafka.api.OffsetRequest.SmallestTimeString
    )

    //读取偏移量
    val fromOffsets = DB.readOnly(implicit session => {
      SQL("select * from minor where groupId = ? ").bind(load.getString("kafka.group.id"))
        .map(rs => {
          (TopicAndPartition(rs.string("topic"), rs.get[String]("partitions").toInt) -> rs.long("offset"))
        }).list().apply()
    }).toMap

    //假设程序第一次启动
    val stream = if (fromOffsets.size == 0) {
      KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParam, topic)
    } else {
      /**
        * kafka默认保存数据是7天 168个小时，如果旧的偏移量已经过期，会造成系统异常
        * 校验偏移量
        */
      var checkedOffset = Map[TopicAndPartition, Long]()

      //获得kafka集群对象
      val kafkaCluster = new KafkaCluster(kafkaParam)
      val earliestLeaderOffsets = kafkaCluster.getEarliestLeaderOffsets(fromOffsets.keySet)
      //left 错误    right 就是数据Map
      if (earliestLeaderOffsets.isRight) {
         //得到数据Map
        val topicAndPartitionToOffset = earliestLeaderOffsets.right.get

        // 开始对比
        checkedOffset = fromOffsets.map(owner => {
          //得到对应分区最早，有效的偏移量
          val clusterEarliestOffset = topicAndPartitionToOffset.get(owner._1).get.offset
          //如果自己的偏移量大于 集群最早的偏移量  则说明没有过期   所以我们用自己的偏移量
          if (owner._2 >= clusterEarliestOffset) {
            owner
          } else {
            (owner._1, clusterEarliestOffset)
          }
      })
    }

      //程序非第一次启动
      val messageHandler = (mm:MessageAndMetadata[String,String]) => {
        (mm.key(),mm.message())
      }
      KafkaUtils.createDirectStream[String,String,StringDecoder,StringDecoder,(String,String)](ssc,kafkaParam,checkedOffset,messageHandler)
    }

    stream.foreachRDD(rdd => {

      val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges

      val baseData: RDD[(String, String, String, String, List[Double])] = rdd.map(tp => JSON.parseObject(tp._2))
        .filter(_.getString("serviceName").equals("reChargeNotifyReq"))
        .map(jobj => {

          val provincceCode = jobj.getString("provinceCode")
          val requestId = jobj.getString("RequestId")
          val reciveTime = jobj.getString("receiveNotifyTIme")
          val result = jobj.getString("bussinessRst")
          val startTime = requestId.substring(0, 18)
          //充值成功金额
          val fee: Double = if (result == "0000") jobj.getDouble("chargefee") else 0
          //是否充值成功
          val succ: Double = if (result == "0000") 1 else 0
          //充值成功的时间
          val costTime: Double = if (result == "0000") TimeUtil.calculateTime(startTime, reciveTime) else 0

          (provincceCode, startTime.substring(0, 9), startTime.substring(0, 11), startTime.substring(0, 13), List(1.toDouble, succ, fee, costTime))
        })

      //第一个业务
      baseData.map(tp => ("all"+tp._2,tp._5))
          .reduceByKey((lst1,lst2) => {
            lst1.zip(lst2).map(tp => tp._1+tp._2)
          }).foreachPartition(itr =>{
        //将结果写入Redis
            itr.foreach(println)
           })


      //记录偏移量
      offsetRanges.foreach(osr => {
        DB.autoCommit{
          implicit session => SQL("replace into minor (groupId,topic,partitions,offset) values (?,?,?,?)")
            .bind(osr.topic,load.getString("kafka.group.id"),osr.partition,osr.untilOffset)
            .update().apply()
        }
      })
    })


      ssc.start()
      ssc.awaitTermination()

  }
}