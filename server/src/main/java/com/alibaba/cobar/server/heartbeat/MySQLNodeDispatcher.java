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

import java.util.ArrayList;
import java.util.List;

import com.alibaba.cobar.server.net.nio.NIOHandler;
import com.alibaba.cobar.server.net.packet.EOFPacket;
import com.alibaba.cobar.server.net.packet.ErrorPacket;
import com.alibaba.cobar.server.net.packet.OkPacket;
import com.alibaba.cobar.server.util.BytesUtil;

/**
 * @author xianmao.hexm
 */
public class MySQLNodeDispatcher implements NIOHandler {

    private static final int RESULT_STATUS_INIT = 0;
    private static final int RESULT_STATUS_HEADER = 1;
    private static final int RESULT_STATUS_FIELD_EOF = 2;

    private MySQLNodeConnection source;
    private volatile int resultStatus;
    private volatile byte[] header;
    private volatile List<byte[]> fields;

    public MySQLNodeDispatcher(MySQLNodeConnection source) {
        this.source = source;
        this.resultStatus = RESULT_STATUS_INIT;
    }

    public MySQLNodeConnection getSource() {
        return source;
    }

    @Override
    public void handle(byte[] data) {
        switch (resultStatus) {
        case RESULT_STATUS_INIT:
            switch (data[4]) {
            case OkPacket.FIELD_COUNT:
                source.okPacket(data);
                break;
            case ErrorPacket.FIELD_COUNT:
                source.errorPacket(data);
                break;
            default:
                resultStatus = RESULT_STATUS_HEADER;
                header = data;
                fields = new ArrayList<byte[]>((int) BytesUtil.readLength(data, 4));
            }
            break;
        case RESULT_STATUS_HEADER:
            switch (data[4]) {
            case ErrorPacket.FIELD_COUNT:
                resultStatus = RESULT_STATUS_INIT;
                source.errorPacket(data);
                break;
            case EOFPacket.FIELD_COUNT:
                resultStatus = RESULT_STATUS_FIELD_EOF;
                source.fieldEofPacket(header, fields, data);
                break;
            default:
                fields.add(data);
            }
            break;
        case RESULT_STATUS_FIELD_EOF:
            switch (data[4]) {
            case ErrorPacket.FIELD_COUNT:
                resultStatus = RESULT_STATUS_INIT;
                source.errorPacket(data);
                break;
            case EOFPacket.FIELD_COUNT:
                resultStatus = RESULT_STATUS_INIT;
                source.rowEofPacket(data);
                break;
            default:
                source.rowDataPacket(data);
            }
            break;
        default:
            throw new RuntimeException("Unknown packet!");
        }
    }

}
