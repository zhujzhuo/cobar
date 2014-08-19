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

import com.alibaba.cobar.config.model.InstancesModel;

/**
 * @author xianmao.hexm
 */
public class InstancesConfig {

    private Map<String, Instance> instances;

    public InstancesConfig(InstancesModel model) {
        instances = new HashMap<String, InstancesConfig.Instance>();
        for (InstancesModel.Instance instance : model.getInstanceList()) {
            InstancesConfig.Instance ici = new Instance(instance);
            instances.put(ici.getName(), ici);
        }
    }

    public Map<String, Instance> getInstances() {
        return instances;
    }

    public Instance getInstance(String name) {
        return instances.get(name);
    }

    public static class Instance {
        private String name;
        private String machine;
        private int port;

        public Instance(InstancesModel.Instance model) {
            String name = model.getName();
            if (name != null) {
                this.name = name.trim();
            }
            String machine = model.getMachine();
            if (machine != null) {
                this.machine = machine.trim();
            }
            String port = model.getPort();
            if (port != null) {
                this.port = Integer.parseInt(port.trim());
            }
        }

        public String getName() {
            return name;
        }

        public String getMachine() {
            return machine;
        }

        public int getPort() {
            return port;
        }

    }

}
