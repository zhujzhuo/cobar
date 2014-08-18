package com.alibaba.cobar.config.model;

import java.util.List;

/**
 * @author xianmao.hexm
 */
public class MachinesModel {

    private List<Machine> machineList;

    public List<Machine> getMachineList() {
        return machineList;
    }

    public void setMachineList(List<Machine> machineList) {
        this.machineList = machineList;
    }

    public static class Machine {
        private String id;
        private String host;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

    }

}
