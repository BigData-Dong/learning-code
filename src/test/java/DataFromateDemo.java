import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Auther: admin
 * @Date: 2019/3/11 15:32
 * @Description:
 */
public class DataFromateDemo {

    public static void main(String[] args) throws ParseException {


        String date = "2019-03-11T07:59:51.000Z";
        date = date.replace("Z", " UTC");
        System.out.println(date);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
        Date d = format.parse(date);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss:SSS");
        final String format1 = simpleDateFormat.format(d);
        System.out.println(format1);

    }
}
