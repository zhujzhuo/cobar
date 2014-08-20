package com.alibaba.cobar.config;

import java.util.List;

/**
 * @author xianmao.hexm
 */
public class DataNodesConfig {

    private List<DataNode> dataNodeList;

    public List<DataNode> getDataNodeList() {
        return dataNodeList;
    }

    public void setDataNodeList(List<DataNode> dataNodeList) {
        this.dataNodeList = dataNodeList;
    }

    public static class DataNode {
        private String name;
        private String dataSources;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDataSources() {
            return dataSources;
        }

        public void setDataSources(String dataSources) {
            this.dataSources = dataSources;
        }

    }

}
