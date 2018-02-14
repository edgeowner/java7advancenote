package com.guo.chap21;

/**
 * Created by guo on 2018/2/14.
 * 动态方法调用器
 */
public class DynamicInvoker {
    public static void main(String[] args) {
        DynamicInvoker app = new DynamicInvoker();
        app.printGreeting("guo", 5);
        System.out.println("\nDynamicInvoker of printGreeting methos");
        try {
            app.getClass().getMethod("printGreeting", new Class[]{
                    Class.forName("java.lang.String"), Integer.TYPE}).
                    invoke(app, new Object[]{"xiaoxu", new Integer(3)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void printGreeting(String name, int numberOfTimes) {
        for (int i = 0; i < numberOfTimes; i++) {
            System.out.println("Hello" + name);
        }
    }
}
