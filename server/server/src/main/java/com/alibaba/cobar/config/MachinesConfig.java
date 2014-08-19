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

import com.alibaba.cobar.config.model.MachinesModel;

/**
 * @author xianmao.hexm
 */
public class MachinesConfig {

    private Map<String, Machine> machines;

    public MachinesConfig(MachinesModel model) {
        machines = new HashMap<String, MachinesConfig.Machine>();
        for (MachinesModel.Machine machine : model.getMachineList()) {
            MachinesConfig.Machine mcm = new Machine(machine);
            machines.put(mcm.getName(), mcm);
        }
    }

    public Map<String, Machine> getMachines() {
        return machines;
    }

    public Machine getMachine(String name) {
        return machines.get(name);
    }

    public static class Machine {
        private String name;
        private String host;

        public Machine(MachinesModel.Machine model) {
            String name = model.getName();
            if (name != null) {
                this.name = name.trim();
            }
            String host = model.getHost();
            if (host != null) {
                this.host = host.trim();
            }
        }

        public String getName() {
            return name;
        }

        public String getHost() {
            return host;
        }

    }

}
