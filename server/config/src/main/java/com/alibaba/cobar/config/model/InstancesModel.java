package com.alibaba.cobar.config.model;

import java.util.List;

/**
 * @author xianmao.hexm
 */
public class InstancesModel {

    private List<Instance> instanceList;

    public List<Instance> getInstanceList() {
        return instanceList;
    }

    public void setInstanceList(List<Instance> instanceList) {
        this.instanceList = instanceList;
    }

    public static class Instance {
        private String id;
        private String machine;
        private String port;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMachine() {
            return machine;
        }

        public void setMachine(String machine) {
            this.machine = machine;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

    }

}
