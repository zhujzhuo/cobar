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
/**
 * (created at 2012-4-19)
 */
package com.alibaba.cobar.server.session;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.cobar.server.backend.MySQLConnection;
import com.alibaba.cobar.server.defs.ErrorCode;
import com.alibaba.cobar.server.frontend.ServerConnection;
import com.alibaba.cobar.server.model.DataNodes;
import com.alibaba.cobar.server.net.packet.ErrorPacket;
import com.alibaba.cobar.server.net.packet.OkPacket;
import com.alibaba.cobar.server.route.RouteResultsetNode;
import com.alibaba.cobar.server.startup.CobarContainer;
import com.alibaba.cobar.server.util.ByteBufferUtil;
import com.alibaba.cobar.server.util.StringUtil;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 * @author xianmao.hexm
 */
public class SingleNodeHandler implements MySQLResponse, Terminatable {

    private final RouteResultsetNode node;
    private final ServerSession session;
    private byte packetId;
    private volatile ByteBuffer buffer;
    private boolean isRunning;
    private Runnable terminateCallBack;
    private ReentrantLock lock = new ReentrantLock();

    public SingleNodeHandler(RouteResultsetNode node, ServerSession session) {
        this.session = session;
        this.node = node;
    }

    public void execute() {
        lock.lock();
        try {
            this.isRunning = true;
            this.packetId = 0;
            this.buffer = session.getSource().allocate();
        } finally {
            lock.unlock();
        }

        MySQLConnection c = session.getTarget(node);
        if (c == null) {
            DataNodes dns = CobarContainer.getInstance().getConfigModel().getDataNodes();
            DataNodes.DataNode dn = dns.getDataNode(node.getName());
            dn.acquireConnection(this);
        } else {
            c.setRunning(true);
            _execute(c);
        }
    }

    @Override
    public void connectionAcquired(MySQLConnection c) {
        session.addTarget(node, c);
        c.setRunning(true);
        _execute(c);
    }

    @Override
    public void connectionError(Throwable e, MySQLConnection c) {
        if (!session.closeConnection(node)) {
            c.close();
        }
        endRunning();
        ErrorPacket err = new ErrorPacket();
        err.packetId = ++packetId;
        err.errno = ErrorCode.ER_YES;
        err.message = StringUtil.encode(e.getMessage(), session.getSource().getCharset());
        ServerConnection source = session.getSource();
        source.write(err.write(buffer, source));
    }

    @Override
    public void error(byte[] err, MySQLConnection c) {
        c.setRunning(false);
        if (c.isAutocommit()) {
            session.clearConnections();
        }
        endRunning();
        ByteBufferUtil.write(err, session.getSource());
    }

    @Override
    public void ok(byte[] data, MySQLConnection c) {
        boolean executeResponse = false;
        try {
            executeResponse = c.syncAndExcute();
        } catch (UnsupportedEncodingException e) {
            executeException(c);
        }
        if (executeResponse) {
            c.setRunning(false);
            ServerConnection source = session.getSource();
            if (source.isAutocommit()) {
                session.clearConnections();
            }
            endRunning();
            OkPacket ok = new OkPacket();
            ok.read(data);
            source.setLastInsertId(ok.insertId);
            buffer = ByteBufferUtil.write(data, buffer, source);
            source.write(buffer);
        }
    }

    @Override
    public void rowEof(byte[] eof, MySQLConnection c) {
        ServerConnection source = session.getSource();
        c.setRunning(false);
        c.recordSql(source.getHost(), source.getSchema(), node.getStatement());
        if (source.isAutocommit()) {
            session.clearConnections();
        }
        endRunning();
        buffer = ByteBufferUtil.write(eof, buffer, source);
        source.write(buffer);
    }

    @Override
    public void fieldEof(byte[] header, List<byte[]> fields, byte[] eof, MySQLConnection c) {
        ServerConnection source = session.getSource();
        buffer = session.getSource().allocate();
        ++packetId;
        buffer = ByteBufferUtil.write(header, buffer, source);
        for (int i = 0, len = fields.size(); i < len; ++i) {
            ++packetId;
            buffer = ByteBufferUtil.write(fields.get(i), buffer, source);
        }
        ++packetId;
        buffer = ByteBufferUtil.write(eof, buffer, source);
        source.write(buffer);
    }

    @Override
    public void rowData(byte[] row, MySQLConnection c) {
        ++packetId;
        buffer = ByteBufferUtil.write(row, buffer, session.getSource());
    }

    @Override
    public void terminate(Runnable callback) {
        boolean zeroReached = false;
        lock.lock();
        try {
            if (isRunning) {
                terminateCallBack = callback;
            } else {
                zeroReached = true;
            }
        } finally {
            lock.unlock();
        }
        if (zeroReached) {
            callback.run();
        }
    }

    private void endRunning() {
        Runnable callback = null;
        lock.lock();
        try {
            if (isRunning) {
                isRunning = false;
                callback = terminateCallBack;
                terminateCallBack = null;
            }
        } finally {
            lock.unlock();
        }
        if (callback != null) {
            callback.run();
        }
    }

    private void _execute(MySQLConnection c) {
        if (session.closed()) {
            c.setRunning(false);
            endRunning();
            session.clearConnections();
            return;
        }
        c.setResponseHandler(this);
        try {
            c.execute(node, session.getSource(), session.getSource().isAutocommit());
        } catch (UnsupportedEncodingException e1) {
            executeException(c);
            return;
        }
    }

    private void executeException(MySQLConnection c) {
        c.setRunning(false);
        endRunning();
        session.clearConnections();
        ErrorPacket err = new ErrorPacket();
        err.packetId = ++packetId;
        err.errno = ErrorCode.ER_YES;
        err.message = StringUtil.encode("unknown backend charset: " + c.getCharset(), session.getSource().getCharset());
        ServerConnection source = session.getSource();
        source.write(err.write(buffer, source));
    }

}
