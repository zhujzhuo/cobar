/*
 * Copyright 1999-2014 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cobar.server.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.cobar.server.util.ExecutorUtil.NameableExecutor;

/**
 * @author xianmao.hexm
 */
public class QueueTest {

    public void testLinkedQueue() {
        final BlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>();
        final AtomicLong count = new AtomicLong(0L);

        //取数据
        NameableExecutor exec = ExecutorUtil.create("testLinkedQueue", 5);
        exec.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (queue.poll() != null) {
                        count.incrementAndGet();
                    }
                }
            }
        });

        //放数据
        new Thread() {
            @Override
            public void run() {
                byte[] buff = new byte[6];
                while (true) {
                    queue.offer(buff);
                }
            }
        }.start();

        //统计
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    int size1 = queue.size();
                    long c1 = count.get();
                    try {
                        Thread.sleep(5000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    long c2 = count.get();
                    int size2 = queue.size();
                    System.out.println("tps=" + (c2 - c1) / 5 + ",size1=" + size1 + ",size2=" + size2);
                }
            }
        }.start();
    }

    public void testBufferQueue() {
        final BufferQueue<byte[]> queue = new BufferQueue<byte[]>(10000);
        final AtomicLong count = new AtomicLong(0L);

        //取数据
        NameableExecutor exec = ExecutorUtil.create("testBufferQueue", 5);
        exec.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (queue.poll() != null) {
                        count.incrementAndGet();
                    }
                }
            }
        });

        //放数据
        new Thread() {
            @Override
            public void run() {
                byte[] buff = new byte[6];
                try {
                    while (true) {
                        queue.put(buff);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        //统计
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    int size1 = queue.size();
                    long c1 = count.get();
                    try {
                        Thread.sleep(5000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    long c2 = count.get();
                    int size2 = queue.size();
                    System.out.println("tps=" + (c2 - c1) / 5 + ",size1=" + size1 + ",size2=" + size2);
                }
            }
        }.start();
    }

    public void testArrayQueue() {
        final BlockingQueue<byte[]> queue = new ArrayBlockingQueue<byte[]>(10000);
        final AtomicLong count = new AtomicLong(0L);

        //取数据
        NameableExecutor exec = ExecutorUtil.create("testArrayQueue", 5);
        exec.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (queue.poll() != null) {
                        count.incrementAndGet();
                    }
                }
            }
        });

        //放数据
        new Thread() {
            @Override
            public void run() {
                byte[] buff = new byte[6];
                while (true) {
                    queue.offer(buff);
                }
            }
        }.start();

        //统计
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    int size1 = queue.size();
                    long c1 = count.get();
                    try {
                        Thread.sleep(5000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    long c2 = count.get();
                    int size2 = queue.size();
                    System.out.println("tps=" + (c2 - c1) / 5 + ",size1=" + size1 + ",size2=" + size2);
                }
            }
        }.start();
    }

    public static void main(String[] args) {
        QueueTest test = new QueueTest();
        test.testLinkedQueue();
        //        test.testBufferQueue();
        //                test.testArrayQueue();
    }

}
