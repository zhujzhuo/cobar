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

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.cobar.server.defs.ErrorCode;
import com.alibaba.cobar.server.net.ResponseHandler;
import com.alibaba.cobar.server.net.packet.AbstractPacket;
import com.alibaba.cobar.server.net.packet.CommandPacket;
import com.alibaba.cobar.server.startup.CobarContainer;

/**
 * @author xianmao.hexm
 */
public class MySQLNodeResponseHandler implements ResponseHandler {

    private static final long NORMAL_HEARTBEAT = 10 * 1000L;
    private static final int RETRY_TIMES = 10;
    private static final long ERROR_HEARTBEAT = 100L;

    private MySQLNodeConnection connection;
    private CommandPacket packet;
    private AtomicInteger errorCount;

    public void setConnection(MySQLNodeConnection connection) {
        this.connection = connection;
        this.packet = getHeartbeatPacket();
        this.errorCount = new AtomicInteger(0);
    }

    public void connectionAquired() {
        errorCount.set(0);
        packet.write(connection);
    }

    public void okPacket(byte[] data) {
        errorCount.set(0);
        doHeartbeat(NORMAL_HEARTBEAT);
    }

    public void errorPacket(byte[] data) {
        if (errorCount.get() < RETRY_TIMES) {
            errorCount.incrementAndGet();
            doHeartbeat(ERROR_HEARTBEAT);
        } else {
            errorCount.set(0);
            doHeartbeat(NORMAL_HEARTBEAT);
        }
    }

    public void fieldEofPacket(byte[] header, List<byte[]> fields, byte[] data) {
    }

    public void rowDataPacket(byte[] data) {
    }

    public void rowEofPacket(byte[] data) {
        errorCount.set(0);
        doHeartbeat(NORMAL_HEARTBEAT);
    }

    public void error(int code, Throwable t) {
        connection.close();
        if (errorCount.get() < RETRY_TIMES) {
            errorCount.incrementAndGet();
            newHeartbeat(ERROR_HEARTBEAT);
        } else {
            errorCount.set(0);
            newHeartbeat(NORMAL_HEARTBEAT);
        }
    }

    private void doHeartbeat(long delay) {
        Timer timer = CobarContainer.getInstance().getTimer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                packet.write(connection);
            }
        }, delay);
    }

    private void newHeartbeat(long delay) {
        Timer timer = CobarContainer.getInstance().getTimer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                MySQLNodeConnectionFactory factory = CobarContainer.getInstance().getMysqlNodeFactory();
                try {
                    factory.make(connection.getDataSource(), MySQLNodeResponseHandler.this);
                } catch (Exception e) {
                    error(ErrorCode.ERR_CONNECT_SOCKET, e);
                }
            }
        }, delay);
    }

    private CommandPacket getHeartbeatPacket() {
        CommandPacket packet = new CommandPacket();
        packet.packetId = 0;
        packet.command = AbstractPacket.COM_QUERY;
        packet.arg = "select 1".getBytes();
        return packet;
    }

}
