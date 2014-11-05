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
package com.alibaba.cobar.server.heartbeat;

import java.nio.channels.SocketChannel;

import com.alibaba.cobar.server.model.Cluster;
import com.alibaba.cobar.server.net.BackendConnection;

/**
 * @author xianmao.hexm
 */
public class CobarNodeConnection extends BackendConnection {

    private long threadId;
    private long clientFlags;
    private CobarNodeResponseHandler responseHandler;
    private Cluster.Node node;

    public CobarNodeConnection(SocketChannel channel) {
        super(channel);
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public long getClientFlags() {
        return clientFlags;
    }

    public void setClientFlags(long clientFlags) {
        this.clientFlags = clientFlags;
    }

    public Cluster.Node getNode() {
        return node;
    }

    public void setNode(Cluster.Node node) {
        this.node = node;
    }

    public CobarNodeResponseHandler getResponseHandler() {
        return responseHandler;
    }

    public void setResponseHandler(CobarNodeResponseHandler responseHandler) {
        responseHandler.setConnection(this);
        this.responseHandler = responseHandler;
    }

    public void connectionAquired() {
        responseHandler.connectionAquired();
    }

    public void okPacket(byte[] data) {
        responseHandler.okPacket(data);
    }

    public void errorPacket(byte[] data) {
        responseHandler.errorPacket(data);
    }

    @Override
    public void error(int code, Throwable t) {
        responseHandler.error(code, t);
    }

}
