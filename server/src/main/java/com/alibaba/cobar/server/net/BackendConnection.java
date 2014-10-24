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
package com.alibaba.cobar.server.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import com.alibaba.cobar.server.model.CobarModel;
import com.alibaba.cobar.server.model.DataSources.DataSource;
import com.alibaba.cobar.server.model.Instances.Instance;
import com.alibaba.cobar.server.model.Machines.Machine;
import com.alibaba.cobar.server.net.nio.NIOConnector;
import com.alibaba.cobar.server.net.nio.NIOProcessor;
import com.alibaba.cobar.server.startup.CobarContainer;
import com.alibaba.cobar.server.util.TimeUtil;

/**
 * @author xianmao.hexm
 */
public abstract class BackendConnection extends AbstractConnection {

    protected NIOConnector connector;
    protected boolean isFinishConnect;
    protected DataSource dataSource;

    public BackendConnection(SocketChannel channel) {
        super(channel);
    }

    public void setConnector(NIOConnector connector) {
        this.connector = connector;
    }

    public void connect(Selector selector) throws IOException {
        CobarModel cm = CobarContainer.getInstance().getConfigModel();
        Instance instance = cm.getInstances().getInstance(dataSource.getInstance());
        Machine machine = cm.getMachines().getMachine(instance.getMachine());
        this.host = machine.getHost();
        this.port = instance.getPort();
        channel.register(selector, SelectionKey.OP_CONNECT, this);
        channel.connect(new InetSocketAddress(host, port));
    }

    public boolean finishConnect() throws IOException {
        if (channel.isConnectionPending()) {
            channel.finishConnect();
            localPort = channel.socket().getLocalPort();
            isFinishConnect = true;
            return true;
        } else {
            return false;
        }
    }

    public void setProcessor(NIOProcessor processor) {
        this.processor = processor;
        this.readBuffer = processor.getBufferPool().allocate();
        processor.addBackend(this);
    }

    public boolean isIdleTimeout() {
        long last = Math.max(statistic.getLastWriteTime(), statistic.getLastReadTime());
        return TimeUtil.currentTimeMillis() > last + idleTimeout;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

}
