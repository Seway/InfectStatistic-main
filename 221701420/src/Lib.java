import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//储存省份患者信息
class province_info{
    boolean empty;//是否为空
    String name;//省份
    int infected;//感染患者
    int suspected;//疑似患者
    int cure;//治愈
    int dead;//死亡
    public province_info(String temp){
        name=temp;
        empty=true;
        infected=0;//感染患者
        suspected=0;//疑似患者
        cure=0;//治愈
        dead=0;//死亡
    }
}

public class Lib {
    //初始化数据数组
    ArrayList<province_info> infos = new ArrayList<province_info>();

    public Lib() throws IOException {

        //读取排序好的文件对象并进行处理
        for (File file_temp : sort_file()) {
           System.out.println(file_temp.getName());
            read_file(file_temp);//读取指定的文件
        }
        summary();
        Arrangement();
        out();
    }
    //对指定目录下的文件按照日期进行排序
    private File[] sort_file() throws IOException {
        File file=new File("C:\\Users\\58215\\Desktop\\log");//创建指定目录的File对象
        File[] files = file.listFiles();
        List fileList = Arrays.asList(files);

        //按照日期升序对file对象进行排序
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && o2.isFile())
                    return -1;
                if (o1.isFile() && o2.isDirectory())
                    return 1;
                return o1.getName().compareTo(o2.getName());
            }
        });
        return files;
    }

    //读取指定的文件(传入File对象)
    private void read_file(File file) throws IOException {
        InputStreamReader reader=new InputStreamReader(new FileInputStream(file),"UTF-8");
        BufferedReader bfreader=new BufferedReader(reader);
        String line;
        while((line=bfreader.readLine())!=null) {//包含该行内容的字符串，不包含任何行终止符，如果已到达流末尾，则返回 null
           // System.out.println(line);
            adjust_arry(line);
            //Arrangement();
            //System.out.println();
        }
    }

    //对某行文字进行判断并对数据队列进行调整
    private void adjust_arry(String line){
            String[] lines=line.split(" ");
            if(lines[0].equals("//")) return;//省略注释部分

            //判断该条信息所对应的省份是否被创建
            boolean success=false;
            int where=0;
            for( int i=0;i<infos.size();i++){
                if(infos.get(i).name.equals(lines[0])){
                    success=true;
                    where=i;
                }
            }

            if(success==false){
                where=infos.size();
                province_info e=new province_info(lines[0]);
                infos.add(e);
            }

            //死亡或者治愈
            if (lines.length==3){
                if (lines[1].equals("死亡")){
                    int temp=trans_num(lines[2]);
                    infos.get(where).dead+=temp;
                    infos.get(where).infected-=temp;
                }
                else if (lines[1].equals("治愈")){
                    int temp=trans_num(lines[2]);
                    infos.get(where).cure+=temp;
                    infos.get(where).infected-=temp;
                }
            }
            //其他情况
            else if(lines.length==4){
                if (lines[1].equals("新增")){
                    int temp=trans_num(lines[3]);
                    if (lines[2].equals("感染患者")){//新增感染患者
                        infos.get(where).infected+=temp;
                    }
                    else {//新增疑似患者
                        infos.get(where).suspected+=temp;
                    }
                }
                else if(lines[1].equals("疑似患者")){//确认感染
                    int temp=trans_num(lines[3]);
                    infos.get(where).suspected-=temp;
                    infos.get(where).infected+=temp;
                }
                else if(lines[1].equals("排除")){
                    int temp=trans_num(lines[3]);
                    infos.get(where).suspected-=temp;
                }
            }

            //人口流动情况
            else if(lines.length==5){
                int temp=trans_num(lines[4]);//流动人数
                int to=search_province(lines[3]);//患者流向的省份
                if (lines[1].equals("感染患者")){//感染患者流动
                    infos.get(where).infected-=temp;
                    infos.get(to).infected+=temp;
                }
                else {//疑似患者流动
                    infos.get(where).suspected-=temp;
                    infos.get(to).suspected+=temp;
                }
            }
    }

    //对表示人数的字符串由String转为int
    private int trans_num(String line){
        //正则表达式提取数字
        String regEx="[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(line);
        int num=Integer.parseInt(m.replaceAll("").trim());
        return num;
    }

    //搜寻省份对应的
    private int search_province(String line){
        int num=-1;
        for (int i=0;i<infos.size();i++){
            if (line.equals(infos.get(i).name)){
                num=i;
                break;
            }
        }
        if (num==-1){
            num=infos.size();
            province_info e=new province_info(line);
            infos.add(e);
        }
        return num;
    }

    //对现有的对象数组进行整理
    private void Arrangement(){
        for (int i=0;i<infos.size();i++){
            System.out.print(infos.get(i).name);
            System.out.print("感染患者"+infos.get(i).infected+"人 ");
            System.out.print("疑似患者"+infos.get(i).suspected+"人 ");
            System.out.print("治愈"+infos.get(i).cure+"人 ");
            System.out.print("死亡"+infos.get(i).dead+"人 \n");
        }
    }

    //统计全国的情况
    private void summary(){
        province_info all=new province_info("全国");
        for (int i=0;i<infos.size();i++){
            all.dead+= infos.get(i).dead;
            all.suspected+=infos.get(i).suspected;
            all.infected+=infos.get(i).infected;
            all.cure+=infos.get(i).cure;
        }
        infos.add(all);
    }

    //写入文件
    private void out() throws IOException {
        File f1=new File("C:\\Users\\58215\\Desktop\\result");//传入文件/目录的路径
        File f2=new File(f1,"test.txt");//第一个参数为一个目录文件，第二个参数为要在当前f1目录下要创建的文件

        PrintWriter printWriter =new PrintWriter(new FileWriter(f2,true),true);//第二个参数为true，从文件末尾写入 为false则从开头写入

        for (int i=0;i<infos.size();i++){
            printWriter.print(infos.get(i).name+" ");
            printWriter.print("感染患者"+infos.get(i).infected+"人 ");
            printWriter.print("疑似患者"+infos.get(i).suspected+"人 ");
            printWriter.print("治愈"+infos.get(i).cure+"人 ");
            printWriter.print("死亡"+infos.get(i).dead+"人 \n");
        }
        printWriter.close();//记得关闭输入流
    }
}



