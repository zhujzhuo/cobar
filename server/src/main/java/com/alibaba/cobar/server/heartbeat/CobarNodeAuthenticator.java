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

import com.alibaba.cobar.server.net.nio.NIOHandler;
import com.alibaba.cobar.server.net.packet.AuthPacket;
import com.alibaba.cobar.server.net.packet.ErrorPacket;
import com.alibaba.cobar.server.net.packet.HandshakePacket;
import com.alibaba.cobar.server.net.packet.OkPacket;

/**
 * @author xianmao.hexm
 */
public class CobarNodeAuthenticator implements NIOHandler {

    private CobarNodeConnection source;
    private HandshakePacket handshake;

    public CobarNodeAuthenticator(CobarNodeConnection source) {
        this.source = source;
    }

    @Override
    public void handle(byte[] data) {
        HandshakePacket hsp = this.handshake;
        if (hsp == null) {
            // 设置握手数据包
            hsp = new HandshakePacket();
            hsp.read(data);
            this.handshake = hsp;

            // 发送认证数据包
            AuthPacket ap = new AuthPacket();
            ap.packetId = 1;
            ap.clientFlags = source.getClientFlags();
            ap.maxPacketSize = source.getProtocol().getMaxPacketSize();
            ap.charsetIndex = (hsp.serverCharsetIndex & 0xff);
            ap.write(source);
        } else { // 处理认证结果
            switch (data[4]) {
            case OkPacket.FIELD_COUNT:
                source.setHandler(new CobarNodeDispatcher(source));
                source.connectionAquired();
                break;
            case ErrorPacket.FIELD_COUNT:
                ErrorPacket err = new ErrorPacket();
                err.read(data);
                throw new RuntimeException(new String(err.message));
            default:
                throw new RuntimeException("Unknown packet");
            }
        }
    }

}
