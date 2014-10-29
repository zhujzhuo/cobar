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

import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.channels.SocketChannel;
import java.sql.SQLNonTransientException;
import java.util.Set;

import org.apache.log4j.Logger;

import com.alibaba.cobar.server.defs.ErrorCode;
import com.alibaba.cobar.server.frontend.handler.ServerPrepareHandler;
import com.alibaba.cobar.server.model.Schemas;
import com.alibaba.cobar.server.net.FrontendConnection;
import com.alibaba.cobar.server.net.packet.OkPacket;
import com.alibaba.cobar.server.net.protocol.MySQLMessage;
import com.alibaba.cobar.server.route.RouteResultset;
import com.alibaba.cobar.server.route.ServerRouter;
import com.alibaba.cobar.server.session.ServerSession;
import com.alibaba.cobar.server.startup.CobarContainer;
import com.alibaba.cobar.server.util.ByteBufferUtil;

/**
 * @author xianmao.hexm 2011-4-21 上午11:22:57
 */
public class ServerConnection extends FrontendConnection {

    private static final Logger LOGGER = Logger.getLogger(ServerConnection.class);

    private String user;
    private ServerPrivileges privileges;
    private ServerQueryHandler queryHandler;
    private ServerPrepareHandler prepareHandler;
    private ServerSession session;
    private volatile int txIsolation;
    private volatile boolean autocommit;
    private volatile boolean txInterrupted;
    private long lastInsertId;

    public ServerConnection(SocketChannel channel) {
        super(channel);
        this.autocommit = true;
        this.txInterrupted = false;
    }

    public void initDB(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        mm.position(5);
        String db = mm.readString();

        // 检查schema是否已经设置
        if (schema != null) {
            if (schema.equals(db)) {
                ByteBufferUtil.write(OkPacket.OK, this);
            } else {
                writeErrMessage(ErrorCode.ER_DBACCESS_DENIED_ERROR, "Not allowed to change the database!");
            }
            return;
        }

        // 检查schema的有效性
        if (db == null || !privileges.schemaExists(db)) {
            writeErrMessage(ErrorCode.ER_BAD_DB_ERROR, "Unknown database '" + db + "'");
            return;
        }
        if (!privileges.userExists(user, host)) {
            writeErrMessage(ErrorCode.ER_ACCESS_DENIED_ERROR, "Access denied for user '" + user + "'");
            return;
        }
        Set<String> schemas = privileges.getUserSchemas(user);
        if (schemas == null || schemas.size() == 0 || schemas.contains(db)) {
            this.schema = db;
            ByteBufferUtil.write(OkPacket.OK, this);
        } else {
            String s = "Access denied for user '" + user + "' to database '" + db + "'";
            writeErrMessage(ErrorCode.ER_DBACCESS_DENIED_ERROR, s);
        }
    }

    public void query(byte[] data) {
        if (queryHandler != null) {
            // 取得语句
            MySQLMessage mm = new MySQLMessage(data);
            mm.position(5);
            String sql = null;
            try {
                sql = mm.readString(charset);
            } catch (UnsupportedEncodingException e) {
                writeErrMessage(ErrorCode.ER_UNKNOWN_CHARACTER_SET, "Unknown charset '" + charset + "'");
                return;
            }
            if (sql == null || sql.length() == 0) {
                writeErrMessage(ErrorCode.ER_NOT_ALLOWED_COMMAND, "Empty SQL");
                return;
            }

            // 执行查询
            queryHandler.query(sql);
        } else {
            writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Query unsupported!");
        }
    }

    public void stmtPrepare(byte[] data) {
        if (prepareHandler != null) {
            // 取得语句
            MySQLMessage mm = new MySQLMessage(data);
            mm.position(5);
            String sql = null;
            try {
                sql = mm.readString(charset);
            } catch (UnsupportedEncodingException e) {
                writeErrMessage(ErrorCode.ER_UNKNOWN_CHARACTER_SET, "Unknown charset '" + charset + "'");
                return;
            }
            if (sql == null || sql.length() == 0) {
                writeErrMessage(ErrorCode.ER_NOT_ALLOWED_COMMAND, "Empty SQL");
                return;
            }

            // 执行预处理
            prepareHandler.prepare(sql);
        } else {
            writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Prepare unsupported!");
        }
    }

    public void stmtExecute(byte[] data) {
        if (prepareHandler != null) {
            prepareHandler.execute(data);
        } else {
            writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Prepare unsupported!");
        }
    }

    public void stmtClose(byte[] data) {
        if (prepareHandler != null) {
            prepareHandler.close();
        } else {
            writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Prepare unsupported!");
        }
    }

    public void setQueryHandler(ServerQueryHandler queryHandler) {
        this.queryHandler = queryHandler;
    }

    public void setPrepareHandler(ServerPrepareHandler prepareHandler) {
        this.prepareHandler = prepareHandler;
    }

    public ServerPrivileges getPrivileges() {
        return privileges;
    }

    public void setPrivileges(ServerPrivileges privileges) {
        this.privileges = privileges;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getTxIsolation() {
        return txIsolation;
    }

    public void setTxIsolation(int txIsolation) {
        this.txIsolation = txIsolation;
    }

    public boolean isAutocommit() {
        return autocommit;
    }

    public void setAutocommit(boolean autocommit) {
        this.autocommit = autocommit;
    }

    public long getLastInsertId() {
        return lastInsertId;
    }

    public void setLastInsertId(long lastInsertId) {
        this.lastInsertId = lastInsertId;
    }

    /**
     * 设置是否需要中断当前事务
     */
    public void setTxInterrupt() {
        if (!autocommit && !txInterrupted) {
            txInterrupted = true;
        }
    }

    public ServerSession getSession() {
        return session;
    }

    public void setSession(ServerSession session) {
        this.session = session;
    }

    public void kill(byte[] data) {
        writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unknown command");
    }

    public void execute(String sql, int type) {
        // 状态检查
        if (txInterrupted) {
            writeErrMessage(ErrorCode.ER_YES, "Transaction error, need to rollback.");
            return;
        }

        // 检查当前使用的DB
        String db = this.schema;
        if (db == null) {
            writeErrMessage(ErrorCode.ER_NO_DB_ERROR, "No database selected");
            return;
        }
        Schemas.Schema schema = CobarContainer.getInstance().getConfigModel().getSchemas().getSchema(db);
        if (schema == null) {
            writeErrMessage(ErrorCode.ER_BAD_DB_ERROR, "Unknown database '" + db + "'");
            return;
        }

        // 路由计算
        RouteResultset rrs = null;
        try {
            rrs = ServerRouter.route(schema, sql, this);
        } catch (SQLNonTransientException e) {
            StringBuilder s = new StringBuilder();
            LOGGER.warn(s.append(this).append(sql).toString(), e);
            String msg = e.getMessage();
            writeErrMessage(ErrorCode.ER_PARSE_ERROR, msg == null ? e.getClass().getSimpleName() : msg);
            return;
        }

        // session执行
        session.execute(rrs, type);
    }

    /**
     * 提交事务
     */
    public void commit() {
        if (txInterrupted) {
            writeErrMessage(ErrorCode.ER_YES, "Transaction error, need to rollback.");
        } else {
            session.commit();
        }
    }

    /**
     * 回滚事务
     */
    public void rollback() {
        // 状态检查
        if (txInterrupted) {
            txInterrupted = false;
        }

        // 执行回滚
        session.rollback();
    }

    /**
     * 撤销执行中的语句
     * 
     * @param sponsor 发起者为null表示是自己
     */
    public void cancel(final FrontendConnection sponsor) {
        processor.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                session.cancel(sponsor);
            }
        });
    }

    @Override
    public void error(int code, Throwable t) {
        // 根据异常类型和信息，选择日志输出级别。
        if (t instanceof EOFException) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(this, t);
            }
        } else if (t instanceof IOException) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(this, t);
            }
        } else {
            LOGGER.warn(this, t);
        }

        // 异常返回码处理
        switch (code) {
        case ErrorCode.ERR_HANDLE_DATA:
            String msg = t.getMessage();
            writeErrMessage(ErrorCode.ER_YES, msg == null ? t.getClass().getSimpleName() : msg);
            break;
        default:
            close();
        }
    }

    @Override
    public boolean close() {
        if (super.close()) {
            processor.getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    session.terminate();
                }
            });
            return true;
        } else {
            return false;
        }
    }

}
