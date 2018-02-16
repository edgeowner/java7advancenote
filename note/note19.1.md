## 分支/合并框架
 前面的知识点都利用率 现在计算机提供的并行机制，然而我们还需要粒度更细的并行机制。例如：考虑使用递归计算Fibonacci数列的方法，
 ```java
 finonacci( n - 1) + finonacci( n - 2)
 ```
可以将这两个子任务分配给每个新的线程，当他们计算完成时，将结果相加。事实上，每个字问题的计算又可以分解为两个子问题，**直到不可细分位置** 这类算法被称为**分治算法**复杂的问题被分解为较小的问题，在根据字问题的解推导出原始问题的解。这样问题就容易并行化。Java SE 7引入了新的分支/合并(Fork/Join)框架以简化这类分治算法的实现。

大型任务被分解为若干块，然后放入队列用于后续计算，在队列中，任务还可以将自身分解为更小的部分。线程会从队列中能够如取出任务并执行。当所有线程结束后 ，将各部分结果合并得到最终结果。“分支”是指任务分解，“合并”是指结果合并。每个工作线程都维着任务的双端队列。队列中后来的任务先执行。当工作中没有任务时需要执行的时候，会尝试从其他线程“窃取”任务，如果”窃取失败，没有其他 工作可做，就会下线。“窃取”的好处是减少了工作队列中争用情况。

像Task这样的大型任务将被分解为两个或更多的子任务，每个子任务可以进一步分解为新的子任务。直到子任务变得足够简单并得到解决。子任务敌对得到解决。

### ForkJoinPool类

ForkJoinPool类是用于执行ForkJoinTask的ExecutorSerivce。与其他ExecutorService的不同之处在于ForkJoinPool采用了前面提到的“工作窃取”机制。在构造过程中，可以在构造函中指定线程池的大小。如果使用的是默认无参构造函数，那么会创建大小等于可用处理器数量的线程池。尽管已之地功能线程池的大小，但线程还会在尝试维护更多活跃线程的任意时刻动态调整自身大小。ForkJoinPool提供了相应方法用于管理和监控那些提交的任务。ForkJoinPool与其他ExecutorService的另一个重大区别在于：线程池需要**在程序结束时显示停止**,因为其中所有的线程都处于**守护状态**

有三种不同的方式可以将任务提交给ForkJoinPool。在异步执行模式下，可以调用execute方法，并将ForkJoinTask作为参数。至于任务本身需要调用fork方法将任务在多个线程间分解。如果需要等待计算结果，可以调用ForkJoinPool的invoke方法。在ForkJoinTask中，可以接着调用invoke方法。invoke方法开始执行任务并在任务结束后返回结果。如果底层失败就会抛出异常或错误。最后可以通过调用ForkJoinPool的submit方法将任务提交给线程池，submit会返回一个Future对象，可以使用该对象检查任务状态和获取执行任务的结果。

### ForkJoinTask类
ForkJoinTask类是运行在前面提到的ForkJoinPool中，用来创建任务的抽象类，RecursiveAction和RecursiveTask仅有两个直接子类。任务在做提交给ForkJoinPool后，便开始执行。ForkJoinTask仅包含两个操作---分支和合并一旦开始执行，就会启动其他子任务。合并操作会等待子操作结束并在结果后提取运行结果。ForkJoinTask实现类Future接口，是Fiture轻量级形式。Future接口的get方法实现，可用于等待任务结束并取得结果。还可以通过invoke方法执行任务。该方法会在任务结束后返回结果，invokeAl方法可以接受任务集合作为参数，该方法会分解出指定集合中的所有任务，并在每个人物结束后返回，或异常时。

ForkJoinTask类提供若干检查任务执行状态的方法，只要任务结束，不管什么方法，`isDone`方法都用于检查任务是否结束，返回true表示任务结束。`isCompleledAbnormally`方法用于检测任务是否在被取消并且没有异常的情况下正常结束。返回true表示正常结束。
`isCancelled`方法用于检查任务是否被取消，返回true表示正常取消。

通常情况下不会直接继承ForkJoinTask类，相反，会创建基于RecursiveTask或RecursiveAction的类。两者均为ForkJoinTask的抽象子类。RucursiveTask用于**返回结果的任务，** 而RecursiveAction用于不返回结果的任务。无论使用哪一种，都要在子类中实现compute方法，并在其中执行任务的主要计算。

### 大型浮点数数组排序
```java
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

```
