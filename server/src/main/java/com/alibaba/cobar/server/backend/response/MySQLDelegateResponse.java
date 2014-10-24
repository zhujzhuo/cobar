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
/**
 * (created at 2012-4-19)
 */
package com.alibaba.cobar.server.backend.response;

import java.util.List;

import com.alibaba.cobar.server.backend.MySQLConnection;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class MySQLDelegateResponse implements MySQLResponse {

    private final MySQLResponse target;

    public MySQLDelegateResponse(MySQLResponse target) {
        if (target == null) {
            throw new IllegalArgumentException("delegate is null!");
        }
        this.target = target;
    }

    @Override
    public void connectionAcquired(MySQLConnection c) {
        target.connectionAcquired(c);
    }

    @Override
    public void connectionError(Throwable e, MySQLConnection c) {
        target.connectionError(e, c);
    }

    @Override
    public void ok(byte[] ok, MySQLConnection c) {
        target.ok(ok, c);
    }

    @Override
    public void error(byte[] err, MySQLConnection c) {
        target.error(err, c);
    }

    @Override
    public void fieldEof(byte[] header, List<byte[]> fields, byte[] eof, MySQLConnection c) {
        target.fieldEof(header, fields, eof, c);
    }

    @Override
    public void rowData(byte[] row, MySQLConnection c) {
        target.rowData(row, c);
    }

    @Override
    public void rowEof(byte[] eof, MySQLConnection c) {
        target.rowEof(eof, c);
    }

}
