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
package com.alibaba.cobar.server.backend;

import java.util.List;

/**
 * @author xianmao.hexm
 */
public class MySQLResponseHandlerProxy implements MySQLResponseHandler {

    private MySQLResponseHandler handler;

    public MySQLResponseHandlerProxy(MySQLResponseHandler handler) {
        this.handler = handler;
    }

    public void setConnection(MySQLConnection c) {
        handler.setConnection(c);
    }

    public void error(int code, Throwable t) {
        handler.error(code, t);
    }

    public void connectionAquired() {
        handler.connectionAquired();
    }

    public void okPacket(byte[] data) {
        handler.okPacket(data);
    }

    public void errorPacket(byte[] data) {
        handler.errorPacket(data);
    }

    public void fieldEofPacket(byte[] header, List<byte[]> fields, byte[] data) {
        handler.fieldEofPacket(header, fields, data);
    }

    public void rowDataPacket(byte[] data) {
        handler.rowDataPacket(data);
    }

    public void rowEofPacket(byte[] data) {
        handler.rowEofPacket(data);
    }

}
