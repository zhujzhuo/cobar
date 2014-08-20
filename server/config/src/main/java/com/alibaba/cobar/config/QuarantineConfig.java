package com.alibaba.cobar.config;

import java.util.List;

/**
 * @author xianmao.hexm
 */
public class QuarantineConfig {

    private List<Host> hostList;

    public List<Host> getHostList() {
        return hostList;
    }

    public void setHostList(List<Host> hostList) {
        this.hostList = hostList;
    }

    public static class Host {
        private String name;
        private String users;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUsers() {
            return users;
        }

        public void setUsers(String users) {
            this.users = users;
        }

    }
}
