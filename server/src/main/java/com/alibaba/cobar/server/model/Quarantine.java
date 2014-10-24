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
package com.alibaba.cobar.server.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.alibaba.cobar.server.config.QuarantineConfig;
import com.alibaba.cobar.server.util.SplitUtil;

/**
 * 隔离区配置定义
 * 
 * @author xianmao.hexm
 */
public class Quarantine {

    private Map<String, Host> hosts;

    public Quarantine(QuarantineConfig model) {
        hosts = new HashMap<String, Host>();
        for (QuarantineConfig.Host host : model.getHostList()) {
            Quarantine.Host qch = new Quarantine.Host(host);
            hosts.put(qch.getName(), qch);
        }
    }

    public Map<String, Host> getHosts() {
        return hosts;
    }

    public Host getHost(String name) {
        return hosts.get(name);
    }

    public static class Host {
        private String name;
        private Set<String> users;

        public Host(QuarantineConfig.Host model) {
            String name = model.getName();
            if (name != null) {
                this.name = name.trim();
            }
            this.users = new HashSet<String>();
            String users = model.getUsers();
            if (users != null) {
                for (String u : SplitUtil.split(users, ',', true)) {
                    this.users.add(u);
                }
            }
        }

        public String getName() {
            return name;
        }

        public Set<String> getUsers() {
            return users;
        }

    }

}
