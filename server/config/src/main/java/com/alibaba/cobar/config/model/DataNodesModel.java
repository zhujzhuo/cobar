package com.alibaba.cobar.config.model;

import java.util.List;

/**
 * @author xianmao.hexm
 */
public class DataNodesModel {

    private List<DataNode> dataNodeList;

    public List<DataNode> getDataNodeList() {
        return dataNodeList;
    }

    public void setDataNodeList(List<DataNode> dataNodeList) {
        this.dataNodeList = dataNodeList;
    }

    public static class DataNode {
        private String id;
        private String dataSources;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDataSources() {
            return dataSources;
        }

        public void setDataSources(String dataSources) {
            this.dataSources = dataSources;
        }

    }

}
