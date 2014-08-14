package com.alibaba.cobar.config2.model;

import java.util.List;

/**
 * @author xianmao.hexm
 */
public class DataNodes {
    private List<DataNode> dataNodeList;

    public List<DataNode> getDataNodeList() {
        return dataNodeList;
    }

    public void setDataNodeList(List<DataNode> dataNodeList) {
        this.dataNodeList = dataNodeList;
    }

    public static class DataNode {
        private String id;
        private String dataSource;

        /**
         * @return the id
         */
        public String getId() {
            return id;
        }

        /**
         * @param id the id to set
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * @return the dataSource
         */
        public String getDataSource() {
            return dataSource;
        }

        /**
         * @param dataSource the dataSource to set
         */
        public void setDataSource(String dataSource) {
            this.dataSource = dataSource;
        }

    }

}
