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
package com.alibaba.cobar.backend.mysql;

import java.security.NoSuchAlgorithmException;

import com.alibaba.cobar.backend.mysql.callback.ResponseHandler;
import com.alibaba.cobar.model.DataSources.DataSource;
import com.alibaba.cobar.net.nio.NIOHandler;
import com.alibaba.cobar.net.packet.AuthPacket;
import com.alibaba.cobar.net.packet.EOFPacket;
import com.alibaba.cobar.net.packet.ErrorPacket;
import com.alibaba.cobar.net.packet.HandshakePacket;
import com.alibaba.cobar.net.packet.OkPacket;
import com.alibaba.cobar.net.packet.Reply323Packet;
import com.alibaba.cobar.util.CharsetUtil;
import com.alibaba.cobar.util.SecurityUtil;

/**
 * MySQL 验证处理器
 * 
 * @author xianmao.hexm
 */
public class MySQLAuthenticator implements NIOHandler {

    private MySQLConnection source;
    private HandshakePacket handshake;
    private ResponseHandler callback;

    public MySQLAuthenticator(MySQLConnection source) {
        this.source = source;
    }

    public ResponseHandler getCallback() {
        return callback;
    }

    public void setCallback(ResponseHandler callback) {
        this.callback = callback;
    }

    @Override
    public void handle(byte[] data) {
        try {
            HandshakePacket hsp = this.handshake;
            if (hsp == null) {
                // 设置握手数据包
                hsp = new HandshakePacket();
                hsp.read(data);
                this.handshake = hsp;

                // 设置连接属性
                source.setThreadId(hsp.threadId);
                int charsetIndex = (hsp.serverCharsetIndex & 0xff);
                String charset = CharsetUtil.getCharset(charsetIndex);
                if (charset != null) {
                    source.setCharset(charset);
                } else {
                    throw new RuntimeException("Unknown charsetIndex:" + charsetIndex);
                }

                // 发送认证数据包
                DataSource ds = source.getDataSource();
                AuthPacket ap = new AuthPacket();
                ap.packetId = 1;
                ap.clientFlags = source.getClientFlags();
                ap.maxPacketSize = source.getProtocol().getMaxPacketSize();
                ap.charsetIndex = charsetIndex;
                ap.user = ds.getUser();
                try {
                    ap.password = passwd(ds.getPassword(), hsp);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e.getMessage());
                }
                ap.database = ds.getSchema();
                ap.write(source);
            } else { // 处理认证结果
                switch (data[4]) {
                case OkPacket.FIELD_COUNT:
                    source.setAuthenticated(true);
                    source.setHandler(new MySQLDispatcher(source));
                    if (callback != null) {
                        callback.connectionAcquired(source);
                    }
                    break;
                case ErrorPacket.FIELD_COUNT:
                    ErrorPacket err = new ErrorPacket();
                    err.read(data);
                    throw new RuntimeException(new String(err.message));
                case EOFPacket.FIELD_COUNT:
                    auth323(data[3]);
                    break;
                default:
                    throw new RuntimeException("Unknown packet!");
                }
            }
        } catch (RuntimeException e) {
            if (callback != null) {
                callback.connectionError(e, source);
            }
            throw e;
        }
    }

    private void auth323(byte packetId) {
        // 发送323响应认证数据包
        Reply323Packet r323 = new Reply323Packet();
        r323.packetId = ++packetId;
        String pass = source.getDataSource().getPassword();
        if (pass != null && pass.length() > 0) {
            byte[] seed = this.handshake.seed;
            r323.seed = SecurityUtil.scramble323(pass, new String(seed)).getBytes();
        }
        r323.write(source);
    }

    private static byte[] passwd(String pass, HandshakePacket hs) throws NoSuchAlgorithmException {
        if (pass == null || pass.length() == 0) {
            return null;
        }
        byte[] passwd = pass.getBytes();
        int sl1 = hs.seed.length;
        int sl2 = hs.restOfScrambleBuff.length;
        byte[] seed = new byte[sl1 + sl2];
        System.arraycopy(hs.seed, 0, seed, 0, sl1);
        System.arraycopy(hs.restOfScrambleBuff, 0, seed, sl1, sl2);
        return SecurityUtil.scramble411(passwd, seed);
    }

}
