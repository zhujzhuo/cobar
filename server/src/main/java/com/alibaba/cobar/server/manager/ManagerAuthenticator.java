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
package com.alibaba.cobar.server.manager;

import org.apache.log4j.Logger;

import com.alibaba.cobar.server.net.nio.NIOHandler;
import com.alibaba.cobar.server.net.packet.AbstractPacket;
import com.alibaba.cobar.server.net.packet.AuthPacket;
import com.alibaba.cobar.server.net.packet.QuitPacket;
import com.alibaba.cobar.server.util.ByteBufferUtil;
import com.alibaba.cobar.server.util.CharsetUtil;

/**
 * @author xianmao.hexm
 */
public class ManagerAuthenticator implements NIOHandler {

    private static final Logger LOGGER = Logger.getLogger(ManagerAuthenticator.class);
    private static final byte[] AUTH_OK = new byte[] { 7, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0 };

    protected final ManagerConnection source;

    public ManagerAuthenticator(ManagerConnection source) {
        this.source = source;
    }

    @Override
    public void handle(byte[] data) {
        // check quit packet
        if (data.length == QuitPacket.QUIT.length && data[4] == AbstractPacket.COM_QUIT) {
            source.close();
            return;
        }

        // read auth packet
        AuthPacket auth = new AuthPacket();
        auth.read(data);

        // set success
        success(auth);
    }

    protected void success(AuthPacket auth) {
        source.setAuthenticated(true);
        String charset = CharsetUtil.getCharset(auth.charsetIndex);
        if (charset != null) {
            source.setCharset(charset);
        }
        source.setHandler(new ManagerDispatcher(source));
        if (LOGGER.isInfoEnabled()) {
            StringBuilder s = new StringBuilder();
            s.append(source).append('\'').append(auth.user).append("' login success");
            byte[] extra = auth.extra;
            if (extra != null && extra.length > 0) {
                s.append(",extra:").append(new String(extra));
            }
            LOGGER.info(s.toString());
        }
        ByteBufferUtil.write(AUTH_OK, source);
    }

    protected void failure(int errno, String info) {
        LOGGER.error(source.toString() + info);
        source.writeErrMessage((byte) 2, errno, info);
    }

}
