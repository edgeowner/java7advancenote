## 阻塞队列和同步器
jiti
Java语言从诞生起就支持多线程和并发，那时候线程提供的是异步，而不是并发。如今，默认情况下，所有机器都配备了多核处理器，
所以软件开发人员需要编写能够充分利用并行机制的应用程序，从而充分利用硬件。在许多情况下，并行机制有利于提高应用程序的性能。

自动报价系统，这样的WEB应用程序需要访问三个数据库--价格数据库，提供了商品的基本价格，客户端数据库，提供了给客户的折扣结构。运费数据库，提供了各种运输方式的基本费用。对于这三个数据库提供的计算和结果的访问应该是相互独立的，最后对这三个结果进行汇总，生成最终报价。

具有多个动画的游戏可以从中获益，通过将每个动画分别独立的处理器上运行。再比如图像处理应用程序，图像的每个像素都需要某种处理，如反显颜色，这受益于将大量的像素分成较小的组，并将每个组分配给独立的核进行处理。当多个事件同时发生时，游戏很更精彩。

**通常顺序操作不能被分解成并行程序，而且应该在单个线程内连续运行** 在一般情况下，为了满足各种性能的扩展需求，可以使用并发框架。将繁重的线性任务分解成可以并行执行的更小的任务。通常CPU密集型应用是利用并行处理的理想选择。

Java虚拟机(JVM)将那些多线程任务映射到多个可用的硬件线程，如果是单线程应用，虚拟机只能用单个执行线程来完成这个任务，并且只能映射到单个硬件线程。即使其他硬件线程是空闲的。更高级别的API可以帮助我们改写成多线程应用程序，基于多核/多处理器的现代虚拟机所作的映射对多线程应用程序的确有很大的好处。


### 阻塞队列
在阻塞队列的帮助下，许多同步问题都可以被公式化。阻塞队列是队列，当线程试图对空队列进行出列操作，或试图向满的队列中插入条目时，队列就会阻塞。直到其他线程向队列中放入数据时才可以移除，同样，直到其他线程从队列中移除条目之后才可以加入。通过使用轮询或等待-通知机制可以实现阻塞队列。就轮询机制来说，读线程周期性的调用队列的get方法，直到队列的消息变为可用。至于等待-通知机制，读线程仅仅是等待队列对象，队列对象会在有条目时通知线程。

- 火车站买票
- 聊天服务器：读操作接收到来的消息，并将它们放入主消息队列。写操作每次从队列中取出一条数据，并将发送到适当的聊天客户端，队列的使用有效的分离了读出和写入操作。在任何给定时间内，如果队列已满，要发布消息的读线程就不得不等待队列中的可用位置。同样，如果写操作是空的，那么所有的写线程必须等待直到队列有可用的消息位置。因此，即使读写操作的速度差别很大，数据完整性也绝不受损。
- 在线交易

#### 阻塞队列的特征
阻塞队列的典型特征可以概括如下：
- 阻塞队列提供了方法来向其中添加条目。这些方法的调用都是阻塞调用的，其中条目的插入必须等待，直到队列的空间变为可用。
- 队列提供了方法来从中删除条目，对这些方法的调用同样是阻塞调用的。调用者会等待条目被放入空队列
- add和remove方法可以选择性地为它们的等待操作提供**超时并可能被中断**
- put和take操作在单独的线程中实现，从而在两种类型的操作之间提供了良好的绝缘性。通常是在不阻塞整个队列的情况下完成的，从而显著提高了这些操作的并发性。
- 不能像阻塞队列中插入null元素
- 阻塞队列可能受容量的限制
- 阻塞队列的实现是线程安全的，然而批量操作，比如addAll，没有必要一定原子地执行，
- 阻塞队列在本质上不支持“关闭”或“停止”操作，这表示没有更多的条目可添加


#### BlockingQueue接口
J2SE5.0引入了阻塞队列这个概念作为并发框架的一部分，BlockingQueue接口有利于队列的构造，并提供了几个方法来对队列进行操作。队列一旦创建好，就可以使用add和remove方法添加或从队列中移除元素，正如名字所示，在失败的情况下，这些方法会抛出一些一样。队列通常具有有限的大小，如果这样的队列已满，ad操作会失败，同样队列为空，remove操作也会失败。

#### BlockingQueue接口的实现
J2SE 5.0提供了BlockingQueue接口的几个实现：

##### ArrayBlockingQueue
ArrayBlockingQueue类实现类又数组支持的有界阻塞队列，实现类FIFO(先进先出)排序机制。新元素被插入到队列的尾部，获取操作则在队列的头部进行。队列的尾部是在队列中存在时间最短的元素，而队列的头部是在队列中存在时间最长的元素。队列的大小是固定的，在队列构造的时候确定，之后不能在增加数组的容量。

##### LinkedBlockingQueue
LinkedBlockingQueue是通过将阻塞队列的最大容量变为可变，进而扩展了数据阻塞队列的概念。你仍然可以在指定容量已禁止过度扩容。如果不指定容量，默认值是最大的整数值。没有容量限制是由好处的，因为如果先飞着晚于预订时间选取条目，生产者无需等待。与基于数组的队列相同，重载的构造函数可以接受集合指定的初始值。这种队列比基于数组阻塞队列具有更高的吞吐量。但同时也有较少的可预见性。除了移除操作在线性时间运行之外，队列的大多数操作都是在常数时间内运行的。

##### PriorityBlockingQueue

PriorityBlockingQueue是无界队列，可以决定元素的优先顺序，优先级可以由元素的自然顺序或你提供的比较器来确定。依照优先级队列的顺序，视图插入不可比较的对象会导致ClassCastException异常。如果系统资源耗尽，虽然是无界队列，添加操作也会失败，

##### DelayedQueue
DelayedQueue是一种特殊的优先级队列，按照每个元素的延迟时间进行排序

##### TransferQueue
JavaSE7 引入了一个新的接口，名为TransferQueue，扩展自BlockingQueue。与同步队列类似，这里的消费者可能会等待消费者接收源于，新的接口添加了transfer方法。BlockingQueue现有的put方法直接将元素入队，而无需等待接收，TransferQueue也可能是容量有限的。

### 股票交易系统

```java
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

```
### 卖家和买家

```java
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
```
### 输出
```java
Buy order by Thread-134: 48
Buy order by Thread-134: 83
Buy order by Thread-134: 2
Buy order by Thread-134: 52
Sell order byThread-86: 90
Sell order byThread-86: 19
Sell order byThread-86: 64
Sell order byThread-86: 83
Sell order byThread-86: 27
Buy order by Thread-163: 94
Buy order by Thread-163: 74
```
当在键盘上按下Enter键使，程序终止

在这个程序中，如果没有使用阻塞队列，访问非同步队列中放置的交易时会发生竞争，每个人都会尝试抢得低于当前市场价格出售的股票，多个交易者会选取统一订单，交易之间会很混乱，由于阻塞队列 确保了同步地访问队列，因此交易的完整性绝不会 受到损害。

在这个例子中使用了`LinkedBlockingQueue`，气死也可以基于优先级的队列，这样会自动按照交易的买价和卖价对交易进行排列，具有最好买价和最低卖价的订单总是排在队列的头部。要使用基于优先级的队列，**需要提供适当的比较器。**
