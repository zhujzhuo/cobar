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
package com.alibaba.cobar.server.net.packet;

import java.nio.ByteBuffer;

import com.alibaba.cobar.server.net.FrontendConnection;
import com.alibaba.cobar.server.net.protocol.MySQLMessage;
import com.alibaba.cobar.server.util.ByteBufferUtil;

/**
 * From server to client in response to command, if error.
 * 
 * <pre>
 * Bytes                       Name
 * -----                       ----
 * 1                           field_count, always = 0xff
 * 2                           errno
 * 1                           (sqlstate marker), always '#'
 * 5                           sqlstate (5 characters)
 * n                           message
 * 
 * @see http://forge.mysql.com/wiki/MySQL_Internals_ClientServer_Protocol#Error_Packet
 * </pre>
 * 
 * @author xianmao.hexm 2010-7-16 上午10:45:01
 */
public class ErrorPacket extends AbstractPacket {

    public static final byte FIELD_COUNT = (byte) 0xff;
    private static final byte SQLSTATE_MARKER = (byte) '#';
    private static final byte[] DEFAULT_SQLSTATE = "HY000".getBytes();

    public byte fieldCount = FIELD_COUNT;
    public int errno;
    public byte mark = SQLSTATE_MARKER;
    public byte[] sqlState = DEFAULT_SQLSTATE;
    public byte[] message;

    public void read(BinaryPacket bin) {
        packetLength = bin.packetLength;
        packetId = bin.packetId;
        MySQLMessage mm = new MySQLMessage(bin.data);
        fieldCount = mm.read();
        errno = mm.readUB2();
        if (mm.hasRemaining() && (mm.read(mm.position()) == SQLSTATE_MARKER)) {
            mm.read();
            sqlState = mm.readBytes(5);
        }
        message = mm.readBytes();
    }

    public void read(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        packetLength = mm.readUB3();
        packetId = mm.read();
        fieldCount = mm.read();
        errno = mm.readUB2();
        if (mm.hasRemaining() && (mm.read(mm.position()) == SQLSTATE_MARKER)) {
            mm.read();
            sqlState = mm.readBytes(5);
        }
        message = mm.readBytes();
    }

    @Override
    public ByteBuffer write(ByteBuffer buffer, FrontendConnection c) {
        packetLength = calcPacketLength();
        int headerSize = c.getProtocol().getPacketHeaderSize();
        buffer = ByteBufferUtil.check(buffer, headerSize + packetLength, c);
        ByteBufferUtil.writeUB3(buffer, packetLength);
        buffer.put(packetId);
        buffer.put(fieldCount);
        ByteBufferUtil.writeUB2(buffer, errno);
        buffer.put(mark);
        buffer.put(sqlState);
        if (message != null) {
            buffer = ByteBufferUtil.write(message, buffer, c);
        }
        return buffer;
    }

    public void write(FrontendConnection c) {
        packetLength = calcPacketLength();
        ByteBuffer buffer = c.allocate();
        ByteBufferUtil.writeUB3(buffer, packetLength);
        buffer.put(packetId);
        buffer.put(fieldCount);
        ByteBufferUtil.writeUB2(buffer, errno);
        buffer.put(mark);
        buffer.put(sqlState);
        if (message != null) {
            buffer = ByteBufferUtil.write(message, buffer, c);
        }
        c.write(buffer);
    }

    @Override
    public int calcPacketLength() {
        int size = 9;// 1 + 2 + 1 + 5
        if (message != null) {
            size += message.length;
        }
        return size;
    }

    @Override
    protected String getPacketInfo() {
        return "Error Packet";
    }

}
