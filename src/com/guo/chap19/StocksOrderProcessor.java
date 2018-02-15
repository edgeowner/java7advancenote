package com.guo.chap19;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Created by guo on 2018/2/15.
 * 演示可取消任务的股票交易处理程序
 */
public class StocksOrderProcessor {
    static final int MAX_NUMBER_OF_ORDER = 1_000_000;       //交易订单
    //1、创建数量为1000的线程池来执行订单。经过测试1000个线程，CPU维持在70%-80%左右。
    static private ExecutorService executor = Executors.newFixedThreadPool(1000);
    //2、创建ArrayList来保存执行执行订单的引用
    static private List<Future> ordersToProcess = new ArrayList<>();

    /**
     * 创建内部私有类OrderExecutor以处理订单执行的业务逻辑。
     * OrderExecutor实现了Callable接口以支持异步调用。
     */
    public static class OrderExecutor implements Callable {
        int id = 0;
        int count = 0;
         //3、传入整型变量id来记录订单编号。
        public OrderExecutor(int id) {
            this.id = id;
        }

        @Override
        public Object call() throws Exception {
            try {
                //4、将技术设为1000，每次计数前，让线程休眠一段不同的时间
                while (count < 1000) {
                    count++;
                    //5、通过让线程休眠一段不同的时间，模拟现实中每个订单需要不同的处理时间。
                    Thread.sleep(new Random(
                            System.currentTimeMillis() % 10).nextInt(10));
                }
                System.out.println("Successfully executed order:" + id);
            } catch (Exception ex) {
                throw (ex);
            }
            return id;
        }
    }

    public static void main(String[] args) {
        
        System.out.printf("Submitting %d trades%n", MAX_NUMBER_OF_ORDER);
        //6、通过循环遍历，提交一百万订单。
        for (int i = 0; i < MAX_NUMBER_OF_ORDER; i++) {
            submitOrder(i);
        }
        //7、创建“邪恶”线程尝试随机的取消某些订单。
        //每当执行到这里时，就会创建一些取消请求，并针对待处理的订单列表中存储的Future对象执行。
        new Thread(new EvilThread(ordersToProcess)).start();


        System.out.println("Cancelling a few order at random");
        try {

            //8a、某些订单可能已经被处理，模拟器就会继续处理剩余订单。
            // b、如果订单在执行器分配线程之前被取消，就将永远不会执行。
            // c、为了留有足够的时间结束所有待处理的订单，让执行器等待30秒。
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        System.out.println("Checking status before shutdown");
        int count = 0;
        //9a、通过循环遍历，统计有多少订单被成功取消。
        // b、对于订单中每一个Future对象，调用isCancelld方法。
        // c、如果对应的被成功取消，则方法返回true
        for (Future f : ordersToProcess) {
            if (f.isCancelled()) {
                count++;
            }
        }
        System.out.printf("%d trades cancelled%n", count);
        //10、立即停止执行器释放分配的所有资源 （貌似我的百万订单根本停不下来啊，求解！）
        executor.shutdownNow();

    }

    private static void submitOrder(int id) {
        //6、a 创建一个Callable实例，每个实例都有为一个的Id供跟踪
        Callable<Integer> callable = new OrderExecutor(id);
        //6、b 调用ExecutorService的submit方法可将创建的任务提交以待执行。
        //并且将submit方法返回的对象放到待处理订单的数组里列表中。
        ordersToProcess.add(executor.submit(callable));
    }

}

/**
 * 邪恶线程，随机的取消某些订单。
 */
class EvilThread implements Runnable {
    private List<Future> ordersToProcess;
     //1、在构造函数中传入待处理的订单列表，这样可以对某一些Future对象发送取消请求。
    public EvilThread(List<Future> future) {
        this.ordersToProcess = future;
    }

    @Override
    public void run() {
         //2、创建100个取消请求
        Random myNextKill = new Random(System.currentTimeMillis() % 100);
        for (int i = 0; i < 100; i++) {
            //3、随机选择Future对象进行取消。
            int index = myNextKill.nextInt(StocksOrderProcessor.MAX_NUMBER_OF_ORDER);
            //4、调用Future对象的cancel方法以发送请求，并将cancel方法的参数设为ture。表示任务可能会在执行过程中被中断。
            boolean cancel = ordersToProcess.get(index).cancel(true);
            //5、判断是否取消成功，
            if (cancel) {
                System.out.println("Cancel Order Succeded:" + index);
            } else {
                System.out.println("cancel Order Failed" + index);
            }
            try {
                //6、在每两个请求之间让“邪恶”线程睡一会。
                Thread.sleep(myNextKill.nextInt(100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
