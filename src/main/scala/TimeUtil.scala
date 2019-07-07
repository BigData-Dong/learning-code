import java.text.SimpleDateFormat

object TimeUtil {

    def calculateTime(sTime:String,eTime:String):Long = {
      val df = new SimpleDateFormat("yyyyMMddHHmmssSSS")
      val startTime = df.parse(sTime).getTime
      val endTime = df.parse(eTime).getTime
      endTime - startTime
    }
}
