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
package com.alibaba.cobar.server.backend;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.alibaba.cobar.server.defs.Capabilities;
import com.alibaba.cobar.server.model.DataSources.DataSource;
import com.alibaba.cobar.server.net.factory.BackendConnectionFactory;
import com.alibaba.cobar.server.startup.CobarContainer;
import com.alibaba.cobar.server.util.BufferQueue;
import com.alibaba.cobar.server.util.TimeUtil;

/**
 * @author xianmao.hexm 2012-4-12
 */
public class MySQLConnectionFactory extends BackendConnectionFactory {

    public MySQLConnection make(DataSource dataSource, MySQLResponseHandler responseHandler) throws IOException {
        MySQLConnection c = getConnection(dataSource, responseHandler);
        CobarContainer.getInstance().getConnector().postConnect(c);
        return c;
    }

    public MySQLConnection make(MySQLConnectionPool pool, MySQLResponseHandler responseHandler) throws IOException {
        MySQLConnection c = getConnection(pool.getDataSource(), responseHandler);
        c.setPool(pool);
        CobarContainer.getInstance().getConnector().postConnect(c);
        return c;
    }

    protected MySQLConnection getConnection(DataSource dataSource, MySQLResponseHandler responseHandler)
            throws IOException {
        MySQLConnection c = new MySQLConnection(getChannel());
        c.setClientFlags(getClientFlags());
        c.setHandler(new MySQLAuthenticator(c));
        c.setWriteQueue(new BufferQueue<ByteBuffer>(writeQueueCapacity));
        c.setIdleTimeout(idleTimeout);
        c.setDataSource(dataSource);
        c.setResponseHandler(responseHandler);
        c.setLastTime(TimeUtil.currentTimeMillis());
        c.setInThePool(false);
        return c;
    }

    protected long getClientFlags() {
        int flag = 0;
        flag |= Capabilities.CLIENT_LONG_PASSWORD;
        flag |= Capabilities.CLIENT_FOUND_ROWS;
        flag |= Capabilities.CLIENT_LONG_FLAG;
        flag |= Capabilities.CLIENT_CONNECT_WITH_DB;
        // flag |= Capabilities.CLIENT_NO_SCHEMA;
        // flag |= Capabilities.CLIENT_COMPRESS;
        flag |= Capabilities.CLIENT_ODBC;
        // flag |= Capabilities.CLIENT_LOCAL_FILES;
        flag |= Capabilities.CLIENT_IGNORE_SPACE;
        flag |= Capabilities.CLIENT_PROTOCOL_41;
        flag |= Capabilities.CLIENT_INTERACTIVE;
        // flag |= Capabilities.CLIENT_SSL;
        flag |= Capabilities.CLIENT_IGNORE_SIGPIPE;
        flag |= Capabilities.CLIENT_TRANSACTIONS;
        // flag |= Capabilities.CLIENT_RESERVED;
        flag |= Capabilities.CLIENT_SECURE_CONNECTION;
        // client extension
        // flag |= Capabilities.CLIENT_MULTI_STATEMENTS;
        // flag |= Capabilities.CLIENT_MULTI_RESULTS;
        return flag;
    }

}
