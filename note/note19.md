![](https://i.imgur.com/obzyV18.jpg)
## Callabl、Future、Executors与分支/合并框架

## 19.1 Callable和Future接口

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

### 19.1.5 FutureTask类
FutureTask类同时实现类Runnable接口和Future接口。因此，FutureTask类技能拥有Runnable接口提供的异步计算能力，也能拥有Future接口提供的返回值给调用方的Future对象取消任务的能力。FutureTask类可以用于封装Callable和Runnable接口。

```java
//Future<Integer> future = executor.submit(Callable);
FutureTask<Integer> future = new FutureTaks<Integer>(Callable);
future.run()
```
run方法会调用任务，并将任务的计算结果赋值给Future对象。

也可以将FutureTask实例交给Executor对象用于执行。
```java
executor.submit(future);
```
由于FutureTask类也实现了Future接口，因此FutureTak接口实例可以用来取消任务，检查任务等。

#### 19.1.6 创建可取消的任务。
取消任务可以使用执行器返回的Future对象，而创建和执行任务可以使用前面讨论的FutureTask类。

开发可以处理上百万次请求的模拟器。会发送数千条数据交易请求给模拟器。**模拟器包含的线程池用于处理这些请求。**
还将编写一个“邪恶”的线程，**它会随机选择诺干订单，并且尝试取消他们。如果订单已经执行，取消请求会失败。**
如果在订单在被分配给线程执行之前接收到取消请求，那么订单会被取消。**如果交易订单正在执行。并且线程可被中断，**
那么在订单处理过程中接收的取消请求会结束剩余的处理流程。从而取消订单。

```java

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
}
```
## 主函数

```java
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
```
## 邪恶线程
----
```java
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
                System.out.println("cancel Order Failed:" + index);
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

```
程序运行后部分输出如下:

```
Submitting 1000000 trades

Successfully executed order:28
Successfully executed order:380
Successfully executed order:288
Successfully executed order:120
Cancelling a few order at random
Successfully executed order:116
Successfully executed order:1004
Successfully executed order:1005

Cancel Order Succeded:698021
cancel Order Failed:98832(重点)
...
Successfully executed order:12268
Successfully executed order:12420
Successfully executed order:13190
Successfully executed order:12199

Checking status before shutdown
99 trades cancelled(重点)
Successfully executed order:14045      //估计Kill线程太多了,遗漏这个了.求解.
```
从输出可以看到:
- 订单698021被成功取消,这个订单还未执行,
- 订单98832的取消请求失败了,因为这个订单已经执行结束.
- 在发送的100个请请求中,有99个被成功取下.也可能是100%,取决你的电脑配置.
