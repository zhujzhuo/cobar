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

import java.util.ArrayList;
import java.util.List;

import com.alibaba.cobar.config.model.DataNodesModel;
import com.alibaba.cobar.util.SplitUtil;

/**
 * @author xianmao.hexm
 */
public class DataNodesConfig {

    private List<DataNode> dataNodeList;

    public DataNodesConfig(DataNodesModel model) {
        dataNodeList = new ArrayList<DataNodesConfig.DataNode>();
        for (DataNodesModel.DataNode dataNode : model.getDataNodeList()) {
            dataNodeList.add(new DataNode(dataNode));
        }
    }

    public List<DataNode> getDataNodeList() {
        return dataNodeList;
    }

    public static class DataNode {
        private String id;
        private String[] dataSources;

        public DataNode(DataNodesModel.DataNode model) {
            String id = model.getId();
            if (id != null) {
                this.id = id.trim();
            }
            String dataSources = model.getDataSources();
            if (dataSources != null) {
                this.dataSources = SplitUtil.split(dataSources, ',', true);
            }
        }

        public String getId() {
            return id;
        }

        public String[] getDataSources() {
            return dataSources;
        }

    }

}
