import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @Auther: admin
 * @Date: 2019/4/2 10:29
 * @Description:
 */
public class DemoAcu {

    public static void main(String[] args) {
        //递归获取文件
        traverseFaolder("C:\\Users\\admin\\Desktop");

        //查找docx文件
//        find(String path);
        //压缩docx文件
//        zipFiles();

    }
    public static  void traverseFaolder(String path){
        File file = new File(path);
        final File[] files = file.listFiles();
        if(files == null || files.length == 0){
            return;
        }
        for (File filepath : files){
            if(filepath.isDirectory()){
                traverseFaolder(filepath.getAbsolutePath());
            }else{
                System.out.println(filepath.getName());
            }
        }
    }

    public static void find(String path) throws IOException {
        File file = new File(path);
        File[] files = file.listFiles();
        //如果文件数组为null则返回
        if (files == null)
            return;
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                //判断是不是文件夹，如果是文件夹则继续向下查找文件
                find(files[i].getAbsolutePath());
            } else {
                if (files[i].getName().endsWith(".docx")) {
                    //记录文件路径
                    String filePath = files[i].getAbsolutePath().toLowerCase();
                    BufferedWriter fw = new BufferedWriter(new FileWriter("path.txt",true));
                    fw.write(filePath);
                    fw.newLine();
                    fw.flush();
                }
            }
        }
    }

    public static void zipFiles() {
        File zipFile = new File("file.zip");
        // 判断压缩后的文件存在不，不存在则创建
        if (!zipFile.exists()) {
            try {
                zipFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 创建 FileOutputStream 对象
        FileOutputStream fileOutputStream = null;
        // 创建 ZipOutputStream
        ZipOutputStream zipOutputStream = null;
        // 创建 FileInputStream 对象
        BufferedReader br = null;

        try {
            // 实例化 FileOutputStream 对象
            fileOutputStream = new FileOutputStream(zipFile);
            // 实例化 ZipOutputStream 对象
            zipOutputStream = new ZipOutputStream(fileOutputStream);
            // 创建 ZipEntry 对象
            ZipEntry zipEntry = null;
            // 遍历源文件数组
            FileInputStream fileInputStream = null;
                br = new BufferedReader(new FileReader("file.txt"));
                    String line = new String();
                 while((line=br.readLine())!=null) {
                     File file = new File(line);
                    zipEntry = new ZipEntry(file.getName());
                    zipOutputStream.putNextEntry(zipEntry);
                    // 该变量记录每次真正读的字节个数
                    int len;
                    // 定义每次读取的字节数组
                    byte[] buffer = new byte[1024];
                    fileInputStream = new FileInputStream(file);
                    while ((len = fileInputStream.read(buffer)) > 0) {
                        zipOutputStream.write(buffer, 0, len);
                    }
                }

            zipOutputStream.closeEntry();
            zipOutputStream.close();
            br.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
}
}
