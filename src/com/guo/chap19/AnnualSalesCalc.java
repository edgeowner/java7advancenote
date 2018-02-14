package com.guo.chap19;

import java.text.DateFormatSymbols;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by guo on 2018/2/15.
 * 计算年销售额
 */
public class AnnualSalesCalc {
    private static int NUMBER_OF_CUSTOMERS = 100;           //表示矩阵的行数
    private static int NUMBER_OF_MONTHS = 12;               //表示矩阵的列数
    private static int salesMatrix[][];                     //代表尚未创建的二维整形数组。

    /**
     * Summer用来计算每行的和。
     * Summer类被声明在AnnualSalesCalc的内部类，并且为private和static。
     * 之所以声明为private，是因为Summer类不会用在其他地方。
     * 声明为静态是应为Summer类会被静态的main方法调用。Summer类还实现类Callable接口。
     * 构造函数接受ID的整数值，并存储在类变量中以备后用。
     */
    private static class Summer implements Callable {
        private int companyID;

        public Summer(int companyID) {
            this.companyID = companyID;
        }

        /**
         * Call方法被映射为任何类型的泛型类，在这里被映射为Integer类型
         * call方法通过for循环遍历来计算矩阵特定行内的所有元素的总和
         */
        public Integer call() throws Exception {
            int sum = 0;
            //
            for (int col = 0; col < NUMBER_OF_MONTHS; col++) {
                sum += salesMatrix[companyID][col];
            }
            //在求和计算完成之后，程序会打印一条消息给用户，
            // 表明这个Callable对象的任务已经结束，并且无论什么时候被询问，都可以将计算结果返回给调用者。
            System.out.printf("Totaling for client 1%02d completed%n", companyID);
            return sum;
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        generateMatrix();
        printMaxtrix();

        //我们需要执行者服务调用Callable对象，Executor类提供此服务
        //newFixedThreadPool是Executors类的静态方法，接受一个整型参数，
        //该参数的值决定了这个方法创建的线程数目。
        //该方法会创建固定的线程池，用于执行不同的任务，并且在结束时会返回一个ExecutorService实例。
        ExecutorService executor = Executors.newFixedThreadPool(10);

        //声明的Set对象用于存储Future对象，从而监控提交的任务。
        Set<Future<Integer>> set = new HashSet<Future<Integer>>();
        for (int row = 0; row < NUMBER_OF_CUSTOMERS; row++) {
            //为矩阵的每一行都实例化类Summer对象，Summer对象同时也是Callable对象
            Callable<Integer> callable = new Summer(row);

            //通过调用前面创建的executor对象的submit方法来运行这个Callable对象；
            //submit方法将Callable对象提交给线程池中的某一个线程，并返回一个Future对象给调用者。
            //调用方可以使用这个Future对象的get方法来获取计算结果。
            Future<Integer> future = executor.submit(callable);

            //我们将返回的Future对象放在集合中，以便在最后阶段获取集合中所有元素之和，进而计算总和
            set.add(future);
        }
        int sum = 0;
        //在提交 所有任务之后，使用for-each循环计算总和
        for (Future<Integer> future : set) {
            sum += future.get();
        }
        System.out.printf("%n The annual turnover (bags) : %s%n%n", sum);
        //最后关闭执行者服务，以释放分配的所有资源。
        executor.shutdown();

    }

    /**
     * 生成矩阵
     */
    private static void generateMatrix() {
        salesMatrix = new int[NUMBER_OF_CUSTOMERS][NUMBER_OF_MONTHS];
        for (int i = 0; i < NUMBER_OF_CUSTOMERS; i++) {
            for (int j = 0; j < NUMBER_OF_MONTHS; j++) {
                //数组中的每个元素都被初始化为0-99之间的随机数。
                salesMatrix[i][j] = (int) (Math.random() * 100);
            }
        }
    }

    /**
     * 打印矩阵
     */
    private static void printMaxtrix() {
        System.out.print("\t\t");
        String[] monthDisplayName = new DateFormatSymbols().getShortMonths();
        for (String strName : monthDisplayName) {
            System.out.printf("%8s", strName);
        }
        System.out.printf("\n\n");
        for (int i = 0; i < NUMBER_OF_CUSTOMERS; i++) {
            System.out.printf("Client ID : 1%02d", i);
            for (int j = 0; j < NUMBER_OF_MONTHS; j++) {
                System.out.printf("%8d", salesMatrix[i][j]);
            }

            System.out.println();
        }
        System.out.println("\n\n");
    }
}

