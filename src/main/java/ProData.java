import java.util.UUID;

/**
 * @Auther: admin
 * @Date: 2019/2/19 09:19
 * @Description:
 */
public class ProData {

    public static void main(String[] args) {

    }


    private static String getUUID(){
        return UUID.randomUUID().toString().replace("-","");
    }
}
