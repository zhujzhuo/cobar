/*
 * Copyright 1999-2014 Alibaba Group.
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
package com.alibaba.cobar.server.core.net.protocol;

import java.nio.ByteBuffer;

import com.alibaba.cobar.server.core.net.AbstractConnection;

/**
 * @author xianmao.hexm
 */
public class MySQLProtocol {

    private final AbstractConnection c;
    private int packetHeaderSize;
    private int maxPacketSize;
    private int bufferOffset;

    public MySQLProtocol(AbstractConnection c) {
        this.c = c;
        this.packetHeaderSize = 4;
        this.maxPacketSize = 16 * 1024 * 1024;
    }

    public int getPacketHeaderSize() {
        return packetHeaderSize;
    }

    public int getMaxPacketSize() {
        return maxPacketSize;
    }

    public void handle(ByteBuffer buffer) {
        int offset = this.bufferOffset, size = 0, position = buffer.position();
        for (;;) {
            size = getPacketSize(buffer, offset);
            if (size == -1) {// 未达到可计算数据包长度的数据
                if (!buffer.hasRemaining()) {
                    buffer = checkBuffer(buffer, offset, position);
                }
                break;
            }

            if (position >= offset + size) { // 至少有一个数据包的数据在buffer中
                // 从buffer中复制一个数据包的数据，然后提交处理。
                byte[] data = new byte[size];
                buffer.position(offset);
                buffer.get(data, 0, size);
                c.handle(data);

                // 设置偏移量
                offset += size;
                if (position == offset) {// 数据正好全部处理完毕
                    if (this.bufferOffset != 0) {
                        this.bufferOffset = 0;
                    }
                    buffer.clear();
                    break;
                } else {// 还有剩余数据未处理
                    this.bufferOffset = offset;
                    buffer.position(position);
                    continue;
                }
            } else {// 未到达一个数据包的数据
                if (!buffer.hasRemaining()) {
                    buffer = checkBuffer(buffer, offset, position);
                }
                break;
            }
        }
    }

    /**
     * 获取数据包大小
     */
    private int getPacketSize(ByteBuffer buffer, int offset) {
        if (buffer.position() < offset + packetHeaderSize) {
            return -1;
        } else {
            int length = buffer.get(offset) & 0xff;
            length |= (buffer.get(++offset) & 0xff) << 8;
            length |= (buffer.get(++offset) & 0xff) << 16;
            return length + packetHeaderSize;
        }
    }

    /**
     * 检查Buffer容量，不够则扩展当前缓存，直到最大值。
     */
    private ByteBuffer checkBuffer(ByteBuffer buffer, int offset, int position) {
        // 当偏移量为0时需要扩容，否则移动数据至偏移量为0的位置。
        if (offset == 0) {
            if (buffer.capacity() >= maxPacketSize) {
                throw new IllegalArgumentException("Packet size over the limit.");
            }
            int size = buffer.capacity() << 1;
            size = (size > maxPacketSize) ? maxPacketSize : size;
            ByteBuffer newBuffer = ByteBuffer.allocate(size);
            buffer.position(offset);
            newBuffer.put(buffer);
            // 赋值新的buffer并回收扩容前的缓存块
            c.setReadBuffer(newBuffer);
            c.getProcessor().getBufferPool().recycle(buffer);
            return newBuffer;
        } else {
            buffer.position(offset);
            buffer.compact();
            this.bufferOffset = 0;
            return buffer;
        }
    }

}
