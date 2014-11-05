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
import java.net.Socket;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import com.alibaba.cobar.server.defs.Versions;
import com.alibaba.cobar.server.net.nio.NIOProcessor;
import com.alibaba.cobar.server.net.packet.ErrorPacket;
import com.alibaba.cobar.server.net.packet.HandshakePacket;
import com.alibaba.cobar.server.util.CharsetUtil;
import com.alibaba.cobar.server.util.RandomUtil;
import com.alibaba.cobar.server.util.StringUtil;

/**
 * @author xianmao.hexm
 */
public abstract class FrontendConnection extends AbstractConnection {

    protected static final long AUTH_TIMEOUT = 15 * 1000L;

    protected String charset;
    protected byte[] seed;
    protected int serverCapabilities;
    protected boolean isAuthenticated;
    protected String schema;

    public FrontendConnection(SocketChannel channel) {
        super(channel);
        Socket socket = channel.socket();
        this.host = socket.getInetAddress().getHostAddress();
        this.port = socket.getPort();
        this.localHost = socket.getLocalAddress().getHostAddress();
        this.localPort = socket.getLocalPort();
    }

    public void setProcessor(NIOProcessor processor) {
        this.processor = processor;
        this.readBuffer = processor.getBufferPool().allocate();
        processor.addFrontend(this);
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public byte[] getSeed() {
        return seed;
    }

    public void setServerCapabilities(int serverCapabilities) {
        this.serverCapabilities = serverCapabilities;
    }

    public void setAuthenticated(boolean isAuthenticated) {
        this.isAuthenticated = isAuthenticated;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public void register(Selector selector) throws IOException {
        super.register(selector);
        if (!isClosed.get()) {
            // 生成认证数据
            byte[] rand1 = RandomUtil.randomBytes(8);
            byte[] rand2 = RandomUtil.randomBytes(12);

            // 保存认证数据
            byte[] seed = new byte[rand1.length + rand2.length];
            System.arraycopy(rand1, 0, seed, 0, rand1.length);
            System.arraycopy(rand2, 0, seed, rand1.length, rand2.length);
            this.seed = seed;

            // 发送握手数据包
            HandshakePacket hs = new HandshakePacket();
            hs.packetId = 0;
            hs.protocolVersion = Versions.PROTOCOL_VERSION;
            hs.serverVersion = Versions.SERVER_VERSION;
            hs.threadId = id;
            hs.seed = rand1;
            hs.serverCapabilities = serverCapabilities;
            hs.serverCharsetIndex = (byte) (CharsetUtil.getIndex(charset) & 0xff);
            hs.serverStatus = 2;
            hs.restOfScrambleBuff = rand2;
            hs.write(this);
        }
    }

    @Override
    public void idleCheck() {
        if (isIdleTimeout(isAuthenticated ? idleTimeout : AUTH_TIMEOUT)) {
            close();
        }
    }

    public void writeErrMessage(int errno, String msg) {
        writeErrMessage((byte) 1, errno, msg);
    }

    public void writeErrMessage(byte id, int errno, String msg) {
        ErrorPacket err = new ErrorPacket();
        err.packetId = id;
        err.errno = errno;
        err.message = StringUtil.encode(msg, charset);
        err.write(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("[class=")
                                              .append(getClass().getSimpleName())
                                              .append(",host=")
                                              .append(host)
                                              .append(",port=")
                                              .append(port);
        if (schema != null) {
            sb.append(",schema=").append(schema);
        }
        sb.append(']');
        return sb.toString();
    }

}
