package com.betterzhang.concurrency.basic;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 如何正确停止线程？为什么volatile标记位的停止方法是错误的？
 */
public class Lesson2 {

    public static void main(String[] args) throws InterruptedException {
        Thread thread1 = new Thread(new StopThread1());
        thread1.start();
        Thread.sleep(5);
        thread1.interrupt();

        Thread thread2 = new Thread(new StopThread2());
        thread2.start();
        Thread.sleep(5);
        thread2.interrupt();

        VolatileCanStop r = new VolatileCanStop();
        Thread thread3 = new Thread(r);
        thread3.start();
        Thread.sleep(3000);
        r.canceled = true;

        ArrayBlockingQueue storage = new ArrayBlockingQueue<>(8);

        Producer producer = new Producer(storage);
        Thread producerThread = new Thread(producer);
        producerThread.start();
        Thread.sleep(500);

        Consumer consumer = new Consumer(storage);
        while (consumer.needMoreNums()) {
            System.out.println(consumer.storage.take() + "被消费了");
            Thread.sleep(100);
        }
        System.out.println("消费者不需要更多数据了。");

        // 一旦消费不需要更多数据了，我们应该让生产者也停下来，但是实际情况却停不下来
        producer.canceled = true;
        System.out.println(producer.canceled);
    }

}

class StopThread1 implements Runnable {

    @Override
    public void run() {
        int count = 0;

        // 在 StopThread 类的 run() 方法中，首先判断线程是否被中断，然后判断 count 值是否小于 1000
        while (!Thread.currentThread().isInterrupted() && count < 1000) {
            System.out.println("count = " + count++);
        }
    }
}

/**
 * sleep 期间能否感受到中断
 * 如果 sleep、wait 等可以让线程进入阻塞的方法使线程休眠了，而处于休眠中的线程被中断，
 * 那么线程是可以感受到中断信号的，并且会抛出一个 InterruptedException 异常，同时清除中断信号，
 * 将中断标记位设置成 false。这样一来就不用担心长时间休眠中线程感受不到中断了，因为即便线程还在休眠，
 * 仍然能够响应中断通知，并抛出异常。
 */
class StopThread2 implements Runnable {

    @Override
    public void run() {
        int count = 0;

        try {
            while (!Thread.currentThread().isInterrupted() && count <= 1000) {
                System.out.println(count);
                count++;
                Thread.sleep(1000000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

/*
 * 两种最佳处理方式
 * 1. 方法签名抛异常， run() 强制 try / catch
 * 2. 再次中断
 */
class StopThread3 {

    // 因为如果线程在休眠期间被中断，那么会自动清除中断信号。
    // 如果这时手动添加中断信号，中断信号依然可以被捕捉到。
    // 这样后续执行的方法依然可以检测到这里发生过中断，可以做出相应的处理，
    // 整个线程可以正常退出。
    private void reInterrupt() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

}

/**
 * volatile 修饰标记位适用的场景
 */
class VolatileCanStop implements Runnable {

    public volatile boolean canceled = false;

    @Override
    public void run() {
        int num = 0;
        try {
            while (!canceled && num < 1000000) {
                if (num % 10 == 0) {
                    System.out.println(num + "是10的倍数。");
                }
                num++;
                Thread.sleep(1);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

/**
 * volatile 修饰标记位不适用的场景
 */
class Producer implements Runnable {

    public volatile boolean canceled = false;

    BlockingQueue storage;

    public Producer(BlockingQueue storage) {
        this.storage = storage;
    }

    @Override
    public void run() {
        int num = 0;
        try {
            while (num <= 100000 && !canceled) {
                if (num % 50 == 0) {
                    storage.put(num);
                    System.out.println(num + "是50的倍数，被放到仓库中了");
                }
                num++;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("生产者结束运行");
        }
    }
}

class Consumer {

    BlockingQueue storage;

    public Consumer(BlockingQueue storage) {
        this.storage = storage;
    }

    public boolean needMoreNums() {
        if (Math.random() > 0.97) {
            return false;
        }
        return true;
    }

}