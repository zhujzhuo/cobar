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

import com.alibaba.cobar.config.model.ClusterModel;

/**
 * @author xianmao.hexm
 */
public class ClusterConfig {

    private List<Node> nodeList;

    public ClusterConfig(ClusterModel model) {
        nodeList = new ArrayList<Node>();
        for (ClusterModel.Node node : model.getNodeList()) {
            nodeList.add(new ClusterConfig.Node(node));
        }
    }

    public List<Node> getNodeList() {
        return nodeList;
    }

    public static class Node {
        private String name;
        private String host;
        private int weight;

        public Node(ClusterModel.Node model) {
            String name = model.getName();
            if (name != null) {
                this.name = name.trim();
            }
            String host = model.getHost();
            if (host != null) {
                this.host = host.trim();
            }
            String weight = model.getWeight();
            if (weight != null) {
                this.weight = Integer.parseInt(weight.trim());
            }
        }

        public String getName() {
            return name;
        }

        public String getHost() {
            return host;
        }

        public int getWeight() {
            return weight;
        }

    }

}
