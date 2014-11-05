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

import com.alibaba.cobar.server.net.ResponseHandler;

/**
 * @author xianmao.hexm
 */
public abstract class MySQLResponseHandler implements ResponseHandler {

    protected MySQLConnection connection;

    public void setConnection(MySQLConnection connection) {
        this.connection = connection;
    }

    public void connectionAquired() {
    }

    public void okPacket(byte[] data) {
    }

    public void errorPacket(byte[] data) {
    }

    public void fieldEofPacket(byte[] header, List<byte[]> fields, byte[] data) {
    }

    public void rowDataPacket(byte[] data) {
    }

    public void rowEofPacket(byte[] data) {
    }

    public void error(int code, Throwable t) {
        connection.close();
    }

}
