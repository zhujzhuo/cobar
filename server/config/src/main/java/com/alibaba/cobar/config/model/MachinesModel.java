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
        private String name;
        private String host;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

    }

}
