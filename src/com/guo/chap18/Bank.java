package com.guo.chap18;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by guo on 17/2/2018.
 * 基于信号量的银行柜员示例的实现
 */
public class Bank {
    //1、COUNT变量控制将会访问银行客户的数目
    private final static int COUNT = 100;
    //2、semaphore变量保存指向创建Semaphore对象的引用
    //   构造函数第一个参数用于指定许可的数目，
    //    构造函数第二个参数指定公平设置，当设置为true是，表示FIFO行为——先进先出。
    private final static Semaphore semaphore = new Semaphore(2,true);

    public static void main(String[] args) {
        //3、设置循环来处理100个客户的请求
        for (int i = 0; i < COUNT; i++) {
            final int count = i;
            //4、为每个客户创建匿名线程
            new Thread() {
                @Override
                public void run() {
                    try {
                        try {
                            //5、通过调用semaphore类的tryAcquire方法，线程试图获取许可，
                            //   第一个参数指定等待时间，第二个参数指定时间为单位。
                            if (semaphore.tryAcquire(10, TimeUnit.MILLISECONDS)){
                                //6、如果获得许可，则线程调用Teller类的getService方法来请求服务，
                                //   需要使用表示当前执行线程的迭代数的参数，
                                Teller.getService(count);
                            }
                        } finally {
                            //7、在线程与银行柜员完成业务后，调用smaphore方法来释放许可，
                            semaphore.release();
                        }
                    }catch (Exception ex){

                    }
                }
            }.start();     //8、启动线程
        }
    }
}

class Teller {
    //Teller定义了名为getService静态方法
    static public void getService(int i) {
        try {
            //打印当前线程执行的id，
            System.out.println("servimg " + i);
            //使线程水米娜某个随机事件。目的是为了模拟每个顾客占用柜台的时间是可变的情况。
            Thread.sleep((long) (Math.random() * 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
