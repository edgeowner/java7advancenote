package com.guo.chap19;

import javax.sound.midi.Soundbank;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Created by guo on 16/2/2018.
 * 大型浮点数数组排序
 * 需求：
 *      1、假设有一些数目非常大的浮点数(一百万个)，需要编写一个程序将这些数字按升序排序，
 *      2、针对单线程的常用排序算法需要消耗过长的时间才能生成排好序的数组。
 *      3、这类问题恰好与分治模式相契合。我们将数组分解成多个较小的数组，并在每个数组中独立进行排序。
 *      4、最后将不断归并排好序的数组合并成更大的数组以创建最终排好序的数组。
 */
public class ParallelMergeSort {
    private static ForkJoinPool threadPool;
    private static final int THRESHOLD = 16;

    /**
     * 4、1 sort方法接受对象数组作为参数，目标是对数组进行升序排序。
     * @param objectArray
     */
    private static void sort(Comparable[] objectArray) {
        //4、2 声明临时目标数组用于存储排序结果，大小等同于输入数组。
        Comparable[] destArray = new Comparable[objectArray.length];
        //4、3 创建一个SortTask对象，并调用invoke方法将它提交给线程池。
        //4、4 SortTask接受4个参数，待排序的数组，已经用于存储排序后的对象目标数组，源数组中待排序元素的开始索引与结束索引。
        threadPool.invoke(new SortTask(objectArray, destArray, 0, objectArray.length - 1));
    }

    /**
     * 重点：
     *    --5、SortTask其功能继承自RecursiveAction类。
     *      a、此排序算法不直接返回结果给调用方，因此基于RecursiveAction类。
     *      b、如果算法具有返回值，比如计算 finonacci( n - 1) + finonacci( n - 2) ，那么就应该继承RecursiveTask类哦。
     *      c、作为SortTask类的具体实现的一部分，需要重写compute抽象方法，
     */
    static class SortTask extends RecursiveAction {
        private Comparable[] sourceArray;
        private Comparable[] destArray;
        private int lowerIndex, upperIndex;

        public SortTask(Comparable[] sourceArray, Comparable[] destArray, int lowerIndex, int upperIndex) {
            this.sourceArray = sourceArray;
            this.destArray = destArray;
            this.lowerIndex = lowerIndex;
            this.upperIndex = upperIndex;
        }

        /**
         * 6、compute方法用于检查带排序原色的大小。
         */
        @Override
        protected void compute() {
              //6、1 如果小于预定义的值THRESHOLD(16),就调用insertSort方法进行排序 。
            if (upperIndex - lowerIndex < THRESHOLD) {
                insertionSort(sourceArray, lowerIndex, upperIndex);
                return;
            }

            //6、2 如果没有超过，就创建两个子任务并递归进行调用。
            //6、3 每个子任务接收原有数组的一半作为自己的源数组，
            //6、3 minIndex定义了原有数组的中心点。调用invokeAll，将这两个人物提交给线程池
            //6、4 分解任务为子任务的过程会一直递归执行，直到每个子任务变得足够小位置。
            //6、5 所有分解得到的子任务都被递交给线程池，当它们结束时，compute方法会调用merge方法
            int minIndex = (lowerIndex + upperIndex >>> 1);
            invokeAll(new SortTask(sourceArray, destArray, lowerIndex, minIndex), new SortTask(sourceArray, destArray, minIndex + 1, upperIndex));
            merge(sourceArray, destArray, lowerIndex, minIndex, upperIndex);
        }
    }

    /**
     *归并算法
     */
    public static void merge(Comparable[] sourceArray, Comparable[] destArray, int lowerIndex, int mindIndex, int upperIndex) {
        //1、 如果源数组中间索引小于等于右边的则直接返回。
        if (sourceArray[mindIndex].compareTo(sourceArray[mindIndex + 1]) <= 0) {
            return;
        }
        /**
         * 2、调用底层实现的数组拷贝，是一个本地方法
         * void arraycopy(Object src,  int  srcPos,Object dest, int destPos,int length);
         *  src      the source array.
         * srcPos   starting position in the source array.
         * dest     the destination array.
         * destPos  starting position in the destination data.
         * length   the number of array elements to be copied.
         */
        System.arraycopy(sourceArray, lowerIndex, destArray, lowerIndex, mindIndex - lowerIndex + 1);
        int i = lowerIndex;
        int j = mindIndex + 1;
        int k = lowerIndex;
         //3、 将两个数组进行归并，
        while (k < j && j < upperIndex) {
            if (destArray[i].compareTo(sourceArray[j]) <= 0) {
                sourceArray[k++] = destArray[i++];
            } else {
                sourceArray[k++] = sourceArray[j++];
            }
        }
        System.arraycopy(destArray, i, sourceArray, k, j - k);
    }

    /**
     * 插入排序 (得好好研究下算法了)
     * 1、从后向前找到格式的位置插入，
     * 2、 每步将一个待排序的记录，按其顺序大小插入到前面已经排好的子序列合适的位置。
     * 3、直到全部插入位置。
     */
    private static void insertionSort(Comparable[] objectArray, int lowerIndex, int upperIndex) {
        //1、控制比较的轮数，
        for (int i = lowerIndex + 1; i <= upperIndex; i++) {
            int j = i;
            Comparable tempObject = objectArray[j];
            //2、后一个和前面一个比较，如果前面的小，则把前面的赋值到后面。
            while (j > lowerIndex && tempObject.compareTo(objectArray[j - 1]) < 0) {
                objectArray[j] = objectArray[j - 1];
                --j;
            }
            objectArray[j] = tempObject;
        }
    }

    /**
     * 3、1 使用Random类生成范围在0-1000之间的数据点，并使用这些随机生成的数据初始化数组每一个元素。
     * 3、2 创建好的数据点的数目等同于函数参数的数目，这个例子是1000，增大该数字，可以验证并行排序算法的效率。
     * @param length
     * @return
     */
    public static Double[] createRandomData(int length) {
        Double[] data = new Double[length];
        for (int i = 0; i < data.length; i++) {
            data[i] = length * Math.random();
        }
        return data;
    }

    /**
     * 主函数
     *
     * @param args
     */
    public static void main(String[] args) {
        //1、获取当前运行代码所在机器上的可以用处理器数目
        int processors = Runtime.getRuntime().availableProcessors();
        System.out.println("no of processors:" + processors);
        //2、1创建大小等同于处理器数目的线程池，这一数目是运行在可用硬件最佳数目
        //2、2如果创建更大的线程池，就会有CPU竞争的情况发生，
        //2、3通过实例化ForkJoinPool，并将线程池大小作为参数传入构造函数以创建线程池
        threadPool = new ForkJoinPool(processors);
        //3、构造随机数据的数组，并输入以方便严重
        Double[] data = createRandomData(100000);
        System.out.println("original unsorted data：");
        for (Double d : data) {
            System.out.printf("%3.2f ", (double) d);
        }
        //4、调用sort方法对生成的数据进行排序，并再次输出数组，以验证数组已经排好序。
        sort(data);

        System.out.println("\n\n Sorted Array ");
        for (double d : data) {
            System.out.printf("%3.2f ", d);
        }
    }
}
