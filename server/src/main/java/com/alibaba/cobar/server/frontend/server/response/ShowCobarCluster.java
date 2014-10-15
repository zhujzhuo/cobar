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
package com.alibaba.cobar.server.frontend.server.response;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.alibaba.cobar.server.core.defs.Alarms;
import com.alibaba.cobar.server.core.defs.Fields;
import com.alibaba.cobar.server.core.model.Cluster;
import com.alibaba.cobar.server.core.net.packet.EOFPacket;
import com.alibaba.cobar.server.core.net.packet.FieldPacket;
import com.alibaba.cobar.server.core.net.packet.ResultSetHeaderPacket;
import com.alibaba.cobar.server.core.net.packet.RowDataPacket;
import com.alibaba.cobar.server.frontend.server.ServerConnection;
import com.alibaba.cobar.server.startup.CobarServer;
import com.alibaba.cobar.server.util.IntegerUtil;
import com.alibaba.cobar.server.util.PacketUtil;
import com.alibaba.cobar.server.util.StringUtil;

/**
 * @author xianmao.hexm
 */
public class ShowCobarCluster {

    private static final Logger alarm = Logger.getLogger("alarm");

    private static final int FIELD_COUNT = 2;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();
    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;
        fields[i] = PacketUtil.getField("HOST", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("WEIGHT", Fields.FIELD_TYPE_LONG);
        fields[i++].packetId = ++packetId;
        eof.packetId = ++packetId;
    }

    public static void response(ServerConnection c) {
        ByteBuffer buffer = c.allocate();

        // write header
        buffer = header.write(buffer, c);

        // write field
        for (FieldPacket field : fields) {
            buffer = field.write(buffer, c);
        }

        // write eof
        buffer = eof.write(buffer, c);

        // write rows
        byte packetId = eof.packetId;
        for (RowDataPacket row : getRows(c)) {
            row.packetId = ++packetId;
            buffer = row.write(buffer, c);
        }

        // last eof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.write(buffer, c);

        // post write
        c.write(buffer);
    }

    private static List<RowDataPacket> getRows(ServerConnection c) {
        List<RowDataPacket> rows = new LinkedList<RowDataPacket>();

        Cluster cluster = CobarServer.getInstance().getCobar().getCluster();
        for (Cluster.Node n : cluster.getNodes().values()) {
            if (n != null && n.isOnline()) {
                rows.add(getRow(n, c.getCharset()));
            }
        }

        if (rows.size() == 0) {
            alarm.error(Alarms.CLUSTER_EMPTY + c.toString());
        }

        return rows;
    }

    private static RowDataPacket getRow(Cluster.Node node, String charset) {
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(StringUtil.encode(node.getHost(), charset));
        row.add(IntegerUtil.toBytes(node.getWeight()));
        return row;
    }

}
