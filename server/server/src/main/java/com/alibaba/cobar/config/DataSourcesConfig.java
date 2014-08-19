/*
 * Copyright 1999-2014 Alibaba Group.
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
package com.alibaba.cobar.config;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.cobar.config.model.DataSourcesModel;

/**
 * @author xianmao.hexm
 */
public class DataSourcesConfig {

    private Map<String, DataSource> dataSources;

    public DataSourcesConfig(DataSourcesModel model) {
        dataSources = new HashMap<String, DataSourcesConfig.DataSource>();
        for (DataSourcesModel.DataSource source : model.getDataSourceList()) {
            DataSourcesConfig.DataSource ds = new DataSource(source);
            dataSources.put(ds.getName(), ds);
        }
    }

    public Map<String, DataSource> getDataSources() {
        return dataSources;
    }

    public DataSource getDataSource(String name) {
        return dataSources.get(name);
    }

    public static class DataSource {
        private String name;
        private String instance;
        private String schema;
        private String user;
        private String password;

        public DataSource(DataSourcesModel.DataSource model) {
            String name = model.getName();
            if (name != null) {
                this.name = name.trim();
            }
            String instance = model.getInstance();
            if (instance != null) {
                this.instance = instance.trim();
            }
            String schema = model.getSchema();
            if (schema != null) {
                this.schema = schema.trim();
            }
            String user = model.getUser();
            if (user != null) {
                this.user = user.trim();
            }
            String password = model.getPassword();
            if (password != null) {
                this.password = password.trim();
            }
        }

        public String getName() {
            return name;
        }

        public String getInstance() {
            return instance;
        }

        public String getSchema() {
            return schema;
        }

        public String getUser() {
            return user;
        }

        public String getPassword() {
            return password;
        }

    }

}
