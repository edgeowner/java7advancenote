## 工具类

本章主要内容：

String类
- 操作字符串的各种技术
- 字符串对象的比较
- 创建格式化输出

日期类和时间类
- Calendar类
- GregorianCalendar
- 开发本地时间转换应用程序

Java中的内省和反射机制
- 理解Class类
- 使用Method类进行动态调用
- 创建类浏览应用程序以及动态的发现和执行未知类



### 21.1 String类

主要包括连接字符串、从给定字符串中提取子串以及使用其他的字符串序列来替换原有的字符串序列

#### 21.1.1 几个重要的方法

String类提供了名为subString方法，用于提取给定字符串的一部分。调用这个方法时，可以指定开始索引和结束索引。
```java
public String subString(int beginIndex)
public String subString(int bedinIndex,int endIndex)
```
beginIndex参数指定在字符串中开始提取位置的索引，endIndex参数指定提取操作到达的结束位置的索引。

可以使用contains方法来检查给定字符串中是否存在与某个子串。如果在给定字符串中找到了指定的字符串序列，则contains方法返回true

```java
public boolean contains(charSequence s)
```

通过使用replace方法，可以用另外一个字符串替换字符串中的任意字符。replace方法使用指定的新字符串替换所有出现的给定字符
```java
public String replace(char oldChar, char newChar)
```
repalce方法中有多个变体可供使用

```java
//replace方法会使用心得字符串替换所有出现的给定字符串序列
public String replace(CharSequence target,charSequence replacement)

//repalce方法使用replacement字符串替换所有与给定正则表达式相匹配的子串
public String replaceAll(String regex,String replacement)

//replace方法使用replacement字符串替换第一个与正则表达式匹配的子串，正则表达式由第一个参数指定
public replaceFirst(String regex,String replacement)

```

split方法基于指定的分隔符分离出给定字符串的标识符，split方法有两个版本

```java

public String split(String regex)
//限制了要分离的标识符的数目，两个方法都将标识符返回到字符串数组中
public String split(String regex, int limit)
```

String类的format方法允许使用printf风格的形式来格式化给定的字符串

#### 21.1.2 String方法的实战演示
```java
package com.guo.chap21;

/**
 * Created by guo on 2018/2/14.
 * 如何使用String类的各种方法将字符串解析成单个标识符
 */
public class StocksEODPaser {
    //IBM在证券交易所上市结束日的报价
    private static String trade = "IBM,/09/09/218,87,100,80,95,1567823";

    public static void main(String[] args) {

        //retrieving a substring
        //使用substring方法从IBM交易字符串中提取日期字段。
        String dateField = trade.substring(4, 14);
        System.out.println("Substring field date equals" + dateField);

        //locating a character sequence
        //contains方法检查输入的字符串是否包含参数中指定的字符串
        if (trade.contains("/09/09/218")) {
            System.out.println("This is a trade on 09/09/2018");
        }

        //replace a character sequence
        //调用replace方法将字段分隔符逗号换成冒号。然后赋值给新的变量
        String str = trade.replace(",", ":");
        System.out.println("after replaceing delimitz:" + str);

        //replacing a character sequence
        //使用新的字符串替换旧的字符串
        str = trade.replace("100", "101");
        System.out.println("After replacing trade price 100:" + str);

        System.out.println("Spliting string into its fields");
        //调用split方法，将输入字符串中的标识符分离出来，然后在for-each中循环中将值打印到控制台
        String[] fields = trade.split(",");
        for (String strField : fields) {
            System.out.println("\t" + strField);
        }

        //计算最高价和最低价之间的差值，需要将字段的值转为对应的float类型。通过使用Float类的parse方法来实现的。
        float hilowDifference = Float.parseFloat(fields[3]) - Float.parseFloat(fields[4]);
        //为了将差值转换成String类型，需要使用String类的valueOf方法
        str = String.valueOf(hilowDifference);
        System.out.println("Difference in Hi to Low price:$" + str);

        //为了打印浮点数的值，可以简单的将浮点数追加到另外一个字符串中("" + hilowDifference)
        //使用静态的format方法，通过在末尾添加0来格式化给定的浮点数
        System.out.println(String.format("Formatted HiLow Differnce： $%.02f", hilowDifference));
    }
}

```

#### 21.1.3 字符串的比较

当比较两个字符串时，了解发生了什么会比较有趣。一般情况下，你会使用比较操作符(==)来比较两个对象。当使用比较操作符比较两个字符串时，我们来看看在这种情况下会发生什么。
```java
package com.guo.chap21;

/**
 * Created by guo on 2018/2/14.
 * 比较两个字符串对象
 */
public class StringComparator {
    public static void main(String[] args) {
        String str1 = "This is a test string";
        String str2 = new String(str1);
        String str3 = "This is a test string";
        System.out.println("str1.equals(str2) returns " + str1.equals(str2));
        System.out.println("str1 == str2 returns " + str1 == str2);
        System.out.println("str1.equals(str3) returns " + str1.equals(str3));
        System.out.println("str1 == str3 returns " + str1 == str3 );
    }
}

程序输出结果如下：
E:\jdk\jdk1.8.0\bin\java
str1.equals(str2) returns true
str1 == str2 returns false
str1.equals(str3) returns true
str1 == str3 returns true
```

现在让我们来分析下输出结果。第一条比较语句使用equals方法来比较str1和str2.对于String类，equals方法比较两个操作对象的内容。因为str1和str2包含相同的字符序列，所以会返回true。

下一条语句使用的是等号运算符，因为str1和str2是两个不同的对象，所以既暖对象的内容是相同的，但比较仍然会返回false。str2使用通过调用String类的构造函数完成的。

第三条语句再一次使用了equals方法，返回true。因为str1和str2的内同时相同的。

第四条语句str1和str3两个变量是不相同的，并且是单独初始化的。虽然这样，但由于这两个对象包含相同的字符序列，因此编译器只为str1和str3创建了一个对象。

#### 21.1.4 创建格式化输出

J2SE 5.0在Java中引入了printf风格的输出，这是通过引入java.util包中的Format类实现的。这个类的format方法允许你指定输出字符串的格式。

```java
package com.guo.chap21;
import java.util.Formatter;
/**
 * Created by guo on 2018/2/14.
 */
public class StringFormat01 {
    public static void main(String[] args) {
        //表示可变的字符序列，与早期的StringBuffer类变比，处于性能考虑，建议使用StringBuilder类来创建可变字符串
        StringBuilder stringBuilder = new StringBuilder();
        Formatter formatter = new Formatter(stringBuilder);
        formatter.format("MAX float value: %10e\n", Float.MAX_VALUE );
        System.out.println(stringBuilder);

    }
}

在控制台输出如下内容：
MAX float value: 3.402823e+38
```
除了使用Formatter类，还可以使用java.io.PrintStream、java.io.PrintWriter和java.lang.String类中新添加的prinf方法，这些类中重载的printf方法也允许你在格式化输出时指定语言环境。

### 21.2Calendar类

Java提供了丰富的日期和时间操作的功能。在这个类别中，最重要的类是 **GregorianCalenda**
该类在java.util包中定义：

```java
public class GregorianCalendar extends Calendar

```

#### 21.2.1 GregorianCalendar类的方法

这个类的各种构造函数如下：
```java
public GregorianCalendar();
public GregorianCalendar(TimeZone zone);
public GregorianCalendar(Locale alocale);
public GregorianCalendar(TimeZone zone,Locale alocale);
public GregorianCalendar(int year,int month,int dayOfMonth);
public GregorianCalendar(int year,int month,int dayOfMonth,
                         int hourOfDay,int minute,int second);
```

不带参数的类构造函数使用默认语言环境中默认的失去的当前时间构造Calendar。其他变化形式在创建时允许你指定时区和区域设置。最后两种允许你指定不同的日期和时间参数，这样便可以创建任何日期和日历实例，而不是当前时间的实力。

这个类的add方法需要两个参数，第一个参数指明将第二个参数指定的时间数加到Calendar类的哪个字段

```java
public void add(int field,int amount)
```
Calendar类的好几个方法都允许进行日期比较。例如：after和before方法允许直接与另一个时间对象比较，已确定当前对象的指定的时间是否发生在其他时间之前或之后
```java
public boolean after(Object when)
public boolean before(Object when)
```
compareTo方法提供了两个时间对象之间的比较
```java
public int compareTo(Calendar anotherCalendar)
```
get和set方法用于获取和设置当前对象的时区。学习类用法最好的方法就是通过实际例子，

### 21.3 内省和反射
Java语言的动态性，是java初始性列表中非常时髦的词语，Java的这中动态特性是在内省和反射的机制上实现的。反射允许程序在运行时检查任何类或对象的内部，内省是建立在反射基础上的。使用内省，运行的代码可以得到对象所属的类，得到类的方法和构造函数，找出每个方法所需的参数和返回类型，加载未知的类，创建所发现的类的对象和数组，并调用新创建对象的方法。这就是Java的动态性。所有的这些事情都是在运行时完成的，编译器不知道在运行时加载的类。
```java
/**
 * Created by guo on 2018/2/14.
 * 动态方法调用器
 */
public class DynamicInvoker {
    public static void main(String[] args) {
        DynamicInvoker app = new DynamicInvoker();
        app.printGreeting("guo", 5);
        System.out.println("\nDynamicInvoker of printGreeting methos");
        try {
            app.getClass().getMethod("printGreeting", new Class[]{
                    Class.forName("java.lang.String"), Integer.TYPE}).
                    invoke(app, new Object[]{"xiaoxu", new Integer(3)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void printGreeting(String name, int numberOfTimes) {
        for (int i = 0; i < numberOfTimes; i++) {
            System.out.println("Hello" + name);
        }
    }
}
```

DynamicInvoker是一个非常简单的应用程序，在main方法中创建实例并调用printGreeting方法，该方法由第一个参数指定的人打印了一句问候，第二和参数指定了打印次数。

除了名称的变化和打印次数的变化外，产生的输出与之前在app对象上调用printGreeting方法的输出类似。
因此，这种动态调用的出处之一就是在代码中混淆和模糊方法调用。当然，缺点就是在运行时检查方法，而不是在编译期。有些人可能仍然认为这是优点。还有没有更好的使用内省和反射的原因呢？

内省具有十分广泛的应用可以用来编写插件--应用程序在运行时发现插件的方法并调用它，JUnit使用内省来识别以“Test”单词开头测试方法并按顺序调用它们。

在Web服务中，客户端可以从UDDI注册中发现服务的接口，并通过动态编译来获取服务，你甚至可以通过在运行时内省类来开发自己的Javadocs类型应用程序。

此特性自从Java语言诞生起以来就已经可用，并且在JDK1.1版本中，反射API已经成为Java核心的一部分。在JDK1.1中，java.lang.Class类添加了新的方法并且引入了新的java.lang.reflct.

**CLass,这个是内省和反射机制的基础。**


#### 21.3.1 CLass类
Java定义了类Class。因为是final的，所以Class类不能在程序中继承

```java
public final class CLass<T> extends Object
          implements Serializables,GenericDeclaration,Type,AnnotateElement
```
每当程序运行时加载类到内存中时，就会创建类的实例来代表加载类的或接口。
Java中所有的类都派生自Object类，通过调用Object类的getClass方法，可以获取指向这个类的对象引用，还可以使用类的forName方法来加载未知的类，并获得指向Class对象的引用。Class类还提供了几个方法来内省代表的类，例如：可以取得类名，修饰符，构造函数，方法，属性，异常。不仅如此还能创建所表示类的另一个对象和对象数据。因此，这就提供了Java语言的动态性。

getName方法返回Class对象表示的累得完全限定名，getModifiers方法返回Class对象代表类或接口的Java语言修饰符。
```java
public int getModifiers()
```
这些修饰符使用整数进行编码，并且必须使用Modifiers类的方法进行解码。

getMothod方法返回Method对象的数组，从而反映所代表对象的所有公共方法，其中包括继承自父类和父接口中的所有方法。
```java
public Metho[] getMethod() throws SecurityException
```
同样，getConstuctor方法返回所有公有构造函数的数组；

forName方法接受类名作为参数，并返回表示指定类的calss对象
这个方法对于将编译时未知的Java类注入运行时环境中，进而实现动态实例化是非常有用的。如果不能定位这个类，则可能会抛出ClassNotFoundException异常。

newInstance方法用于创建Class对象表示类的对象。如果这个类或其无参构造函数不可访问，你会得到IllegalAccessException异常。

使用forName方法允许你在开发时使用类型未知的对象，你会发现这个功能的一些十分有用的应用程序，最知名的应用是在代码中动态的加载JDBC驱动，

#### 21.3.2 Method类

对于在运行时动态调用方法，此类是非常有用的。
```java
public final class Method extends AccessibleObject
              implements Member,GenericDeclaration
```
像Class类一样，Method类也是final修饰的，因此不能扩展。Method类提供了一些getter方法来获取所表示方法的所有细节。

getParameterTypes方法返回Class对象的数组，Class对象表示这个方法所需的形参类型。

通过Method对象调用getReturnType方法，能够获得方法的返回类型。

invoke方法调用由Mehtod对象表示的底层方法。

### 21.4 小节
在本章你学习力Java类库提供的几个工具类。String类提供了几个用于操作字符串的方法，substring方法允许从给定的字符串中提取字符序列。contains方法运行在给定的字符串中检查是否存在子串。可是使用replace方法在指定的字符串对象中使用另一个字符串序列来替换指定的字符或字符序列。format方法允许创建格式化的字符串输出。要比较两个字符串，可以使用equals方法。浙江导致对字符串的内容进行比较，不要使用等于运算符来比较两个字符串是否相等。

Java通过Calendar类提供的日期和时间的表示功能GregorianCalendar的具体实现，提供了几个方法来表示日期和时间。可以在实际的应用中使用这个类，找换粗与你所在地的当前时间实例对应的时间各地时间

内省和反射机制是Java语言的强大功能之一，提供了Java语言的动态特性。java.lang包中哦国内提供了名为Class的类，从而表示已加载到内存的类，可以使用这个类的实例来获取加载类的细节，这样的细节诶是非常详细的。包括类的修饰符，公共方法，，公共的构造函数，Class类的forName方法允许在运行时系统中注入任何编译时未知的类，一旦类在运行时可以，则调用newInstance方法，就可以创建这种类型的对象，getDeclaredMethods方法一Methos对象数组返回公有的列表，Mehtod类的invoke方法允许动态调用方法。































































































-
