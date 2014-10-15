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
package com.alibaba.cobar.server.frontend.manager;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

import com.alibaba.cobar.server.core.defs.ErrorCode;
import com.alibaba.cobar.server.core.net.nio.NIOHandler;
import com.alibaba.cobar.server.core.net.packet.AbstractPacket;
import com.alibaba.cobar.server.core.net.packet.OkPacket;
import com.alibaba.cobar.server.core.net.protocol.MySQLMessage;
import com.alibaba.cobar.server.frontend.manager.handler.ClearHandler;
import com.alibaba.cobar.server.frontend.manager.handler.ReloadHandler;
import com.alibaba.cobar.server.frontend.manager.handler.RollbackHandler;
import com.alibaba.cobar.server.frontend.manager.handler.SelectHandler;
import com.alibaba.cobar.server.frontend.manager.handler.ShowHandler;
import com.alibaba.cobar.server.frontend.manager.handler.StopHandler;
import com.alibaba.cobar.server.frontend.manager.handler.SwitchHandler;
import com.alibaba.cobar.server.frontend.manager.parser.ManagerParse;
import com.alibaba.cobar.server.frontend.manager.response.Heartbeat;
import com.alibaba.cobar.server.frontend.manager.response.KillConnection;
import com.alibaba.cobar.server.frontend.manager.response.Offline;
import com.alibaba.cobar.server.frontend.manager.response.Online;
import com.alibaba.cobar.server.util.ByteBufferUtil;

/**
 * @author xianmao.hexm
 */
public class ManagerDispatcher implements NIOHandler {

    private static final Logger LOGGER = Logger.getLogger(ManagerDispatcher.class);

    private final ManagerConnection source;

    public ManagerDispatcher(ManagerConnection source) {
        this.source = source;
    }

    @Override
    public void handle(byte[] data) {
        switch (data[4]) {
        case AbstractPacket.COM_QUERY:
            handleQuery(data);
            break;
        case AbstractPacket.COM_HEARTBEAT:
            Heartbeat.response(source, data);
            break;
        case AbstractPacket.COM_PING:
        case AbstractPacket.COM_INIT_DB:
            ByteBufferUtil.write(OkPacket.OK, source);
            break;
        case AbstractPacket.COM_QUIT:
            source.close();
            break;
        default:
            source.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unsupported command");
        }
    }

    protected void handleQuery(byte[] data) {
        ManagerConnection c = this.source;
        String charset = c.getCharset();

        // 获取query语句
        MySQLMessage mm = new MySQLMessage(data);
        mm.position(5);
        String sql = null;
        try {
            sql = mm.readString(charset);
        } catch (UnsupportedEncodingException e) {
            c.writeErrMessage(ErrorCode.ER_UNKNOWN_CHARACTER_SET, "Unknown charset '" + charset + "'");
            return;
        }
        if (sql == null || sql.length() == 0) {
            c.writeErrMessage(ErrorCode.ER_NOT_ALLOWED_COMMAND, "Empty SQL");
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(new StringBuilder().append(c).append(sql).toString());
        }
        int rs = ManagerParse.parse(sql);
        switch (rs & 0xff) {
        case ManagerParse.SELECT:
            SelectHandler.handle(sql, c, rs >>> 8);
            break;
        case ManagerParse.SET:
            ByteBufferUtil.write(OkPacket.OK, c);
            break;
        case ManagerParse.SHOW:
            ShowHandler.handle(sql, c, rs >>> 8);
            break;
        case ManagerParse.SWITCH:
            SwitchHandler.handler(sql, c, rs >>> 8);
            break;
        case ManagerParse.KILL_CONN:
            KillConnection.response(sql, rs >>> 8, c);
            break;
        case ManagerParse.OFFLINE:
            Offline.execute(sql, c);
            break;
        case ManagerParse.ONLINE:
            Online.execute(sql, c);
            break;
        case ManagerParse.STOP:
            StopHandler.handle(sql, c, rs >>> 8);
            break;
        case ManagerParse.RELOAD:
            ReloadHandler.handle(sql, c, rs >>> 8);
            break;
        case ManagerParse.ROLLBACK:
            RollbackHandler.handle(sql, c, rs >>> 8);
            break;
        case ManagerParse.CLEAR:
            ClearHandler.handle(sql, c, rs >>> 8);
            break;
        default:
            c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
        }
    }

}
