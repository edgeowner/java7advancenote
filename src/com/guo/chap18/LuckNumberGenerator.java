package com.guo.chap18;

import java.util.Random;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;

/**
 * Created by guo on 17/2/2018.
 * 基于TransferQueue的幸运数字生产器
 */
public class LuckNumberGenerator {
    public static void main(String[] args) {
        //1、创建TransferQueue实例用于存储String
        TransferQueue<String> queue = new LinkedTransferQueue<>();
        //2、创建生产者线程并启动，生产者接收队列作为参数，
        Thread producer = new Thread(new Producer(queue));
        producer.setDaemon(true);
        producer.start();
        for (int i = 0; i < 10; i++) {
            //在创建生产者线程之后，在创建十个消费者线程，每个消费者线程间存在两秒间隔。
            Thread consumer = new Thread(new Consumer(queue));
            consumer.setDaemon(true);
            consumer.start();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

/**
 * 生产者Producer实现类Runnable接口，
 * Runnable将构造函数接受的TransferQueue对象存储在私有成员变量中，
 */
class Producer implements Runnable {
    private final TransferQueue<String> queue;

    public Producer(TransferQueue queue) {
        this.queue = queue;
    }

    private String producer() {
        return " your lucky number " + (new Random().nextInt(1000));
    }

    @Override
    public void run() {
        try {
            //无限循环，并检查消费者是否在等待幸运数字，如果的确等待就产生性欲数字并传递给等待的消费者。
            while (true) {
                if (queue.hasWaitingConsumer()) {
                    queue.transfer(producer());
                }
                //检查另一个等待的消费者之前，让生产者睡眠一秒钟。
                TimeUnit.SECONDS.sleep(1);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

/**
 * 消费者Consumer也实现类Runnable接口，
 */
class Consumer implements Runnable {
    private final TransferQueue<String> queue;

    public Consumer(TransferQueue<String> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            //通过调用TransferQueue对象的take方法来接收习惯那样数字，
            System.out.println("Consumer " + Thread.currentThread().getName() + queue.take());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
