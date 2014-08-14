/*
 * Copyright 1999-2012 Alibaba Group.
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

import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.alibaba.cobar.config.model.DataSourceConfig;
import com.alibaba.cobar.server.backend.BlockingChannel;
import com.alibaba.cobar.server.backend.BlockingChannelFactory;
import com.alibaba.cobar.server.backend.BlockingMySQLChannelFactory;
import com.alibaba.cobar.server.backend.heartbeat.MySQLHeartbeat;
import com.alibaba.cobar.server.defs.Alarms;
import com.alibaba.cobar.server.statistic.SQLRecorder;

/**
 * @author xianmao.hexm 2011-4-26 上午11:12:13
 */
public final class MySQLDataSource {
    private static final Logger LOGGER = Logger.getLogger(MySQLDataSource.class);
    private static final Logger ALARM = Logger.getLogger("alarm");

    private final MySQLDataNode node;
    private final int index;
    private final String name;
    private final DataSourceConfig config;
    private int activeCount;
    private int idleCount;
    private final int size;
    private final BlockingChannel[] items;
    private final ReentrantLock lock;
    private final BlockingChannelFactory factory;
    private final MySQLHeartbeat heartbeat;
    private final SQLRecorder sqlRecorder;

    public MySQLDataSource(MySQLDataNode node, int index, DataSourceConfig config, int size) {
        this.node = node;
        this.index = index;
        this.name = config.getName();
        this.config = config;
        this.size = size;
        this.items = new BlockingChannel[size];
        this.lock = new ReentrantLock();
        this.factory = new BlockingMySQLChannelFactory();
        this.heartbeat = new MySQLHeartbeat(this);
        this.sqlRecorder = new SQLRecorder(config.getSqlRecordCount());
    }

    public MySQLDataNode getNode() {
        return node;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public DataSourceConfig getConfig() {
        return config;
    }

    public int size() {
        return size;
    }

    public int getActiveCount() {
        return activeCount;
    }

    public int getIdleCount() {
        return idleCount;
    }

    public MySQLHeartbeat getHeartbeat() {
        return heartbeat;
    }

    public SQLRecorder getSqlRecorder() {
        return sqlRecorder;
    }

    public void startHeartbeat() {
        heartbeat.start();
    }

    public void stopHeartbeat() {
        heartbeat.stop();
    }

    public void doHeartbeat() {
        if (!heartbeat.isStop()) {
            try {
                heartbeat.heartbeat();
            } catch (Throwable e) {
                LOGGER.error(name + " heartbeat error.", e);
            }
        }
    }

    /**
     * @return never null
     */
    public BlockingChannel getChannel() throws Exception {
        // 尝试从池中取得可用资源
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            // 当活跃资源大于等于池大小时，记录告警信息。
            if (activeCount >= size) {
                StringBuilder s = new StringBuilder();
                s.append(Alarms.DEFAULT).append("[name=").append(name).append(",active=");
                s.append(activeCount).append(",size=").append(size).append(']');
                ALARM.error(s.toString());
            }

            // 检查池中是否有可用资源
            final BlockingChannel[] items = this.items;
            for (int i = 0; idleCount > 0 && i < items.length; i++) {
                if (items[i] != null) {
                    BlockingChannel c = items[i];
                    items[i] = null;
                    --idleCount;
                    if (c.isClosed()) {
                        continue;
                    } else {
                        ++activeCount;
                        return c;
                    }
                }
            }

            // 将创建新连接，在此先假设创建成功。
            ++activeCount;
        } finally {
            lock.unlock();
        }

        // 创建新的资源
        BlockingChannel c = factory.make(this);
        try {
            c.connect(node.getConfig().getWaitTimeout());
        } catch (Exception e) {
            lock.lock();
            try {
                --activeCount;
            } finally {
                lock.unlock();
            }
            c.closeNoActive();
            throw e;
        }
        return c;
    }

    public void releaseChannel(BlockingChannel c) {
        // 状态检查
        if (c == null || c.isClosed()) {
            return;
        }

        // 释放资源
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            final BlockingChannel[] items = this.items;
            for (int i = 0; i < items.length; i++) {
                if (items[i] == null) {
                    ++idleCount;
                    --activeCount;
                    c.setLastActiveTime(TimeUtil.currentTimeMillis());
                    items[i] = c;
                    return;
                }
            }
        } finally {
            lock.unlock();
        }

        // 关闭多余的资源
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

    public void clear() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            final BlockingChannel[] items = this.items;
            for (int i = 0; i < items.length; i++) {
                BlockingChannel c = items[i];
                if (c != null) {
                    c.closeNoActive();
                    --idleCount;
                    items[i] = null;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void idleCheck(long timeout) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            final BlockingChannel[] items = this.items;
            long time = TimeUtil.currentTimeMillis() - timeout;
            for (int i = 0; i < items.length; i++) {
                BlockingChannel c = items[i];
                if (c != null && time > c.getLastAcitveTime()) {
                    c.closeNoActive();
                    --idleCount;
                    items[i] = null;
                }
            }
        } finally {
            lock.unlock();
        }
    }

}
