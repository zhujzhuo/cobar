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
package com.alibaba.cobar.server.frontend.manager.response;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.alibaba.cobar.server.core.defs.Fields;
import com.alibaba.cobar.server.core.model.DataNodes;
import com.alibaba.cobar.server.core.net.packet.EOFPacket;
import com.alibaba.cobar.server.core.net.packet.FieldPacket;
import com.alibaba.cobar.server.core.net.packet.ResultSetHeaderPacket;
import com.alibaba.cobar.server.core.net.packet.RowDataPacket;
import com.alibaba.cobar.server.frontend.manager.ManagerConnection;
import com.alibaba.cobar.server.startup.CobarServer;
import com.alibaba.cobar.server.util.PacketUtil;
import com.alibaba.cobar.server.util.StringUtil;

/**
 * 查看数据节点信息
 * 
 * @author wenfeng.cenwf 2011-4-28
 * @author xianmao.hexm
 */
public final class ShowDataNode {

    private static final int FIELD_COUNT = 2;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();
    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;

        fields[i] = PacketUtil.getField("NAME", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("DATASOURCES", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        eof.packetId = ++packetId;
    }

    public static void execute(ManagerConnection c, String name) {
        ByteBuffer buffer = c.allocate();

        // write header
        buffer = header.write(buffer, c);

        // write fields
        for (FieldPacket field : fields) {
            buffer = field.write(buffer, c);
        }

        // write eof
        buffer = eof.write(buffer, c);

        // write rows
        byte packetId = eof.packetId;
        DataNodes dnc = CobarServer.getInstance().getCobar().getDataNodes();
        for (DataNodes.DataNode dn : dnc.getDataNodes().values()) {
            RowDataPacket row = getRow(dn, c.getCharset());
            row.packetId = ++packetId;
            buffer = row.write(buffer, c);
        }

        // write last eof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.write(buffer, c);

        // post write
        c.write(buffer);
    }

    static RowDataPacket getRow(DataNodes.DataNode node, String charset) {
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(StringUtil.encode(node.getName(), charset));
        row.add(StringUtil.encode(Arrays.toString(node.getDataSources()), charset));
        return row;
    }

}
