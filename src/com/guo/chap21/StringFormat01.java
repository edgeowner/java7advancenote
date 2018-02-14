package com.guo.chap21;
import java.util.Formatter;
/**
 * Created by guo on 2018/2/14.
 */
public class StringFormat01 {
    public static void main(String[] args) {
        //表示可变的字符序列，与早期的StringBuffer类变比，处于性能考虑，建议使用StringBuilder类来创建可变字符串
        StringBuilder stringBuilder = new StringBuilder();
        Formatter formatter = new Formatter(stringBuilder);
        formatter.format("MAX float value: %10e\n", Float.MAX_VALUE );
        System.out.println(stringBuilder);

    }
}
