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
package com.alibaba.cobar.frontend.manager.response;

import java.nio.ByteBuffer;

import com.alibaba.cobar.config.DataSourcesConfig;
import com.alibaba.cobar.defs.Fields;
import com.alibaba.cobar.frontend.manager.ManagerConnection;
import com.alibaba.cobar.net.packet.EOFPacket;
import com.alibaba.cobar.net.packet.FieldPacket;
import com.alibaba.cobar.net.packet.ResultSetHeaderPacket;
import com.alibaba.cobar.net.packet.RowDataPacket;
import com.alibaba.cobar.startup.CobarServer;
import com.alibaba.cobar.util.PacketUtil;
import com.alibaba.cobar.util.StringUtil;

/**
 * 查看数据源信息
 * 
 * @author xianmao.hexm 2010-9-26 下午04:56:26
 * @author wenfeng.cenwf 2011-4-25
 */
public final class ShowDataSource {

    private static final int FIELD_COUNT = 5;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();
    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;

        fields[i] = PacketUtil.getField("NAME", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("INSTANCE", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("SCHEMA", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("USER", Fields.FIELD_TYPE_LONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("PASSWORD", Fields.FIELD_TYPE_VAR_STRING);
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
        DataSourcesConfig dsc = CobarServer.getInstance().getConfig().getDataSources();
        for (DataSourcesConfig.DataSource ds : dsc.getDataSources().values()) {
            RowDataPacket row = getRow(ds, c.getCharset());
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

    static RowDataPacket getRow(DataSourcesConfig.DataSource dsc, String charset) {
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(StringUtil.encode(dsc.getName(), charset));
        row.add(StringUtil.encode(dsc.getInstance(), charset));
        row.add(StringUtil.encode(dsc.getSchema(), charset));
        row.add(StringUtil.encode(dsc.getUser(), charset));
        row.add(StringUtil.encode(dsc.getPassword(), charset));
        return row;
    }

}
