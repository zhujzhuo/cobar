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

import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.alibaba.cobar.server.core.defs.ErrorCode;
import com.alibaba.cobar.server.core.net.FrontendConnection;
import com.alibaba.cobar.server.startup.CobarServer;
import com.alibaba.cobar.server.util.ExecutorUtil.NameableExecutor;

/**
 * @author xianmao.hexm 2011-4-22 下午02:23:55
 */
public class ManagerConnection extends FrontendConnection {

    private static final Logger LOGGER = Logger.getLogger(ManagerConnection.class);

    public ManagerConnection(SocketChannel channel) {
        super(channel);
    }

    @Override
    protected NameableExecutor getExecutor() {
        return CobarServer.getInstance().getManagerExecutor();
    }

    @Override
    public void error(int errCode, Throwable t) {
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
        switch (errCode) {
        case ErrorCode.ERR_HANDLE_DATA:
            String msg = t.getMessage();
            writeErrMessage(ErrorCode.ER_YES, msg == null ? t.getClass().getSimpleName() : msg);
            break;
        default:
            close();
        }
    }

}
