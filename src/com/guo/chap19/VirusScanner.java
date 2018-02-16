package com.guo.chap19;


import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by guo on 2018/2/16.
 * 演示任务调度执行
 * 需求：
 * 如何让任务以一定的频率执行。
 * 1、该应用是以固定频率执行的病毒扫描程序。
 * 2、当扫描开始时，程序弹出窗口以显示扫描进度，当磁盘上所有文件被扫描之后，任务会停止。
 * 3、每次扫描都需要不同的时间，通过让线程随机睡眠一段时间来模拟这个过程。
 * 4、扫描结束之后，状态窗口会被关闭，知道下次扫描才会弹出，
 */
public class VirusScanner {
    private static JFrame appFrame;
    private static JLabel statusString;
    private int scanNumber = 0;
    //1、调用Executors类的newScheduledThreadPool方法来创建线程池。
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private static final GregorianCalendar calendar = new GregorianCalendar();
    private static VirusScanner app = new VirusScanner();

    /**
     * scanDisk方法执行实际的扫描工作
     */
    public void scanDisk() {
        //2、使用线程池中的线程来解决多重并发扫描。
        final Runnable scanner = new Runnable() {
            @Override
            public void run() {
                try {
                    //将状态窗口显示给用户
                    appFrame.setVisible(true);
                    scanNumber++;
                    Calendar cal = Calendar.getInstance();
                    //显示扫描数以及扫描开始时间，接下来，让当前线程随机睡一段时间。
                    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.MEDIUM);
                    statusString.setText(" Scan" + scanNumber + " started at" + df.format(cal.getTime()));
                    //常数1000是用来确保窗口至少显示1秒。在实际程序中，病毒扫描代码会放在sleep语句所在的位置。
                    //让线程休眠是假装病毒扫描持续一段时间，
                    //当线程从休眠中唤醒时，我们隐藏了窗口，这让用户感觉当前一轮已经结束。
                    //题外话1：请卸载国产360，QQ管家，小白可以无视。需要的组件可以下载绿色版。（明明是一个开源软件，你却说那高危险。明明是https://www.github.com开头。）
                    //题外话2：感谢 架构@奇虎360，@江湖人称小白哥。谢谢你的心意，能力没到那，你还不能成为我职业生涯的第一位贵人。骚年，加油吧，越努力，越幸运。
                    Thread.sleep(1000 + new Random().nextInt(10000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        //重点：3、使用之前创建的调度器来让扫描程序以固定频率执行。
        //         a、扫描任务在最初的一秒延迟之后会以每隔15秒的频率运行
        //         b、调用器会返回一个Future对象，用于之后取消扫描任务。
        //         c、为了能够进行取消操作，创建另一个匿名线程。
        //         d、以下代码所有时间单位为秒，目前只是模拟的效果。
        //         e、在实际应用中，病毒扫描应当每天或每几小时执行一次
        final ScheduledFuture<?> scanManager = scheduler.scheduleAtFixedRate(scanner, 1, 15, TimeUnit.SECONDS);
        /**
         * 匿名线程
         * 这个线程只在60秒延迟之后运行一次，模拟会以一分钟的总时间周期执行
         * 每隔15秒，病毒扫描状态窗口会弹出，并且显示请留1秒，或更长时间。
         */
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                //4、取消病毒扫描任务，并关闭调度器和状态窗口
                scanManager.cancel(true);
                scheduler.shutdown();
                appFrame.dispose();

            }
        }, 60, TimeUnit.SECONDS);
    }

    /**
     * 不是重点的main方法：
     * 创建状态窗口、设置并调用scanDisk方法。
     * 注意：主线程会在之后立刻结束，而在scanDisk方法中创建的线程会在接下来一分钟内继续运行。
     */
    public static void main(String[] args) {
        appFrame = new JFrame();
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        appFrame.setSize(400, 70);
        appFrame.setLocation(dimension.width / 2 - appFrame.getWidth() / 2,
                dimension.height / 2 - appFrame.getHeight() / 2);
        statusString = new JLabel();
        appFrame.add(statusString);
        appFrame.setVisible(false);
        app.scanDisk();

    }
}
