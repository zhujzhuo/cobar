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
package com.alibaba.cobar.server.statistics;

import com.alibaba.cobar.server.util.TimeUtil;

/**
 * @author xianmao.hexm
 */
public class ConnectionStatistic {

    private long startupTime;
    private long lastReadTime;
    private long lastWriteTime;
    private long netInBytes;
    private long netOutBytes;
    private int writeAttempts;

    public ConnectionStatistic() {
        this.startupTime = TimeUtil.currentTimeMillis();
        this.lastReadTime = startupTime;
        this.lastWriteTime = startupTime;
        this.netInBytes = 0L;
        this.netOutBytes = 0L;
    }

    public long getStartupTime() {
        return startupTime;
    }

    public void setStartupTime(long startupTime) {
        this.startupTime = startupTime;
    }

    public long getLastReadTime() {
        return lastReadTime;
    }

    public void setLastReadTime(long lastReadTime) {
        this.lastReadTime = lastReadTime;
    }

    public long getLastWriteTime() {
        return lastWriteTime;
    }

    public void setLastWriteTime(long lastWriteTime) {
        this.lastWriteTime = lastWriteTime;
    }

    public long getNetInBytes() {
        return netInBytes;
    }

    public void addNetInBytes(long bytes) {
        netInBytes += bytes;
    }

    public long getNetOutBytes() {
        return netOutBytes;
    }

    public void addNetOutBytes(long bytes) {
        netOutBytes += bytes;
    }

    public int getWriteAttempts() {
        return writeAttempts;
    }

    public int writeAttemptsPlus() {
        return ++writeAttempts;
    }

}
