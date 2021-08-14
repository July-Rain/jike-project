# **Week02 作业**
## **1.（选做）**使用 GCLogAnalysis.java 自己演练一遍串行 / 并行 /CMS/G1 的案例。

### 编译

1. 编译GCLogAnalusis.java文件:javac GCLogAnalysis.java
2. 编译不成功：GCLogAnalysis.java:6: 错误: 编码GBK的不可映射字符
3. 编码问题：javac编译的时候执行UTF-8编译即可：javac -encoding UTF-8 GCLogAnalysis.java

### 执行GC分析

#### 默认GC打印GC日志分析 （本机JDK1.8 默认GC是并行GC）

java -XX:+PrintGCDetails GCLogAnalysis  

默认参数使用物理内存的1/4 。共产生11次YoungGC  3次FullGC 共生产对象14863

GC情况分析

```shell
[GC (Allocation Failure) [PSYoungGen: 65536K->10750K(76288K)] 65536K->22997K(251392K), 0.0047065 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] #分配内存失败，产生一次YoungGC 4毫秒内（GC暂停时间）垃圾回收Young区的内存从65M压缩到10M。压缩了55M。整个Young区最大的容量是762M。整个堆内存的情况从65M压缩到22M,压缩了43M。此次GC过程中有12MYoung区的数据从Young区晋升到Old区。
......
[Full GC (Ergonomics) [PSYoungGen: 10735K->0K(272896K)] [ParOldGen: 200381K->178109K(352256K)] 211117K->178109K(625152K), [Metaspace: 2621K->2621K(1056768K)], 0.0312855 secs] [Times: user=0.19 sys=0.00, real=0.03 secs]
#产生FullGC的原因是：Ergonomics -- 在JVM中的垃圾收集器中的Ergonomics就是负责自动的调解gc暂停时间和吞吐量之间的平衡，然后你的虚拟机性能更好的一种做法。

#发现当我们使用Server模式下的ParallelGC收集器组合（Parallel Scavenge+Serial Old的组合）下，担保机制的实现和之前的Client模式下（SerialGC收集器组合）有所变化。在GC前还会进行一次判断，如果要分配的内存>=Eden区大小的一半，那么会直接把要分配的内存放入老年代中。否则才会进入担保机制。

#bool result = padded_average_promoted_in_bytes() > (float) old_free_in_bytes; 晋升到老生代的平均大小大于老生代的剩余大小，则会返回true，认为需要一次full gc

#Ergonomics产生一次FullGC 31毫秒内（GC暂停时间）垃圾回收Young区的内存从10M压缩到0M(进行FullGC的时候直接把Young区的数据清掉了)。Old区从200M压缩到178M,Old区的容量是352M。整个堆内存的情况从211M压缩到178M,压缩了33M。
......
执行结束!共生成对象次数:14863
```



```shell
#程序退出之前打印堆内存情况
Heap
 PSYoungGen      total 1006592K, used 32866K  #默认的年轻代1006M 使用32M
  eden space 804864K, 4% used #eden 区共804M 使用4%
  from space 201728K, 0% used #from区共201M 使用0%
  to   space 307712K, 0% used #To区共307M 使用0%
 ParOldGen       total 627200K, used 377282K  #老年代共627M 使用377M
  object space 627200K, 60% used  #对象空间627M 使用60%
 Metaspace       used 2627K, capacity 4486K, committed 4864K, reserved 1056768K  #Meta区 使用2M 4M 4M 1056M
  class space    used 290K, capacity 386K, committed 512K, reserved 1048576K #类空间 使用0.2M 计算0.3M 0.5M 重置1048M
```

#### 默认GC,设置堆内存大小为1G,打印GC日志分析 （本机JDK1.8 默认GC是并行GC）

java -XX:+PrintGCDetails -Xmx1g -Xms1g GCLogAnalysis

堆内存大小设置为1G。共产生23次YoungGC  2次FullGC 共生产对象13671

GC情况分析

```shell
[GC (Allocation Failure) [PSYoungGen: 262144K->43512K(305664K)] 262144K->72026K(1005056K), 0.0127275 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] #分配内存失败，产生一次YoungGC 12毫秒内（GC暂停时间）垃圾回收Young区的内存从262M压缩到43M。压缩了219M。整个Young区最大的容量是305M。整个堆内存的情况从262M压缩到72M,压缩了190M。此次GC过程中有29MYoung区的数据从Young区晋升到Old区。
......
[Full GC (Ergonomics) [PSYoungGen: 37600K->0K(232960K)] [ParOldGen: 620654K->328198K(699392K)] 658254K->328198K(932352K), [Metaspace: 2621K->2621K(1056768K)], 0.0597876 secs] [Times: user=0.22 sys=0.00, real=0.06 secs]#Ergonomics产生一次FullGC 59毫秒内（GC暂停时间）垃圾回收Young区的内存从37M压缩到0M(进行FullGC的时候直接把Young区的数据清掉了)。Old区从620M压缩到328M,Old区的容量是699M。整个堆内存的情况从658M压缩到328M,压缩了330M。

......
执行结束!共生成对象次数:13671
```

  

#### 后续针对串行GC、G1GC 、CMSGC 的日志分析

通过把GC日志打印到文件中，通过在线的方式分析GC情况 https://gceasy.io/

```shell
java -XX:+PrintGCDetails -Xloggc:gc.default1Ggc.log -Xmx1g -Xms1g GCLogAnalysis
java -XX:+PrintGCDetails -Xloggc:gc.default512Mgc.log -Xmx512M -Xms512M GCLogAnalysis
java -XX:+PrintGCDetails -Xloggc:gc.default256Mgc.log -Xmx256M -Xms256M GCLogAnalysis
java -XX:+PrintGCDetails -Xloggc:gc.default4Ggc.log -Xmx4G -Xms4G GCLogAnalysis

java -XX:+PrintGCDetails -Xloggc:gc.Serial-1Ggc.log -XX:+UseSerialGC -Xmx1G -Xms1G GCLogAnalysis
java -XX:+PrintGCDetails -Xloggc:gc.Serial-512Mgc.log -XX:+UseSerialGC -Xmx512M -Xms512M GCLogAnalysis
java -XX:+PrintGCDetails -Xloggc:gc.Serial-256Mgc.log -XX:+UseSerialGC -Xmx256M -Xms256M GCLogAnalysis
java -XX:+PrintGCDetails -Xloggc:gc.Serial-4Ggc.log -XX:+UseSerialGC -Xmx4G -Xms4G GCLogAnalysis

java -XX:+PrintGCDetails -Xloggc:gc.G1-1Ggc.log -XX:+UseG1GC -Xmx1G -Xms1G GCLogAnalysis
java -XX:+PrintGCDetails -Xloggc:gc.G1-512Mgc.log -XX:+UseG1GC -Xmx512M -Xms512M GCLogAnalysis
java -XX:+PrintGCDetails -Xloggc:gc.G1-256Mgc.log -XX:+UseG1GC -Xmx256M -Xms256M GCLogAnalysis
java -XX:+PrintGCDetails -Xloggc:gc.G14Ggc.log -XX:+UseG1GC -Xmx4G -Xms4G GCLogAnalysis

java -XX:+PrintGCDetails -Xloggc:gc.CMS-1Ggc.log -XX:+UseConcMarkSweepGC -Xmx1G -Xms1G GCLogAnalysis
java -XX:+PrintGCDetails -Xloggc:gc.CMS-512Mgc.log -XX:+UseConcMarkSweepGC -Xmx512M -Xms512M GCLogAnalysis
java -XX:+PrintGCDetails -Xloggc:gc.CMS-256Mgc.log -XX:+UseConcMarkSweepGC -Xmx256M -Xms256M GCLogAnalysis
java -XX:+PrintGCDetails -Xloggc:gc.CMS-4Ggc.log -XX:+UseConcMarkSweepGC -Xmx4G -Xms4G GCLogAnalysis
```



| GC算法            | 堆栈大小 | YoungGC次数 | FullGC次数 | GC暂停时间 | 是否OOM | GC日志报告                                                   |
| ----------------- | -------- | ----------- | ---------- | ---------- | ------- | ------------------------------------------------------------ |
| UseSerialGC       | 4G       | 3           | 0          | 250ms      |         | https://gceasy.io/my-gc-report.jsp?p=c2hhcmVkLzIwMjEvMDgvMTQvLS1nYy5TZXJpYWwtNEdnYy5sb2ctLTEzLTU5LTE5&channel=WEB |
| UseSerialGC       | 1G       | 13          | 2          | 470 ms     |         | https://gceasy.io/my-gc-report.jsp?p=c2hhcmVkLzIwMjEvMDgvMTQvLS1nYy5TZXJpYWwtMUdnYy5sb2ctLTQtNTUtMjE=&channel=WEB |
| UseSerialGC       | 512M     | 10          | 10         | 640 ms     |         | https://gceasy.io/my-gc-report.jsp?p=c2hhcmVkLzIwMjEvMDgvMTQvLS1nYy5TZXJpYWwtNTEyTWdjLmxvZy0tNS0xLTI4&channel=WEB |
| UseSerialGC       | 256M     | 52          | 7          | 740 ms     | √       | https://gceasy.io/my-gc-report.jsp?p=c2hhcmVkLzIwMjEvMDgvMTQvLS1nYy5TZXJpYWwtMjU2TWdjLmxvZy0tNS0xLTI4&channel=WEB |
| UseParallelGC     | 4G       | 4           | 0          | 250 ms     |         | https://gceasy.io/my-gc-report.jsp?p=c2hhcmVkLzIwMjEvMDgvMTQvLS1nYy5kZWZhdWx0NEdnYy5sb2ctLTEzLTU5LTE5&channel=WEB |
| UseParallelGC     | 1G       | 24          | 2          | 470 ms     |         | https://gceasy.io/my-gc-report.jsp?p=c2hhcmVkLzIwMjEvMDgvMTQvLS1nYy5kZWZhdWx0MUdnYy5sb2ctLTQtNDctNTM=&channel=WEB |
| UseParallelGC     | 512M     | 25          | 10         | 690 ms     |         | https://gceasy.io/my-gc-report.jsp?p=c2hhcmVkLzIwMjEvMDgvMTQvLS1nYy5kZWZhdWx0NTEyTWdjLmxvZy0tNC01My00OA==&channel=WEB |
| UseParallelGC     | 256M     | 12          | 31         | 710 ms     | √       | https://gceasy.io/my-gc-report.jsp?p=c2hhcmVkLzIwMjEvMDgvMTQvLS1nYy5kZWZhdWx0MjU2TWdjLmxvZy0tNC00Ny01Mw==&channel=WEB |
| UseConMarkSweepGC | 4G       | 10          | 0          | 390 ms     |         | https://gceasy.io/my-gc-report.jsp?p=c2hhcmVkLzIwMjEvMDgvMTQvLS1nYy5DTVMtNEdnYy5sb2ctLTEzLTU5LTE5&channel=WEB |
| UseConMarkSweepGC | 1G       | 14          | 2          | 410 ms     |         | https://gceasy.io/my-gc-report.jsp?p=c2hhcmVkLzIwMjEvMDgvMTQvLS1nYy5DTVMtMUdnYy5sb2ctLTQtNC0yMA==&channel=WEB |
| UseConMarkSweepGC | 512M     | 20          | 9          | 580 ms     |         | https://gceasy.io/my-gc-report.jsp?p=c2hhcmVkLzIwMjEvMDgvMTQvLS1nYy5DTVMtNTEyTWdjLmxvZy0tNC00Ny01Mw==&channel=WEB |
| UseConMarkSweepGC | 256M     | 21          | 11         | 790 ms     |         | https://gceasy.io/my-gc-report.jsp?p=c2hhcmVkLzIwMjEvMDgvMTQvLS1nYy5DTVMtMjU2TWdjLmxvZy0tNC00LTIw&channel=WEB |
| UseG1GC           | 4G       | 14          | 0          | 230 ms     |         | https://gceasy.io/my-gc-report.jsp?p=c2hhcmVkLzIwMjEvMDgvMTQvLS1nYy5HMTRHZ2MubG9nLS0xMy01OS0xOQ==&channel=WEB |
| UseG1GC           | 1G       | 14          | 2          | 240 ms     |         | https://gceasy.io/my-gc-report.jsp?p=c2hhcmVkLzIwMjEvMDgvMTQvLS1nYy5HMS0xR2djLmxvZy0tNS0xLTI4&channel=WEB |
| UseG1GC           | 512M     | 40          | 3          | 350 ms     |         | https://gceasy.io/my-gc-report.jsp?p=c2hhcmVkLzIwMjEvMDgvMTQvLS1nYy5HMS01MTJNZ2MubG9nLS01LTEtMjg=&channel=WEB |
| UseG1GC           | 256M     | 32          | 11         | 150 ms     | √       | https://gceasy.io/my-gc-report.jsp?p=c2hhcmVkLzIwMjEvMDgvMTQvLS1nYy5HMS0yNTZNZ2MubG9nLS00LTU2LTMy&channel=WEB |



## **2.（选做）**使用压测工具（wrk 或 sb），演练 gateway-server-0.0.1-SNAPSHOT.jar 示例。

> sb -u http://localhost:8088/api/hello -c 40 -N 30   #压测：40个线程压测30s ,其中SerialGC 特殊 使用的是单线程，40个线程跑不出来结果

### ParallelGC

java -jar -Xmx1G -Xms1G gateway-server-0.0.1-SNAPSHOT.jar 

```shell
RPS: 5294.5 (requests/second) #每秒请求量
Max: 223ms
Min: 0ms
Avg: 0.8ms

  50%   below 0ms
  60%   below 0ms
  70%   below 0ms
  80%   below 0ms
  90%   below 1ms
  95%   below 5ms
  98%   below 12ms
  99%   below 18ms
99.9%   below 35ms
```

### SerialGC

java -jar -XX:+UseSerialGC -Xmx1G -Xms1G  gateway-server-0.0.1-SNAPSHOT.jar

```
sb -u http://localhost:8088/api/hello -c 1 -N 30
Starting at 2021/8/14 16:02:59
[Press C to stop the test]
133555  (RPS: 3970.7)
---------------Finished!----------------
Finished at 2021/8/14 16:03:33 (took 00:00:33.8050637)
Status 200:    133555

RPS: 4283.8 (requests/second)
Max: 21ms
Min: 0ms
Avg: 0ms

  50%   below 0ms
  60%   below 0ms
  70%   below 0ms
  80%   below 0ms
  90%   below 0ms
  95%   below 0ms
  98%   below 0ms
  99%   below 0ms
99.9%   below 0ms
```

### G1GC

java -jar -XX:+UseG1GC -Xmx1G -Xms1G  gateway-server-0.0.1-SNAPSHOT.jar

```

RPS: 5661.6 (requests/second)
Max: 248ms
Min: 0ms
Avg: 0.6ms

  50%   below 0ms
  60%   below 0ms
  70%   below 0ms
  80%   below 0ms
  90%   below 0ms
  95%   below 3ms
  98%   below 8ms
  99%   below 14ms
99.9%   below 41ms
```

### ConcMarkSweepGC

java -jar -XX:+UseConcMarkSweepGC -Xmx1G -Xms1G  gateway-server-0.0.1-SNAPSHOT.jar

```
RPS: 5754.9 (requests/second)
Max: 115ms
Min: 0ms
Avg: 0.6ms

  50%   below 0ms
  60%   below 0ms
  70%   below 0ms
  80%   below 0ms
  90%   below 0ms
  95%   below 4ms
  98%   below 8ms
  99%   below 14ms
99.9%   below 35ms
```

### 同样的Xmx1G的参数情况下压测结果对比：（其中串行GC单个线程压测）

| GC策略                 | RPS    | Max   | Avg   |
| ---------------------- | ------ | ----- | ----- |
| SerialGC(单个线程压测) | 4283.8 | 21ms  | 0ms   |
| ParallelGC             | 5294.5 | 223ms | 0.8ms |
| G1GC                   | 5661.6 | 248ms | 0.6ms |
| CMSGC                  | 5754.9 | 115ms | 0.6ms |



## **3.（选做）**如果自己本地有可以运行的项目，可以按照 2 的方式进行演练。

本地项目：aj-modules-mgr-1.0-SNAPSHOT.jar

### -Xmx1G

sb -u http://localhost:8086/aj/main/web/login  -m POST -t login.txt -c 20 -N 30

同样的Xmx1G的参数情况下压测结果对比

| GC策略     | RPS   | Max    | Avg     |
| ---------- | ----- | ------ | ------- |
| SerialGC   | 138.1 | 1119ms | 139.1ms |
| ParallelGC | 136   | 478ms  | 121.7ms |
| G1GC       | 139.7 | 1096ms | 136.2ms |
| CMSGC      | 135   | 950ms  | 141.2ms |

### -Xmx512M

sb -u http://localhost:8086/aj/main/web/login  -m POST -t login.txt -c 20 -N 30

同样的Xmx512M的参数情况下压测结果对比

| GC策略     | RPS   | Max    | Avg     |
| ---------- | ----- | ------ | ------- |
| SerialGC   | 141.1 | 1264ms | 135.6ms |
| ParallelGC | 172.7 | 304ms  | 110.8ms |
| G1GC       | 118.6 | 1312ms | 160.8ms |
| CMSG       | 127.8 | 1180ms | 149.6ms |

### -Xmx4G

sb -u http://localhost:8086/aj/main/web/login  -m POST -t login.txt -c 20 -N 30

同样的-Xmx4G的参数情况下压测结果对比

| GC策略     | RPS   | Max    | Avg     |
| ---------- | ----- | ------ | ------- |
| SerialGC   | 150.4 | 1217ms | 127.4ms |
| ParallelGC | 150.8 | 957ms  | 126.6ms |
| G1GC       | 143.7 | 1358ms | 132.2ms |
| CMSG       | 155.9 | 1088ms | 122ms   |



## **4.（必做）**根据上述自己对于 1 和 2 的演示，写一段对于不同 GC 和堆内存的总结，提交到 GitHub。

## YoungGC和FullGG的次数

针对-Xmx配置4G、1G、512M、256M 针对SerialGC、ParallelGC、G1GC、CMSGC四种GC策略进行youngGC和FullGG的次数分析。具体结果如图所示 ![](\各个Xmx情况下GC情况.png)

根据上图综合分析：

-Xmx256M场景下：串行GC策略虽然youngGC的次数最高，但是只发生了7次FullGC ，ParallelGC发生了31次FullGC

-Xmx512M场景下：G1GC策略YoungGC产生了40次，FullGC产生了3次，SerialGC和ParallelGC都发生了10次GC

-Xmx1G场景下：所有的GC策略都只产生了2次GC,其中ParallelGC发生了24Young区GC

-Xmx4G场景下:所有的GC都未产生FullGC,其中G1GC产生了14次FullGC

## GC暂停时间

针对-Xmx配置4G、1G、512M、256M 针对SerialGC、ParallelGC、G1GC、CMSGC四种GC策略GC暂停时间进行分析。具体结果如图所示 ![](\各个Xmx情况下GC暂停时间.png)

根据上图综合分析：G1在不同的-Xmx配置下整体的GC暂停时间是最短的（低延迟），这个跟他底层GC算法中的小块region进行垃圾回收有关。

## 实际项目不同-Xmx不同GC策略下的RPS和平均相应情况

针对-Xmx配置4G、1G、512M 针对SerialGC、ParallelGC、G1GC、CMSGC四种GC策略项目接口(http://localhost:8086/aj/main/web/login)压测情况得出的RPS和平均相应时间。具体结果如图所示 ![](\不同Xmx不同GC策略下的RPS和平均相应时间png.png)

根据上图综合分析：在不同的-Xmx配置下整体的RPS没有断崖式区别，其中ParallelGC整体吞吐量较高，比较意外的是G1GC和CMSGC并没有向预想的一样平均相应时间较低，所以整体具体如何选择GC策略还是需要基于实际项目进行测试，寻找最优。

## **5.（选做）**运行课上的例子，以及 Netty 的例子，分析相关现象。

Netty 压测

>sb -u http://127.0.0.1:18088/test -c 40 -N 30

```
RPS: 7323.1 (requests/second)
Max: 142ms
Min: 0ms
Avg: 0.5ms

  50%   below 0ms
  60%   below 0ms
  70%   below 0ms
  80%   below 0ms
  90%   below 0ms
  95%   below 3ms
  98%   below 8ms
  99%   below 12ms
99.9%   below 29ms
```

HttpService02

> sb -u http://localhost:8802/test -c 40 -N 30

```
RPS: 2994.8 (requests/second)
Max: 126ms
Min: 0ms
Avg: 3.3ms

  50%   below 0ms
  60%   below 1ms
  70%   below 3ms
  80%   below 6ms
  90%   below 11ms
  95%   below 16ms
  98%   below 24ms
  99%   below 30ms
99.9%   below 52ms
```

HttpService03

> sb -u http://localhost:8803/test -c 40 -N 30

```

RPS: 3776.9 (requests/second)
Max: 247ms
Min: 0ms
Avg: 2.2ms

  50%   below 0ms
  60%   below 0ms
  70%   below 0ms
  80%   below 2ms
  90%   below 7ms
  95%   below 13ms
  98%   below 22ms
  99%   below 28ms
99.9%   below 53ms
```



**结论：netty > 03 >02 >01**

## **6.（必做）**写一段代码，使用 HttpClient 或 OkHttp 访问 [ http://localhost:8801 ](http://localhost:8801/)，代码提交到 GitHub

pom文件引用

```
  <!-- https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp -->
    <dependency>
      <groupId>com.squareup.okhttp3</groupId>
      <artifactId>okhttp</artifactId>
      <version>5.0.0-alpha.1</version>
    </dependency>
```

java代码实现

```java
package com.jike.java.util;


import java.io.IOException;
import okhttp3.*;

/**
 * @description: --
 * @author: MengyuWu
 * @time: 2021/8/14 14:01
 */
public class OKHttpClientTest {

    public static void main(String[] args) throws IOException {
        String url = "http://localhost:8088/api/hello";
        Response response = HttpClientGetUtil(url);
        System.out.printf(response.toString());
    }
    private static Response HttpClientGetUtil (String url) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
            .url(url)
            .get()//默认就是GET请求，可以不写
            .build();
        Response response = okHttpClient.newCall(request).execute();
        return response;
    }
}

```

