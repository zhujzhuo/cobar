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
package com.alibaba.cobar.server.backend.executor;

import static com.alibaba.cobar.server.route.RouteResultsetNode.DEFAULT_REPLICA_INDEX;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.alibaba.cobar.config.ErrorCode;
import com.alibaba.cobar.server.CobarConfig;
import com.alibaba.cobar.server.CobarServer;
import com.alibaba.cobar.server.backend.BlockingChannel;
import com.alibaba.cobar.server.backend.BlockingMySQLChannel;
import com.alibaba.cobar.server.exeception.UnknownDataNodeException;
import com.alibaba.cobar.server.frontend.ServerConnection;
import com.alibaba.cobar.server.frontend.session.BlockingSession;
import com.alibaba.cobar.server.net.packet.AbstractPacket;
import com.alibaba.cobar.server.net.packet.BinaryPacket;
import com.alibaba.cobar.server.net.packet.EOFPacket;
import com.alibaba.cobar.server.net.packet.ErrorPacket;
import com.alibaba.cobar.server.net.packet.FieldPacket;
import com.alibaba.cobar.server.net.packet.OkPacket;
import com.alibaba.cobar.server.route.RouteResultset;
import com.alibaba.cobar.server.route.RouteResultsetNode;
import com.alibaba.cobar.server.util.MySQLDataNode;
import com.alibaba.cobar.server.util.PacketUtil;
import com.alibaba.cobar.server.util.StringUtil;

/**
 * 单节点数据执行器
 * 
 * @author xianmao.hexm
 */
public final class BlockingSingleNodeExecutor extends BlockingNodeExecutor {
    private static final Logger LOGGER = Logger.getLogger(BlockingSingleNodeExecutor.class);
    private static final int RECEIVE_CHUNK_SIZE = 64 * 1024;

    private byte packetId;
    private boolean isRunning = false;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition taskFinished = lock.newCondition();

    @Override
    public void terminate() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            while (isRunning) {
                taskFinished.await();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 单数据节点执行
     */
    public void execute(RouteResultsetNode rrn, BlockingSession ss, int flag) {
        // 初始化
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            this.packetId = 0;
            this.isRunning = true;
        } finally {
            lock.unlock();
        }

        // 检查连接是否已关闭
        if (ss.getSource().isClosed()) {
            endRunning();
            return;
        }

        // 单节点处理
        BlockingChannel c = ss.getTarget().get(rrn);
        if (c != null) {
            c.setRunning(true);
            bindingExecute(rrn, ss, c, flag);
        } else {
            newExecute(rrn, ss, flag);
        }
    }

    /**
     * 已绑定数据通道的执行
     */
    private void bindingExecute(final RouteResultsetNode rrn, final BlockingSession ss, final BlockingChannel c, final int flag) {
        ss.getSource().getProcessor().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                execute0(rrn, ss, c, flag);
            }
        });
    }

    /**
     * 新数据通道的执行
     */
    private void newExecute(final RouteResultsetNode rrn, final BlockingSession ss, final int flag) {
        final ServerConnection sc = ss.getSource();

        // 检查数据节点是否存在
        CobarConfig conf = CobarServer.getInstance().getConfig();
        final MySQLDataNode dn = conf.getDataNodes().get(rrn.getName());
        if (dn == null) {
            LOGGER.warn(new StringBuilder().append(sc).append(rrn).toString(), new UnknownDataNodeException());
            handleError(ErrorCode.ER_BAD_DB_ERROR, "Unknown dataNode '" + rrn.getName() + "'", ss);
            return;
        }

        // 提交执行任务
        sc.getProcessor().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                // 取得数据通道
                int i = rrn.getReplicaIndex();
                BlockingChannel c = null;
                try {
                    c = (i == DEFAULT_REPLICA_INDEX) ? dn.getChannel() : dn.getChannel(i);
                } catch (Exception e) {
                    LOGGER.warn(new StringBuilder().append(sc).append(rrn).toString(), e);
                    String msg = e.getMessage();
                    handleError(ErrorCode.ER_BAD_DB_ERROR, msg == null ? e.getClass().getSimpleName() : msg, ss);
                    return;
                }

                // 检查连接是否已关闭。
                if (sc.isClosed()) {
                    c.release();
                    endRunning();
                    return;
                }

                // 绑定数据通道
                c.setRunning(true);
                BlockingChannel old = ss.getTarget().put(rrn, c);
                if (old != null && old != c) {
                    old.close();
                }

                // 执行
                execute0(rrn, ss, c, flag);
            }
        });
    }

    /**
     * 数据通道执行
     */
    private void execute0(RouteResultsetNode rrn, BlockingSession ss, BlockingChannel c, int flag) {
        final ServerConnection sc = ss.getSource();

        // 检查连接是否已关闭
        if (sc.isClosed()) {
            c.setRunning(false);
            endRunning();
            ss.clear();
            return;
        }

        try {
            // 执行并等待返回
            BlockingMySQLChannel mc = (BlockingMySQLChannel) c;
            BinaryPacket bin = mc.execute(rrn, sc, sc.isAutocommit());

            // 接收和处理数据
            switch (bin.data[0]) {
            case OkPacket.FIELD_COUNT: {
                mc.setRunning(false);
                if (mc.isAutocommit()) {
                    ss.clear();
                }
                endRunning();
                bin.packetId = ++packetId;// OK_PACKET
                // set lastInsertId
                setLastInsertId(bin, sc);
                sc.write(bin.write(sc.allocate(), sc));
                break;
            }
            case ErrorPacket.FIELD_COUNT: {
                LOGGER.warn(mc.getErrLog(rrn.getStatement(), mc.getErrMessage(bin), sc));
                mc.setRunning(false);
                if (mc.isAutocommit()) {
                    ss.clear();
                }
                endRunning();
                bin.packetId = ++packetId;// ERROR_PACKET
                sc.write(bin.write(sc.allocate(), sc));
                break;
            }
            default: // HEADER|FIELDS|FIELD_EOF|ROWS|LAST_EOF
                handleResultSet(rrn, ss, mc, bin, flag);
            }
        } catch (IOException e) {
            LOGGER.warn(new StringBuilder().append(sc).append(rrn).toString(), e);
            c.close();
            String msg = e.getMessage();
            handleError(ErrorCode.ER_YES, msg == null ? e.getClass().getSimpleName() : msg, ss);
        } catch (RuntimeException e) {
            LOGGER.warn(new StringBuilder().append(sc).append(rrn).toString(), e);
            c.close();
            String msg = e.getMessage();
            handleError(ErrorCode.ER_YES, msg == null ? e.getClass().getSimpleName() : msg, ss);
        }
    }

    /**
     * 处理结果集数据
     */
    private void handleResultSet(RouteResultsetNode rrn, BlockingSession ss, BlockingMySQLChannel mc, BinaryPacket bin, int flag)
            throws IOException {
        final ServerConnection sc = ss.getSource();

        bin.packetId = ++packetId;// HEADER
        List<AbstractPacket> headerList = new LinkedList<AbstractPacket>();
        headerList.add(bin);
        for (;;) {
            bin = mc.receive();
            switch (bin.data[0]) {
            case ErrorPacket.FIELD_COUNT: {
                LOGGER.warn(mc.getErrLog(rrn.getStatement(), mc.getErrMessage(bin), sc));
                mc.setRunning(false);
                if (mc.isAutocommit()) {
                    ss.clear();
                }
                endRunning();
                bin.packetId = ++packetId;// ERROR_PACKET
                sc.write(bin.write(sc.allocate(), sc));
                return;
            }
            case EOFPacket.FIELD_COUNT: {
                bin.packetId = ++packetId;// FIELD_EOF
                ByteBuffer bb = sc.allocate();
                for (AbstractPacket packet : headerList) {
                    bb = packet.write(bb, sc);
                }
                bb = bin.write(bb, sc);
                headerList = null;
                handleRowData(rrn, ss, mc, bb, packetId);
                return;
            }
            default:
                bin.packetId = ++packetId;// FIELDS
                switch (flag) {
                case RouteResultset.REWRITE_FIELD:
                    StringBuilder fieldName = new StringBuilder();
                    fieldName.append("Tables_in_").append(ss.getSource().getSchema());
                    FieldPacket field = PacketUtil.getField(bin, fieldName.toString());
                    headerList.add(field);
                    break;
                default:
                    headerList.add(bin);
                }
            }
        }
    }

    /**
     * 处理RowData数据
     */
    private void handleRowData(RouteResultsetNode rrn, BlockingSession ss, BlockingMySQLChannel mc, ByteBuffer bb, byte id)
            throws IOException {
        final ServerConnection sc = ss.getSource();
        this.packetId = id;
        BinaryPacket bin = null;
        int size = 0;
        try {
            for (;;) {
                bin = mc.receive();
                switch (bin.data[0]) {
                case ErrorPacket.FIELD_COUNT:
                    LOGGER.warn(mc.getErrLog(rrn.getStatement(), mc.getErrMessage(bin), sc));
                    mc.setRunning(false);
                    if (mc.isAutocommit()) {
                        ss.clear();
                    }
                    endRunning();
                    bin.packetId = ++packetId;// ERROR_PACKET
                    bb = bin.write(bb, sc);
                    sc.write(bb);
                    return;
                case EOFPacket.FIELD_COUNT:
                    mc.setRunning(false);
                    if (mc.isAutocommit()) {
                        ss.clear();
                    }
                    endRunning();
                    bin.packetId = ++packetId;// LAST_EOF
                    bb = bin.write(bb, sc);
                    sc.write(bb);
                    return;
                default:
                    bin.packetId = ++packetId;// ROWS
                    bb = bin.write(bb, sc);
                    size += bin.packetLength;
                    if (size > RECEIVE_CHUNK_SIZE) {
                        handleNext(rrn, ss, mc, bb, packetId);
                        return;
                    }
                }
            }
        } catch (IOException e) {
            sc.recycle(bb);
            throw e;
        }
    }

    /**
     * 下一个数据接收任务
     */
    private void handleNext(final RouteResultsetNode rrn, final BlockingSession ss, final BlockingMySQLChannel mc,
                            final ByteBuffer bb, final byte id) {
        final ServerConnection sc = ss.getSource();
        sc.getProcessor().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    handleRowData(rrn, ss, mc, bb, id);
                } catch (IOException e) {
                    LOGGER.warn(new StringBuilder().append(sc).append(rrn).toString(), e);
                    mc.close();
                    String msg = e.getMessage();
                    handleError(ErrorCode.ER_YES, msg == null ? e.getClass().getSimpleName() : msg, ss);
                } catch (RuntimeException e) {
                    LOGGER.warn(new StringBuilder().append(sc).append(rrn).toString(), e);
                    mc.close();
                    String msg = e.getMessage();
                    handleError(ErrorCode.ER_YES, msg == null ? e.getClass().getSimpleName() : msg, ss);
                }
            }
        });
    }

    /**
     * 执行异常处理
     */
    private void handleError(int errno, String message, BlockingSession ss) {
        endRunning();

        // 清理
        ss.clear();

        ServerConnection sc = ss.getSource();
        sc.setTxInterrupt();

        // 通知
        ErrorPacket err = new ErrorPacket();
        err.packetId = ++packetId;// ERROR_PACKET
        err.errno = errno;
        err.message = StringUtil.encode(message, sc.getCharset());
        err.write(sc);
    }

    private void endRunning() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            isRunning = false;
            taskFinished.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private void setLastInsertId(BinaryPacket bin, ServerConnection sc) {
        OkPacket ok = new OkPacket();
        ok.read(bin);
        if (ok.insertId > 0) {
            sc.setLastInsertId(ok.insertId);
        }
    }

}
