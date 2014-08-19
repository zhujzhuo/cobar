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
package com.alibaba.cobar.net;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.cobar.defs.ErrorCode;
import com.alibaba.cobar.net.nio.NIOConnection;
import com.alibaba.cobar.net.nio.NIOHandler;
import com.alibaba.cobar.net.nio.NIOProcessor;
import com.alibaba.cobar.net.protocol.MySQLProtocol;
import com.alibaba.cobar.statistics.ConnectionStatistic;
import com.alibaba.cobar.util.ByteBufferQueue;
import com.alibaba.cobar.util.TimeUtil;

/**
 * @author xianmao.hexm
 */
public abstract class AbstractConnection implements NIOConnection {

    private static final int OP_NOT_READ = ~SelectionKey.OP_READ;
    private static final int OP_NOT_WRITE = ~SelectionKey.OP_WRITE;

    protected long id;
    protected String host;
    protected int port;
    protected int localPort;
    protected long idleTimeout;
    protected final SocketChannel channel;
    protected NIOProcessor processor;
    protected SelectionKey processKey;
    protected final ReentrantLock keyLock;
    protected ByteBuffer readBuffer;
    protected final MySQLProtocol protocol;
    protected NIOHandler handler;
    protected ByteBufferQueue writeQueue;
    protected final ReentrantLock writeLock;
    protected boolean isRegistered;
    protected final AtomicBoolean isClosed;
    protected boolean isSocketClosed;
    protected final ConnectionStatistic statistic;

    public AbstractConnection(SocketChannel channel) {
        this.channel = channel;
        this.keyLock = new ReentrantLock();
        this.protocol = new MySQLProtocol(this);
        this.writeLock = new ReentrantLock();
        this.isClosed = new AtomicBoolean(false);
        this.statistic = new ConnectionStatistic();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public ByteBuffer getReadBuffer() {
        return readBuffer;
    }

    public void setReadBuffer(ByteBuffer readBuffer) {
        this.readBuffer = readBuffer;
    }

    public ByteBufferQueue getWriteQueue() {
        return writeQueue;
    }

    public void setWriteQueue(ByteBufferQueue writeQueue) {
        this.writeQueue = writeQueue;
    }

    public NIOProcessor getProcessor() {
        return processor;
    }

    public MySQLProtocol getProtocol() {
        return protocol;
    }

    public ConnectionStatistic getStatistic() {
        return statistic;
    }

    public ByteBuffer allocate() {
        return processor.getBufferPool().allocate();
    }

    public void recycle(ByteBuffer buffer) {
        processor.getBufferPool().recycle(buffer);
    }

    public void setHandler(NIOHandler handler) {
        this.handler = handler;
    }

    @Override
    public void register(Selector selector) throws IOException {
        try {
            processKey = channel.register(selector, SelectionKey.OP_READ, this);
            isRegistered = true;
        } finally {
            if (isClosed.get()) {
                clearSelectionKey();
            }
        }
    }

    @Override
    public void read() throws IOException {
        // 从channel读取数据到buffer
        ByteBuffer buffer = this.readBuffer;
        int got = channel.read(buffer);
        statistic.setLastReadTime(TimeUtil.currentTimeMillis());
        if (got < 0) {
            throw new EOFException();
        }
        statistic.addNetInBytes(got);
        processor.getStatistic().addNetInBytes(got);

        // 对读取的buffer数据做协议层的处理和转换
        protocol.handle(buffer);
    }

    @Override
    public void handle(final byte[] data) {
        processor.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    handler.handle(data);
                } catch (Throwable t) {
                    error(ErrorCode.ERR_HANDLE_DATA, t);
                }
            }
        });
    }

    @Override
    public void write(ByteBuffer buffer) {
        if (isClosed.get()) {
            processor.getBufferPool().recycle(buffer);
            return;
        }
        if (isRegistered) {
            try {
                writeQueue.put(buffer);
            } catch (InterruptedException e) {
                error(ErrorCode.ERR_PUT_WRITE_QUEUE, e);
                return;
            }
            processor.postWrite(this);
        } else {
            processor.getBufferPool().recycle(buffer);
            close();
        }
    }

    @Override
    public void writeByQueue() throws IOException {
        if (isClosed.get()) {
            return;
        }
        final ReentrantLock lock = this.writeLock;
        lock.lock();
        try {
            // 满足以下两个条件时，切换到基于事件的写操作。
            // 1.当前key对写事件不该兴趣。
            // 2.write0()返回false。
            if ((processKey.interestOps() & SelectionKey.OP_WRITE) == 0 && !write0()) {
                enableWrite();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void writeByEvent() throws IOException {
        if (isClosed.get()) {
            return;
        }
        final ReentrantLock lock = this.writeLock;
        lock.lock();
        try {
            // 满足以下两个条件时，切换到基于队列的写操作。
            // 1.write0()返回true。
            // 2.发送队列的buffer为空。
            if (write0() && writeQueue.size() == 0) {
                disableWrite();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean close() {
        if (isClosed.get()) {
            return false;
        } else {
            if (closeSocket()) {
                boolean status = isClosed.compareAndSet(false, true);
                if (status) {
                    // post closed event
                }
                return status;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean isClosed() {
        return isClosed.get();
    }

    /**
     * 在连接为关闭状态时回收这些资源
     */
    public void recycle() {
        ByteBuffer buffer = null;

        // 回收读缓存
        buffer = this.readBuffer;
        if (buffer != null) {
            this.readBuffer = null;
            this.recycle(buffer);
        }

        // 回收写缓存
        while ((buffer = writeQueue.poll()) != null) {
            this.recycle(buffer);
        }
    }

    /**
     * 打开读事件
     */
    public void enableRead() {
        final Lock lock = this.keyLock;
        lock.lock();
        try {
            SelectionKey key = this.processKey;
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
        } finally {
            lock.unlock();
        }
        processKey.selector().wakeup();
    }

    /**
     * 关闭读事件
     */
    public void disableRead() {
        final Lock lock = this.keyLock;
        lock.lock();
        try {
            SelectionKey key = this.processKey;
            key.interestOps(key.interestOps() & OP_NOT_READ);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 打开写事件
     */
    private void enableWrite() {
        final Lock lock = this.keyLock;
        lock.lock();
        try {
            SelectionKey key = this.processKey;
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        } finally {
            lock.unlock();
        }
        processKey.selector().wakeup();
    }

    /**
     * 关闭写事件
     */
    private void disableWrite() {
        final Lock lock = this.keyLock;
        lock.lock();
        try {
            SelectionKey key = this.processKey;
            key.interestOps(key.interestOps() & OP_NOT_WRITE);
        } finally {
            lock.unlock();
        }
    }

    private boolean write0() throws IOException {
        // 检查是否有遗留数据未写出
        ByteBuffer buffer = writeQueue.attachment();
        if (buffer != null) {
            int written = channel.write(buffer);
            if (written > 0) {
                statistic.addNetOutBytes(written);
                processor.getStatistic().addNetOutBytes(written);
            }
            statistic.setLastWriteTime(TimeUtil.currentTimeMillis());
            if (buffer.hasRemaining()) {
                statistic.writeAttemptsPlus();
                return false;
            } else {
                writeQueue.attach(null);
                processor.getBufferPool().recycle(buffer);
            }
        }
        // 写出发送队列中的数据块
        if ((buffer = writeQueue.poll()) != null) {
            // 如果是一块未使用过的buffer，则执行关闭连接。
            if (buffer.position() == 0) {
                processor.getBufferPool().recycle(buffer);
                close();
                return true;
            }
            buffer.flip();
            int written = channel.write(buffer);
            if (written > 0) {
                statistic.addNetOutBytes(written);
                processor.getStatistic().addNetOutBytes(written);
            }
            statistic.setLastWriteTime(TimeUtil.currentTimeMillis());
            if (buffer.hasRemaining()) {
                writeQueue.attach(buffer);
                statistic.writeAttemptsPlus();
                return false;
            } else {
                processor.getBufferPool().recycle(buffer);
            }
        }
        return true;
    }

    private void clearSelectionKey() {
        final Lock lock = this.keyLock;
        lock.lock();
        try {
            SelectionKey key = this.processKey;
            if (key != null && key.isValid()) {
                key.attach(null);
                key.cancel();
            }
        } finally {
            lock.unlock();
        }
    }

    private boolean closeSocket() {
        clearSelectionKey();
        SocketChannel channel = this.channel;
        if (channel != null) {
            boolean isSocketClosed = true;
            Socket socket = channel.socket();
            if (socket != null) {
                try {
                    socket.close();
                } catch (Throwable e) {
                }
                isSocketClosed = socket.isClosed();
            }
            try {
                channel.close();
            } catch (Throwable e) {
            }
            return isSocketClosed && (!channel.isOpen());
        } else {
            return true;
        }
    }

}
