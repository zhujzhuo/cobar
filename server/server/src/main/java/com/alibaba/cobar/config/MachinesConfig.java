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

import com.alibaba.cobar.config.model.MachinesModel;

/**
 * @author xianmao.hexm
 */
public class MachinesConfig {

    private List<Machine> machineList;

    public MachinesConfig(MachinesModel model) {
        machineList = new ArrayList<MachinesConfig.Machine>();
        for (MachinesModel.Machine machine : model.getMachineList()) {
            machineList.add(new Machine(machine));
        }
    }

    public List<Machine> getMachineList() {
        return machineList;
    }

    public static class Machine {
        private String id;
        private String host;

        public Machine(MachinesModel.Machine model) {
            String id = model.getId();
            if (id != null) {
                this.id = id.trim();
            }
            String host = model.getHost();
            if (host != null) {
                this.host = host.trim();
            }
        }

        public String getId() {
            return id;
        }

        public String getHost() {
            return host;
        }

    }

}
