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

### Executors类
Executors类允许参加线程池并返回`ExecutorService`对象，执行器提供了将任务提交与对任务进行解耦的标准方法，除了对基本的线程生命周期提供支持外，窒息功能其还提供统计收集，应用管理及监控方面的功能。这一切都基于 **生产者-消费者模式。** 使用这种设计模式可以对大型并发应用程序很好的进行扩展。

使用这种服务对象，可以运行`Runnable和Callable`类的实力，你只需要做的是提交任务给服务对象就可以。`ExecutorService`会从线程池中选择线程，并将Runnable对象提交给线程任务。当任务结束时，线程并不会销毁 ，而是返回到线程池中继续执行后续的其他任务，这样可以 **避免创建和销毁线程带来的额外开销**


#### Executors类有许多静态方法可用来创建线程池：

1、`newFixedTHreadPool`方法能够创建固定大小的线程池。线程池中的线程将被用来处理任务请求，如果线程处于空闲状态，线程不会销毁，而是会被存放线程池中一段不确定的是将

2、`newCachedThreadPool`，使用该方法创建的线程池中的线程会在空闲60秒之后自动销毁,

3、`newSingleThreadExecutor`该方法仅创建一个线程，当任务结束后不会销毁而是用于处理其他任务。对于多个任务同时请求，则使用队列来维护所有待处理的请求。随后会顺序执行。

4、`newScheduledThreadPool`可以把它看作是java.util.Timer的替代品，该方法创建固定大小的线程池用来调度执行任务，并返回一个`ScheduledExecutorService`对象,该对象提供了若干个方法用于执行任务的调度执行。

#### 创建线程池以进行任务调度

有时创建可在一定时间延迟后执行的线程，可以设置一个报警器在一段时间过后报警。在某些情况下，你也希望以 **一定的频率或固定时间间隔反复执行线程。**
比如病毒扫描，你可以使用`newScheduledThreadPool`类实现的执行器服务，每24小时运行一次病毒扫描。如果有多个磁盘或大容量的磁盘需要扫描，将扫描的任务分解为多个单元。让每个单元扫描某个特定的磁盘。

另一凸显此服务很实用的应用场景是新闻聚合器。聚合器从多个新闻源收集最新新闻，并将它们排列在客户端以供阅读，多个数据源获取可以并发执行，而这根据目标数据源的网络状况，花费的时间会不一样。客户端和数据源的同步会周期性地执行。如果这样的同步操作频率很高，新的同步操作和当前正在执行的操作就有可能出现重叠。在这种情况下，最好给每次任务的执行设固定的时间间隔，`ScheduledExecutosService`可以帮你实现这样的需求。

### ScheduledExecutorService类

1、`ScheduledExecutorService` 类提供了名为schedule的方法用于设定任务的未来执行。schedule方法有两个重载版本：
```java
//Creates and executes a ScheduledFuture that becomes enabled after the given delay.
<V> ScheduledFuture<V> 	schedule(Callable<V> callable, long delay, TimeUnit unit)
//Creates and executes a one-shot action that becomes enabled after the given delay.
ScheduledFuture<?> 	schedule(Runnable command, long delay, TimeUnit unit)
```
schedule方法接收三个参数：Callable和Runnable接口、延迟时间以及时间单位。该方法安排由 `Callable和Runnable`指定的任务在给定的延迟时间后执行。时间单位 由该方法的第三个参数指定。方法会返回一个**Future对象**给调用方。

2、除了这个简单的延迟执行之外，ScheduledExecutorService类还提供了scheduleAtFixedRate方法，该任务可以指定任务按照一定的频率执行。
```java
//Creates and executes a periodic action that becomes enabled first after the given initial delay, and subsequently with the given period;
//that is executions will commence after initialDelay then initialDelay+period, then initialDelay + 2 * period, and so on.
ScheduledFuture<?> 	scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit)
```
第一次执行发生在给定的延迟之后，后续执行发生在“延迟+固定时间”，“延迟+2*固定周期”，依次类推，这种方法可以用于**病毒扫描**

3、`scheduleWithFiedDelay`方法在给定延迟之后第一次执行任务。之后按照固定好的时间间隔执行，时间间隔递归你以为本次任务运行到下一次任务的开始。这类调度可以用于新闻聚合应用。
```jaav
//Creates and executes a periodic action that becomes enabled first after the given initial delay, and subsequently with the given delay between the termination of one execution and the commencement of the next.
ScheduledFuture<?> 	scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit)
```

### 任务的调度执行(重点在匿名线程)
```java
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


}

```
### 主函数(不是重点)
```java
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
```

### 获取首个已结束的运行结果

之前已经学了如何将任务提交给执行器立即执行、延迟以及周期性的运行 ([计算年销售额](https://segmentfault.com/a/1190000013292805)) 还了解到执行器可以提供并维护多个线程并发的执行任务  ([模拟可取消任务的股票交易处理程序](https://segmentfault.com/a/1190000013294416)) 。在某些情况下，当提交多个任务给执行器，你可能希望处理任意以结束任务的结果，而不像等到每个任务都执行结束。目前只用过执行器的get方法会等待任务结束。当任务提交时，可以创建循环来获取每个计算结果，代码如下：
```java
for(Future<T> result : results) {
  result.get();
}
```

这样就可以顺序的获取结果，但如果某个特定的任务需要长时间才能结束，那么当前的get调用会一直**阻塞**.在这种情况下，即使其他任务已经提前完成，也无法获取结果，为了解决这个问题，可以使用`ExecutorCompletionService`类，该类会检测提交给执行器的任务，通过take方法，可以一个个地获取到任务执行的结果。
