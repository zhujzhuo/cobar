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
/**
 * (created at 2012-4-17)
 */
package com.alibaba.cobar.server.backend.mysql;

import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.alibaba.cobar.server.core.defs.Alarms;
import com.alibaba.cobar.server.core.model.DataSources.DataSource;
import com.alibaba.cobar.server.core.model.Instances.Instance;
import com.alibaba.cobar.server.util.TimeUtil;

/**
 * @author xianmao.hexm
 */
public class MySQLConnectionPool {

    private static final Logger alarm = Logger.getLogger("alarm");

    private final Instance instance;
    private final MySQLConnectionFactory factory;
    private final MySQLConnection[] items;
    private final ReentrantLock lock = new ReentrantLock();

    private final int size;
    private int activeCount;
    private int idleCount;

    public MySQLConnectionPool(Instance instance, int size) {
        this.instance = instance;
        this.size = size;
        this.items = new MySQLConnection[size];
        this.factory = new MySQLConnectionFactory();
    }

    public Instance getInstance() {
        return instance;
    }

    public void acquire(DataSource dataSource) throws Exception {
        MySQLConnection candidate = null;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            // 激活的连接数大于可池化的连接数，输出日志告知。
            if (activeCount >= size) {
                StringBuilder s = new StringBuilder();
                s.append(Alarms.DEFAULT).append("[instance=").append(instance.getName()).append(",active=");
                s.append(activeCount).append(",size=").append(size).append(']');
                alarm.warn(s.toString());
            }

            // 扫描连接池是否有空闲连接
            final MySQLConnection[] items = this.items;
            int candidateIndex = -1;
            for (int i = 0, len = items.length; idleCount > 0 && i < len; ++i) {
                if (items[i] != null) {
                    MySQLConnection c = items[i];

                    // 当前连接已关闭或者退出
                    if (c.isClosedOrQuit()) {
                        items[i] = null;
                        --idleCount;
                        continue;
                    }

                    // 有对应数据源的连接
                    if (c.getDataSource().equals(dataSource)) {
                        items[i] = null;
                        --idleCount;
                        ++activeCount;
                        c.acquired();
                        return;
                    }

                    // 有空闲连接，但数据源不匹配，记录该连接。
                    if (candidateIndex == -1) {
                        candidateIndex = i;
                        continue;
                    }
                }
            }

            // 表示有空闲连接，但需要做数据源切换。
            if (candidateIndex != -1) {
                candidate = items[candidateIndex];
                items[candidateIndex] = null;
                --idleCount;
                ++activeCount;
            }
        } finally {
            lock.unlock();
        }

        // 池中有空闲连接，但需要切换数据源。
        if (candidate != null) {
            candidate.switchDataSource();
            return;
        }

        // 池中没有空闲连接，创建新的连接。
        factory.make(dataSource);
    }

    public void release(MySQLConnection c) {
        if (c == null || c.isClosedOrQuit()) {
            return;
        }

        // release connection
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            final MySQLConnection[] items = this.items;
            for (int i = 0; i < items.length; i++) {
                if (items[i] == null) {
                    ++idleCount;
                    --activeCount;
                    c.setLastTime(TimeUtil.currentTimeMillis());
                    items[i] = c;
                    return;
                }
            }
        } finally {
            lock.unlock();
        }

        // close excess connection
        c.quit();
    }

    public void deActive() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            --activeCount;
        } finally {
            lock.unlock();
        }
    }

}
