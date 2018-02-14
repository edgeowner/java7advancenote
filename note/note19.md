![](https://i.imgur.com/obzyV18.jpg)
## Callabl、Future、Executors与分支/合并框架

## 19.1 CaLlable和Future接口

创建线程要么是实现Runnable接口，要么实现Thread类继承。虽然这么做很简单，但创建出的线程会受到严重的限制--run方法不返回任何值给创建者。因此，许多程序员采用将返回值写到文件中这样的不完美解决方案。Runnable的另一个问题在于不能抛出任何异常，因此必须在run方法中处理所有的异常。幸运的是，J2SE5提供了Callable和Future接口，用于解决此类的需求，Thread模拟了一类 **没有返回结果的任务的执行，** 而Callable则是模拟 ** 具有返回结果的任务的执行。** Future更进一步，模拟一类 **可检查进度并可以取回结果的任务的执行**

### 19.1.1 Callable接口
CCallable接口类似于Runnable接口，拥有接受单个参数的call方法。
```java
public interface Callable<V> {
  V call() throws Exception
}
```
call方法可以返回类型参数中指定的任意类型。需要注意的是，call方法和Runnable接口中的run方法并不相同，这里的call方法会抛出被检查的异常。使用Callable<void>可实现没有返回值的Callable。

不能直接将Callable提交到Thread中执行，而是必须使用`ExecutorService`来执行Callable对象。，可以通过调用submit方法来完成这项任务。
```java
<T> Future <T> submit (Callable <T> task)
```
submit方法返回一个Future对象

### 19.1.2 Future接口
当调用方法将任务提交到Executor时，Executor会返回Future对象给调用方，Future接口的定义如下；
```java
interface Future <V>
```
其中V代表的是Future中get方法返回值类型。调用方通过这个Future对象获取所请求任务的控制权。get方法会将结果返回给调用方。如果计算没有结束，get方法则会一直等下去，重载的get方法可以接受时限参数，从而限制任务的最长执行时间。isDone方法检测热恩物是否执行结束，如果结束，就返回true。cancel方法会在任务正常结束之前尝试取消，如果任务正常结束之前被取消，isCancelled会返回true

FutureTask是封装类实现类Future与Runnable接口，并提供了一种便捷的方式，可以将Callable转为Future与Runnable。

Caller是实现类Callable接口的Java类。Callable接口仅含有call方法，程序员在实现Callable接口时必须实现call方法。call方法通常会包含服务实现。要调用服务就必须实例化Caller类并调用call方法。如果希望上述过程异步完成，就需要采用某些特定机制调用call方法。Callbale接口和Runnable接口不同，后者可以放在Thread类的构造函数中执行，Java提供了另一个名为`ExecutorService`的类用于执行可调用任务。调用方首先创建或获取ExecutorServie实例，并提交可调用任务以待执行。接下来框架会返回Future对象给调用方，这个Future队形可以用于检查可调用任务的状态并获取执行结果。


重点知识在main函数中

```java
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

    public static void main(String[] args) throws Exception {
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
}

```
为了清晰，我把这两个方法从类中拿出来了。
```java
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
```

```
           一月     二月     三月     四月     五月     六月     七月     八月     九月     十月    十一月    十二月

Client ID : 100      89      63      98      51       1      58      69      67      81      97      98      64
Client ID : 101      66      83       3      83      75      43      10      83      92      39      27      75
Client ID : 102      10      88      74      38      33      91      43      82      93      94      28      25
...省略..
Client ID : 197      50      39      43      33      18      61      67      21      11      56      11      19
Client ID : 198      64       5      10      63      97      52      99      35      15       9      73      29
Client ID : 199      47      95      62      51       7      78      68      60      53      58      23      56

Totaling for client 101 completed
Totaling for client 102 completed
Totaling for client 110 completed
...省略..
Totaling for client 105 completed
Totaling for client 109 completed

The annual turnover (bags) : 60152

```
