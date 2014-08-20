/*
 * Copyright 1999-2012 Alibaba Group.
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
package com.alibaba.cobar.model;

import com.alibaba.cobar.config.CobarConfig;

/**
 * @author xianmao.hexm
 */
public class Cobar {

    private Server server;
    private Cluster cluster;
    private Quarantine quarantine;
    private Users users;
    private Schemas schemas;
    private DataNodes dataNodes;
    private DataSources dataSources;
    private Instances instances;
    private Machines machines;

    public Cobar() {
        CobarConfig config = CobarConfig.getInstance();
        this.server = new Server(config.getServer());
        this.cluster = new Cluster(config.getCluster());
        this.quarantine = new Quarantine(config.getQuarantine());
        this.users = new Users(config.getUsers());
        this.schemas = new Schemas(config.getSchemas());
        this.dataNodes = new DataNodes(config.getDataNodes());
        this.dataSources = new DataSources(config.getDataSources());
        this.instances = new Instances(config.getInstances());
        this.machines = new Machines(config.getMachines());
    }

    public Server getServer() {
        return server;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public Quarantine getQuarantine() {
        return quarantine;
    }

    public Users getUsers() {
        return users;
    }

    public Schemas getSchemas() {
        return schemas;
    }

    public DataNodes getDataNodes() {
        return dataNodes;
    }

    public DataSources getDataSources() {
        return dataSources;
    }

    public Instances getInstances() {
        return instances;
    }

    public Machines getMachines() {
        return machines;
    }

}
