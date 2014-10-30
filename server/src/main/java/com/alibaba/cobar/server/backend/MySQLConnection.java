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
package com.alibaba.cobar.server.backend;

import java.nio.channels.SocketChannel;
import java.util.List;

import com.alibaba.cobar.server.net.BackendConnection;

/**
 * @author xianmao.hexm
 */
public class MySQLConnection extends BackendConnection {

    private long threadId;
    private String charset;
    private long clientFlags;
    private boolean isAuthenticated;
    private long lastTime;
    private MySQLResponseHandler responseHandler;
    private MySQLConnectionPool pool;
    private boolean inThePool;

    public MySQLConnection(SocketChannel channel) {
        super(channel);
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public long getClientFlags() {
        return clientFlags;
    }

    public void setClientFlags(long clientFlags) {
        this.clientFlags = clientFlags;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(boolean isAuthenticated) {
        this.isAuthenticated = isAuthenticated;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public MySQLConnectionPool getPool() {
        return pool;
    }

    public void setPool(MySQLConnectionPool pool) {
        this.pool = pool;
    }

    public boolean isInThePool() {
        return inThePool;
    }

    public void setInThePool(boolean inThePool) {
        this.inThePool = inThePool;
    }

    public void release() {
        if (pool != null) {
            pool.releaseConnection(this);
        }
    }

    @Override
    public boolean close() {
        if (pool != null) {
            return pool.closeConnection(this);
        } else {
            return independentClose();
        }
    }

    /**
     * 独立关闭连接
     */
    public boolean independentClose() {
        return super.close();
    }

    public MySQLResponseHandler getResponseHandler() {
        return responseHandler;
    }

    public void setResponseHandler(MySQLResponseHandler responseHandler) {
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

    public void fieldEofPacket(byte[] header, List<byte[]> fields, byte[] data) {
        responseHandler.fieldEofPacket(header, fields, data);
    }

    public void rowDataPacket(byte[] data) {
        responseHandler.rowDataPacket(data);
    }

    public void rowEofPacket(byte[] data) {
        responseHandler.rowEofPacket(data);
    }

    @Override
    public void error(int code, Throwable t) {
        responseHandler.error(code, t);
    }

}
