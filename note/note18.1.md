## 同步器
J2SE 5.0在语言中添加了几个同步结构作为java.util.concurrent包的一部分，包括信号量、屏蔽、闭锁、和交换器。

### 信号量
当想要n个实体以同步的方式访问共享资源时，信号量是很有用的。信号量的典型应用是服务器应用程序，多个线程以某种方式竞争在数量上有限的资源。例如：具有大量并发访问的网站在任何给定时间，可能会收到多个请求以获得存储在服务器内部数据库中的特定数据。因为创建时间和保存资源方面的原因，数据库连接时十分昂贵的，也许只能创建数据量有限的连接，并保存在连接池中，当访问数据的web请求时，服务器应用程序将连接交给它，提供的连接是当时在连接池中可用的连接。如果不这样，那么web请求会被迫等待，直到现有的用户将某个连接返回到连接池中。在某些情况下，如果需求比较大，服务器应用程序也可能会增加连接池的大小，信号量会帮助你实现这一功能。

**信号量允许n个实体以同步的方式访问m的资源。yusynchronized关键字相比，synchronized关键字只允许单一的实体访问。**

在Java中，Semaphore类实现类信号量，在实例化这个类的时候，指定许可的数目。可以使用acquire方法来取得许可，你可能会获得一个以上的许可，这可通过将信号量指定为acquire方法的参数来实现。这是阻塞调用，会阻塞自己直达某个许可是可用的。或者直到等待的线程终止为止。如果不想让线程在许可上阻塞，可是使用tryAcquire方法，如果没有所需数量的可用许可，该方法会返回false。tryAcquire方法接受一个可选参数，用于指定线程等待许可变为可用的时间。

### 基于信号量的银行柜员示例的
```java
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

```
