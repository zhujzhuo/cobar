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
package com.alibaba.cobar.server.backend;

import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.alibaba.cobar.server.defs.Alarms;
import com.alibaba.cobar.server.model.DataSources.DataSource;
import com.alibaba.cobar.server.util.TimeUtil;

/**
 * @author xianmao.hexm
 */
public class MySQLConnectionPool {

    private static final Logger ALARM = Logger.getLogger("alarm");

    private final DataSource dataSource;
    private final int size;
    private final MySQLConnection[] items;
    private final MySQLConnectionFactory factory;
    private final ReentrantLock lock = new ReentrantLock();
    private int activeCount;
    private int idleCount;

    public MySQLConnectionPool(DataSource dataSource, int size) {
        this.dataSource = dataSource;
        this.size = size;
        this.items = new MySQLConnection[size];
        this.factory = new MySQLConnectionFactory();
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void getConnection(final MySQLResponseHandler handler) throws Exception {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            // 连接激活数已经大于等于poolsize，告警。
            if (activeCount >= size) {
                StringBuilder sb = new StringBuilder();
                sb.append(Alarms.DEFAULT).append("[name=").append(dataSource.getName()).append(",active=");
                sb.append(activeCount).append(",size=").append(size).append(']');
                ALARM.error(sb.toString());
            }

            // 从池中获取连接
            final MySQLConnection[] items = this.items;
            for (int i = 0, len = items.length; idleCount > 0 && i < len; ++i) {
                if (items[i] != null) {
                    MySQLConnection conn = items[i];
                    items[i] = null;
                    --idleCount;
                    if (conn.isClosed()) {
                        continue;
                    } else {
                        ++activeCount;
                        handler.connectionAquired();
                        return;
                    }
                }
            }
            ++activeCount;
        } finally {
            lock.unlock();
        }

        // 创建新的连接
        factory.make(this, new MySQLResponseHandlerProxy(handler) {
            private boolean deactived;

            @Override
            public void error(int code, Throwable t) {
                lock.lock();
                try {
                    if (!deactived) {
                        --activeCount;
                        deactived = true;
                    }
                } finally {
                    lock.unlock();
                }
                handler.error(code, t);
            }
        });
    }

    public void releaseConnection(MySQLConnection c) {
        if (c == null || c.isClosed()) {
            return;
        }

        // 把连接放回池中
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

        // 关闭多余的连接
        c.close();
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
