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

import java.util.Set;

import org.apache.log4j.Logger;

import com.alibaba.cobar.server.defs.Alarms;
import com.alibaba.cobar.server.model.Quarantine;
import com.alibaba.cobar.server.model.Schemas;
import com.alibaba.cobar.server.model.Users;
import com.alibaba.cobar.server.startup.CobarContainer;

/**
 * @author xianmao.hexm
 */
public class ServerPrivileges {

    private static final Logger ALARM = Logger.getLogger("alarm");

    public boolean schemaExists(String schema) {
        Schemas sc = CobarContainer.getInstance().getConfigModel().getSchemas();
        return sc.getSchemas().containsKey(schema);
    }

    public boolean userExists(String user, String host) {
        Quarantine quarantine = CobarContainer.getInstance().getConfigModel().getQuarantine();
        if (quarantine.getHosts().containsKey(host)) {
            boolean rs = quarantine.getHost(host).getUsers().contains(user);
            if (!rs) {
                ALARM.error(new StringBuilder().append(Alarms.QUARANTINE_ATTACK)
                                               .append("[host=")
                                               .append(host)
                                               .append(",user=")
                                               .append(user)
                                               .append(']')
                                               .toString());
            }
            return rs;
        } else {
            Users users = CobarContainer.getInstance().getConfigModel().getUsers();
            return users.getUsers().containsKey(user);
        }
    }

    /**
     * 取得用户密码
     */
    public String getPassword(String user) {
        Users.User ucu = CobarContainer.getInstance().getConfigModel().getUsers().getUser(user);
        if (ucu != null) {
            return ucu.getPassword();
        } else {
            return null;
        }
    }

    /**
     * 取得用户schema集合
     */
    public Set<String> getUserSchemas(String user) {
        Users.User ucu = CobarContainer.getInstance().getConfigModel().getUsers().getUser(user);
        if (ucu != null) {
            return ucu.getSchemas();
        } else {
            return null;
        }
    }

}
