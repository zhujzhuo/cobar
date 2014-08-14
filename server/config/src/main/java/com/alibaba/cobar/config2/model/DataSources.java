package com.alibaba.cobar.config2.model;

import java.util.List;

/**
 * @author xianmao.hexm
 */
public class DataSources {

    private List<DataSource> dataSourceList;

    public List<DataSource> getDataSourceList() {
        return dataSourceList;
    }

    public void setDataSourceList(List<DataSource> dataSourceList) {
        this.dataSourceList = dataSourceList;
    }

    public static class DataSource {
        private String id;
        private String instance;
        private String schema;
        private String user;
        private String password;

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
         * @return the instance
         */
        public String getInstance() {
            return instance;
        }

        /**
         * @param instance the instance to set
         */
        public void setInstance(String instance) {
            this.instance = instance;
        }

        /**
         * @return the schema
         */
        public String getSchema() {
            return schema;
        }

        /**
         * @param schema the schema to set
         */
        public void setSchema(String schema) {
            this.schema = schema;
        }

        /**
         * @return the user
         */
        public String getUser() {
            return user;
        }

        /**
         * @param user the user to set
         */
        public void setUser(String user) {
            this.user = user;
        }

        /**
         * @return the password
         */
        public String getPassword() {
            return password;
        }

        /**
         * @param password the password to set
         */
        public void setPassword(String password) {
            this.password = password;
        }

    }

}
