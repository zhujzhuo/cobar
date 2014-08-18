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
package com.alibaba.cobar.config;

/**
 * @author xianmao.hexm
 */
public class CobarConfig {

    private ServerConfig server;
    private ClusterConfig cluster;
    private QuarantineConfig quarantine;
    private UsersConfig users;
    private SchemasConfig schemas;
    private DataNodesConfig dataNodes;
    private DataSourcesConfig dataSources;
    private InstancesConfig instances;
    private MachinesConfig machines;

    public CobarConfig() {
        CobarModel model = CobarModel.getInstance();
        this.server = new ServerConfig(model.getServer());
        this.cluster = new ClusterConfig(model.getCluster());
        this.quarantine = new QuarantineConfig(model.getQuarantine());
        this.users = new UsersConfig(model.getUsers());
        this.schemas = new SchemasConfig(model.getSchemas());
        this.dataNodes = new DataNodesConfig(model.getDataNodes());
        this.dataSources = new DataSourcesConfig(model.getDataSources());
        this.instances = new InstancesConfig(model.getInstances());
        this.machines = new MachinesConfig(model.getMachines());
    }

    public ServerConfig getServer() {
        return server;
    }

    public ClusterConfig getCluster() {
        return cluster;
    }

    public QuarantineConfig getQuarantine() {
        return quarantine;
    }

    public UsersConfig getUsers() {
        return users;
    }

    public SchemasConfig getSchemas() {
        return schemas;
    }

    public DataNodesConfig getDataNodes() {
        return dataNodes;
    }

    public DataSourcesConfig getDataSources() {
        return dataSources;
    }

    public InstancesConfig getInstances() {
        return instances;
    }

    public MachinesConfig getMachines() {
        return machines;
    }

}
