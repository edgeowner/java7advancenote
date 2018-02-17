package com.guo.chap18;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by guo on 17/2/2018.
 * 基于阻塞队列的股票交易服务器
 * 需求：
 *      1、允许交易者往队列中添加出售订单，也可以获取待办的订单
 *      2、在任何给定的时间，如果队列已满，交易者就不得不等待某个位置变为空
 *      3、购买者必须等待，直到队列中有出售订单可用。
 *      4、为了简化情形，假设买方总是必须购买全额数量的可供出售的股票，不可以部分购买。
 */
public class StockExchange {

    public static void main(String[] args) {
        System.out.printf("Hit Enter to terminate %n%n");
        //1、创建LinkedBlockingQueue实例，因为是无限容量，所以交易者可以把任何数量的订单放入队列中，
        //   如果使用ArrayBlockingQueue，那么将会限制每只股票拥有有限次数的交易。
        BlockingQueue<Integer> orderQueue = new LinkedBlockingQueue<>();
        //2、创建Seller卖家实例，Seller是Runnable的实现类。
        Seller seller = new Seller(orderQueue);
        //3、创建100个交易者实例，将自己出售的订单放入队列中，每个出售订单都将会有随机的交易量。
        Thread[] sellerThread = new Thread[100];
        for (int i = 0; i < 100; i++) {
             sellerThread[i] = new Thread(seller);
             sellerThread[i].start();
        }
        //4、创建100个买家实例，选取待售的订单
        Buyer buyer = new Buyer(orderQueue);
        Thread[] buyserThread = new Thread[100];
        for (int i = 0; i < 100; i++) {
            buyserThread[i] = new Thread(buyer);
            buyserThread[i].start();
        }
        try {
            //5、一旦创建生产者和消费者线程，他们会永远保持运行，将订单放入队列以及从队列中获取订单
            //   根据给定时间的负载情况，定期自我阻塞，终止应用程序的方法是用户在键盘上按下Enter键。
            while (System.in.read() != '\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
        //6、main函数会中断所有正在运行的生产者和消费者线程，要求它们中指并退出
        System.out.println("Terminating");
        for (Thread t : sellerThread) {
            t.interrupt();
        }
        for (Thread t : buyserThread) {
            t.interrupt();
        }

    }
}

/**
 * 卖家
 * Seller类实现了Runnable接口并提供了以OrderQueue作为参数的构造函数
 */
class  Seller implements Runnable {
    private BlockingQueue orderQueue;
    private boolean shutdownRequest = false;
    private static int id;
    public Seller(BlockingQueue orderQueue) {
        this.orderQueue = orderQueue;
    }
    @Override
    public void run() {
        while (shutdownRequest == false) {
            //1、在每一次迭代中，为每一次的交易量生产一个随机数
            Integer quantity = (int) (Math.random() * 100);
            try {
                //2、调用put方法，将订单放入队列中，这是阻塞调用，只有在队列容量有限的情况下，
                //    线程才需要等待队列中有出现空的位置
                orderQueue.put(quantity);
                //3、为了方便用户，在控制台打印销售订单的详细信息，以及用于放置销售订单的线程详细信息
                System.out.println("Sell order by" + Thread.currentThread().getName() + ": " + quantity);
            } catch (InterruptedException e) {
                //4、run方法将无限期的运行，定期的向队列中提交订单，通过调用interrupt方法，这个线程可以被另外一个线程中断。
                //   interrupt方法产生的InterruptException异常简单的将shutdownRequest标志设置为true，将导致run方法无限循环终止
                shutdownRequest = true;
            }
        }
    }
}

/**
 *  买家
 *  Buyer类实现了Runnable接口并提供了以OrderQueue作为参数的构造函数
 */
class Buyer implements Runnable{
    private BlockingQueue orderQueue;
    private boolean shutdownRequest = false;
    public Buyer(BlockingQueue orderQueue) {
        this.orderQueue = orderQueue;
    }
    @Override
    public void run() {
        while (shutdownRequest == false) {
            try {
                //1、run方法通过调用take方法，从队列的头部取出待办的交易，
                //   如果队列中没有可用的订单，take方法将阻塞，
                Integer quantity = ((Integer) orderQueue.take());
                //2、为了方便，打印订单和线程的详细信息
                System.out.println("Buy order by " + Thread.currentThread().getName() + ": " + quantity);
            } catch (InterruptedException e) {
                shutdownRequest = true;
            }
        }
    }
}
