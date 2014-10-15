package com.alibaba.cobar.server.config;

import java.util.List;

/**
 * @author xianmao.hexm
 */
public class ClusterConfig {

    private List<Node> nodeList;

    public List<Node> getNodeList() {
        return nodeList;
    }

    public void setNodeList(List<Node> nodeList) {
        this.nodeList = nodeList;
    }

    public static class Node {
        private String name;
        private String host;
        private String weight;

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

        public String getWeight() {
            return weight;
        }

        public void setWeight(String weight) {
            this.weight = weight;
        }

    }

}
