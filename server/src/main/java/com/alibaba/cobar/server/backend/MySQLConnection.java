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

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

import org.apache.log4j.Logger;

import com.alibaba.cobar.server.backend.rshandler.ConnectionAquiredHandler;
import com.alibaba.cobar.server.defs.Capabilities;
import com.alibaba.cobar.server.defs.ErrorCode;
import com.alibaba.cobar.server.net.BackendConnection;
import com.alibaba.cobar.server.net.packet.AbstractPacket;
import com.alibaba.cobar.server.net.packet.CommandPacket;
import com.alibaba.cobar.server.route.RouteResultsetNode;

/**
 * @author xianmao.hexm
 */
public class MySQLConnection extends BackendConnection {

    private static final Logger LOGGER = Logger.getLogger(MySQLConnection.class);
    private static final long DEFAULT_CLIENT_FLAGS = defaultClientFlags();

    private long threadId;
    private String charset;
    private long clientFlags;
    private boolean isAuthenticated;

    public MySQLConnection(SocketChannel channel) {
        super(channel);
        this.clientFlags = DEFAULT_CLIENT_FLAGS;
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

    @Override
    public void idleCheck() {
    }

    @Override
    public void error(int errCode, Throwable t) {
        LOGGER.warn(toString(), t);
        switch (errCode) {
        case ErrorCode.ERR_HANDLE_DATA:
            // TODO handle error
            break;
        case ErrorCode.ERR_PUT_WRITE_QUEUE:
            // TODO handle error
            break;
        default:
            close();
        }
    }

    public void execute(RouteResultsetNode rrn) throws IOException {
        CommandPacket packet = new CommandPacket();
        packet.packetId = 0;
        packet.command = AbstractPacket.COM_QUERY;
        packet.arg = rrn.getStatement().getBytes(charset);
        packet.write(this);
    }

    public void connectionAquired(ConnectionAquiredHandler handler) {
        setHandler(new MySQLDispatcher(this));
        handler.handle(this);
    }

    public void okPacket(byte[] data) {
        LOGGER.info("okPacket");
    }

    public void errorPacket(byte[] data) {
        LOGGER.info("errorPacket");
    }

    public void fieldEofPacket(byte[] header, List<byte[]> fields, byte[] data) {
        LOGGER.info("fieldEofPacket");
    }

    public void rowEofPacket(byte[] data) {
        LOGGER.info("rowEofPacket");
    }

    public void rowDataPacket(byte[] data) {
        LOGGER.info("rowDataPacket");
    }

    private static long defaultClientFlags() {
        int flag = 0;
        flag |= Capabilities.CLIENT_LONG_PASSWORD;
        flag |= Capabilities.CLIENT_FOUND_ROWS;
        flag |= Capabilities.CLIENT_LONG_FLAG;
        flag |= Capabilities.CLIENT_CONNECT_WITH_DB;
        // flag |= Capabilities.CLIENT_NO_SCHEMA;
        // flag |= Capabilities.CLIENT_COMPRESS;
        flag |= Capabilities.CLIENT_ODBC;
        // flag |= Capabilities.CLIENT_LOCAL_FILES;
        flag |= Capabilities.CLIENT_IGNORE_SPACE;
        flag |= Capabilities.CLIENT_PROTOCOL_41;
        flag |= Capabilities.CLIENT_INTERACTIVE;
        // flag |= Capabilities.CLIENT_SSL;
        flag |= Capabilities.CLIENT_IGNORE_SIGPIPE;
        flag |= Capabilities.CLIENT_TRANSACTIONS;
        // flag |= Capabilities.CLIENT_RESERVED;
        flag |= Capabilities.CLIENT_SECURE_CONNECTION;
        // client extension
        // flag |= Capabilities.CLIENT_MULTI_STATEMENTS;
        // flag |= Capabilities.CLIENT_MULTI_RESULTS;
        return flag;
    }

}
