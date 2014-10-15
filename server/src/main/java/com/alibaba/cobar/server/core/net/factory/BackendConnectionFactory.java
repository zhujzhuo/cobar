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
package com.alibaba.cobar.server.core.net.factory;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.alibaba.cobar.server.core.model.DataSources.DataSource;
import com.alibaba.cobar.server.core.net.BackendConnection;
import com.alibaba.cobar.server.core.net.nio.NIOConnector;
import com.alibaba.cobar.server.startup.CobarServer;
import com.alibaba.cobar.server.util.BufferQueue;

/**
 * @author xianmao.hexm
 */
public abstract class BackendConnectionFactory {

    protected int socketRecvBuffer = 16 * 1024;
    protected int socketSendBuffer = 8 * 1024;
    protected int writeQueueCapacity = 8;
    protected long idleTimeout = 8 * 3600 * 1000L;

    public void setSocketRecvBuffer(int socketRecvBuffer) {
        this.socketRecvBuffer = socketRecvBuffer;
    }

    public void setSocketSendBuffer(int socketSendBuffer) {
        this.socketSendBuffer = socketSendBuffer;
    }

    public void setWriteQueueCapacity(int writeQueueCapacity) {
        this.writeQueueCapacity = writeQueueCapacity;
    }

    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    protected abstract BackendConnection getConnection(SocketChannel channel);

    public BackendConnection make(DataSource dataSource) throws IOException {
        SocketChannel channel = getChannel();
        BackendConnection c = getConnection(channel);
        c.setWriteQueue(new BufferQueue<ByteBuffer>(writeQueueCapacity));
        c.setIdleTimeout(idleTimeout);
        c.setDataSource(dataSource);
        NIOConnector connector = CobarServer.getInstance().getConnector();
        c.setConnector(connector);
        connector.postConnect(c);
        return c;
    }

    protected SocketChannel getChannel() throws IOException {
        SocketChannel channel = null;
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            Socket socket = channel.socket();
            socket.setReceiveBufferSize(socketRecvBuffer);
            socket.setSendBufferSize(socketSendBuffer);
            socket.setTcpNoDelay(true);
            socket.setKeepAlive(true);
        } catch (IOException e) {
            closeChannel(channel);
            throw e;
        } catch (RuntimeException e) {
            closeChannel(channel);
            throw e;
        }
        return channel;
    }

    private static void closeChannel(SocketChannel channel) {
        if (channel == null) {
            return;
        }
        Socket socket = channel.socket();
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
        try {
            channel.close();
        } catch (IOException e) {
        }
    }

}
