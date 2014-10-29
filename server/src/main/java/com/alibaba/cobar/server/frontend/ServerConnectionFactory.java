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
package com.alibaba.cobar.server.frontend;

import java.nio.channels.SocketChannel;

import com.alibaba.cobar.server.defs.Capabilities;
import com.alibaba.cobar.server.frontend.handler.ServerPrepareHandler;
import com.alibaba.cobar.server.model.Server;
import com.alibaba.cobar.server.net.FrontendConnection;
import com.alibaba.cobar.server.net.factory.FrontendConnectionFactory;
import com.alibaba.cobar.server.session.ServerSession;
import com.alibaba.cobar.server.startup.CobarContainer;

/**
 * @author xianmao.hexm
 */
public class ServerConnectionFactory extends FrontendConnectionFactory {

    @Override
    protected FrontendConnection getConnection(SocketChannel channel) {
        Server sc = CobarContainer.getInstance().getConfigModel().getServer();
        ServerConnection c = new ServerConnection(channel);
        c.setServerCapabilities(getServerCapabilities());
        c.setTxIsolation(sc.getTxIsolation());
        c.setHandler(new ServerAuthenticator(c));
        c.setPrivileges(new ServerPrivileges());
        c.setQueryHandler(new ServerQueryHandler(c));
        c.setPrepareHandler(new ServerPrepareHandler(c));
        c.setSession(new ServerSession(c));
        return c;
    }

    protected int getServerCapabilities() {
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
        // flag |= ServerDefs.CLIENT_RESERVED;
        flag |= Capabilities.CLIENT_SECURE_CONNECTION;
        return flag;
    }

}
