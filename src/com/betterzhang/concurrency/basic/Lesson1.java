package com.betterzhang.concurrency.basic;

import java.util.Random;
import java.util.concurrent.*;

/**
 * 第01讲：为何说只有1种实现线程的方法
 *
 * 本质上，实现线程只有一种方式，而要想实现线程执行的内容，却有两种方式，
 * 也就是可以通过 实现 Runnable 接口的方式，或是继承 Thread 类重写 run() 方法的方式，
 * 把我们想要执行的代码传入，让线程去执行，在此基础上，如果我们还想有更多实现线程的方式，
 * 比如线程池和 Timer 定时器，只需要在此基础上进行封装即可。
 *
 * 我们应该优先选择通过实现 Runnable 接口的方式来创建线程。
 * 1. 在这种情况下，实现了 Runnable 与 Thread 类的解耦，Thread 类负责线程启动和属性设置等内容，权责分明。
 * 2. 第二点就是在某些情况下可以提高性能。
 * 3. Java 语言不支持双继承，如果我们的类一旦继承了 Thread 类，那么它后续就没有办法再继承其他的类。
 */
public class Lesson1 {

    public static void main(String[] args) {
        // 实现 Runnable 接口
        RunnableThread target = new RunnableThread();
        Thread thread1 = new Thread(target);
        thread1.start();

        // 继承 Thread 类
        Thread thread2 = new ExtendsThread();
        thread2.start();

        // 有返回值的 Callable 创建线程
        // 创建线程池
        ExecutorService service = Executors.newFixedThreadPool(10);
        // 提交任务，并用 Future 提交返回结果
        Future<Integer> future = service.submit(new CallableTask());

        // 匿名内部类创建线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + ": 匿名内部类创建线程");
            }
        }).start();

        // lambda 表达式创建线程
        new Thread(() -> System.out.println(Thread.currentThread().getName() + ": lambda表达式创建线程")).start();
    }

}

/*
 * 实现线程的方式到底有几种？
 */

/**
 * 实现 Runnable 接口
 * 1. 通过 RunnableThread 类实现 Runnable 接口
 * 2. 重写 run() 方法
 * 3. 把这个实现了 run() 方法的实例传到 Thread 类中就可以实现多线程
 */
class RunnableThread implements Runnable {

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + ": 用实现Runnable接口实现线程");
    }
}

/**
 * 继承 Thread 类
 * 1. 通过 ExtendsThread 类继承 Thread 类
 * 2. 重写其中的 run() 方法
 */
class ExtendsThread extends Thread {

    @Override
    public void run() {
        System.out.println(getName() + ": 用Thread类实现线程");
    }
}

/*
 * 线程池创建线程
 * 深入线程池中的源码，来看看线程池是怎么实现线程的？
 * 本质上是通过线程工厂创建线程的，默认采用 DefaultThreadFactory ，它会给线程池创建的线程设置一些默认值。
 * 但是无论怎么设置这些属性，最终它还是通过 new Thread() 创建线程的，只不过这里的构造函数传入的参数要多一些。
 */
/*
static class DefaultThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    DefaultThreadFactory() {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        namePrefix = "pool-" +
                poolNumber.getAndIncrement() +
                "-thread-";
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                namePrefix + threadNumber.getAndIncrement(),
                0);
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}
 */

/**
 * 有返回值的 Callable 创建线程
 */
class CallableTask implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        return new Random().nextInt();
    }
}

/*
 * 其他创建方式
 * 定时器 Timer
 * 深入分析定时器的源码发现，本质上它还是会有一个继承自 Thread 类的 TimerThread
 */
/*
class TimerThread extends Thread {

}
 */

/*
 * 其他方法
 * 匿名内部类创建线程
 */
/*
new Thread(new Runnable() {
    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName());
    }
}).start();
 */

/*
 * 其他方法
 * lambda 表达式创建线程
 */
/*
new Thread(() -> System.out.println(Thread.currentThread().getName())).start();
 */