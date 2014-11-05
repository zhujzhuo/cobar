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
package com.alibaba.cobar.server.heartbeat;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.cobar.server.defs.ErrorCode;
import com.alibaba.cobar.server.model.Cluster;
import com.alibaba.cobar.server.net.ResponseHandler;
import com.alibaba.cobar.server.net.packet.AbstractPacket;
import com.alibaba.cobar.server.net.packet.ErrorPacket;
import com.alibaba.cobar.server.net.packet.HeartbeatPacket;
import com.alibaba.cobar.server.startup.CobarContainer;

/**
 * @author xianmao.hexm
 */
public class CobarNodeResponseHandler implements ResponseHandler {

    private static final long NORMAL_HEARTBEAT = 10 * 1000L;
    private static final int RETRY_TIMES = 10;
    private static final long ERROR_HEARTBEAT = 100L;

    private CobarNodeConnection connection;
    private HeartbeatPacket packet;
    private AtomicLong heartbeatId;
    private AtomicInteger errorCount;

    public CobarNodeResponseHandler() {
        HeartbeatPacket packet = new HeartbeatPacket();
        packet.packetId = 0;
        packet.command = AbstractPacket.COM_HEARTBEAT;
        this.packet = packet;
        this.heartbeatId = new AtomicLong(0L);
        this.errorCount = new AtomicInteger(0);
    }

    public void setConnection(CobarNodeConnection connection) {
        this.connection = connection;
    }

    public void connectionAquired() {
        errorCount.set(0);
        packet.id = heartbeatId.incrementAndGet();
        packet.write(connection);
    }

    public void okPacket(byte[] data) {
        errorCount.set(0);
        connection.getNode().getOnline().compareAndSet(false, true);
        doHeartbeat(NORMAL_HEARTBEAT);
    }

    public void errorPacket(byte[] data) {
        Cluster.Node node = connection.getNode();
        ErrorPacket err = new ErrorPacket();
        err.read(data);
        switch (err.errno) {
        case ErrorCode.ER_SERVER_SHUTDOWN: {
            errorCount.set(0);
            node.getOnline().compareAndSet(true, false);
            doHeartbeat(NORMAL_HEARTBEAT);
            break;
        }
        default:
            if (node.getOnline().get() && errorCount.get() < RETRY_TIMES) {
                errorCount.incrementAndGet();
                doHeartbeat(ERROR_HEARTBEAT);
            } else {
                errorCount.set(0);
                node.getOnline().compareAndSet(true, false);
                doHeartbeat(NORMAL_HEARTBEAT);
            }
        }
    }

    public void error(int code, Throwable t) {
        connection.close();
        Cluster.Node node = connection.getNode();
        if (node.getOnline().get() && errorCount.get() < RETRY_TIMES) {
            errorCount.incrementAndGet();
            newHeartbeat(ERROR_HEARTBEAT);
        } else {
            errorCount.set(0);
            node.getOnline().compareAndSet(true, false);
            newHeartbeat(NORMAL_HEARTBEAT);
        }
    }

    private void doHeartbeat(long delay) {
        Timer timer = CobarContainer.getInstance().getTimer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                packet.id = heartbeatId.incrementAndGet();
                packet.write(connection);
            }
        }, delay);
    }

    private void newHeartbeat(long delay) {
        Timer timer = CobarContainer.getInstance().getTimer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                CobarNodeConnectionFactory factory = CobarContainer.getInstance().getCobarNodeFactory();
                try {
                    factory.make(connection.getNode(), CobarNodeResponseHandler.this);
                } catch (Exception e) {
                    error(ErrorCode.ERR_CONNECT_SOCKET, e);
                }
            }
        }, delay);
    }

}
