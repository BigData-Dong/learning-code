/**
 * @Auther: admin
 * @Date: 2019/3/17 17:22
 * @Description: 方差
 */
public class FCDemo {

    public static void main(String[] args) {

        double[] arr = {30.0,32.0,31.0,28.0,35.0,29.0,31.0};
        final double variance = Variance(arr);
        System.out.println(variance);
    }
    public static double Variance(double[] x) {
        int m=x.length;
        double sum=0;
        for(int i=0;i<m;i++){//求和
            sum+=x[i];
            System.out.println("求和:" + sum);
        }
        double dAve=sum/m;//求平均值
        System.out.println("平均数:" + dAve);
        double dVar=0;
        for(int i=0;i<m;i++){//求方差
            dVar+=(x[i]-dAve)*(x[i]-dAve);
        }
        return dVar/m;
    }

}
