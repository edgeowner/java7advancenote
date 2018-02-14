package com.guo.chap21;

/**
 * Created by guo on 2018/2/14.
 * 如何使用String类的各种方法将字符串解析成单个标识符
 */
public class StocksEODPaser {   
    //IBM在证券交易所上市结束日的报价
    private static String trade = "IBM,/09/09/218,87,100,80,95,1567823";

    public static void main(String[] args) {

        //retrieving a substring
        //使用substring方法从IBM交易字符串中提取日期字段。
        String dateField = trade.substring(4, 14);
        System.out.println("Substring field date equals" + dateField);

        //locating a character sequence
        //contains方法检查输入的字符串是否包含参数中指定的字符串
        if (trade.contains("/09/09/218")) {
            System.out.println("This is a trade on 09/09/2018");
        }

        //replace a character sequence
        //调用replace方法将字段分隔符逗号换成冒号。然后赋值给新的变量
        String str = trade.replace(",", ":");
        System.out.println("after replaceing delimitz:" + str);

        //replacing a character sequence
        //使用新的字符串替换旧的字符串
        str = trade.replace("100", "101");
        System.out.println("After replacing trade price 100:" + str);

        System.out.println("Spliting string into its fields");
        //调用split方法，将输入字符串中的标识符分离出来，然后在for-each中循环中将值打印到控制台
        String[] fields = trade.split(",");
        for (String strField : fields) {
            System.out.println("\t" + strField);
        }

        //计算最高价和最低价之间的差值，需要将字段的值转为对应的float类型。通过使用Float类的parse方法来实现的。
        float hilowDifference = Float.parseFloat(fields[3]) - Float.parseFloat(fields[4]);
        //为了将差值转换成String类型，需要使用String类的valueOf方法
        str = String.valueOf(hilowDifference);
        System.out.println("Difference in Hi to Low price:$" + str);

        //为了打印浮点数的值，可以简单的将浮点数追加到另外一个字符串中("" + hilowDifference)
        //使用静态的format方法，通过在末尾添加0来格式化给定的浮点数
        System.out.println(String.format("Formatted HiLow Differnce： $%.02f", hilowDifference));
    }
}
