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

import com.alibaba.cobar.config.model.ServerModel;
import com.alibaba.cobar.defs.Isolations;

/**
 * 基础配置项
 * 
 * @author xianmao.hexm 2011-1-11 下午02:14:04
 */
public final class ServerConfig {

    private static final int DEFAULT_PORT = 8066;
    private static final int DEFAULT_MANAGER_PORT = 9066;
    private static final int DEFAULT_PROCESSORS = Runtime.getRuntime().availableProcessors();
    private static final String DEFAULT_CHARSET = "utf-8";
    private static final long DEFAULT_IDLE_TIMEOUT = 8 * 3600 * 1000L;
    private static final long DEFAULT_PROCESSOR_CHECK_PERIOD = 30 * 1000L;
    private static final long DEFAULT_DATANODE_IDLE_CHECK_PERIOD = 60 * 1000L;
    private static final long DEFAULT_DATANODE_HEARTBEAT_PERIOD = 10 * 1000L;
    private static final long DEFAULT_CLUSTER_HEARTBEAT_PERIOD = 5 * 1000L;
    private static final long DEFAULT_CLUSTER_HEARTBEAT_TIMEOUT = 10 * 1000L;
    private static final int DEFAULT_CLUSTER_HEARTBEAT_RETRY = 10;

    private int serverPort;
    private int managerPort;
    private int serverExecutor;
    private int managerExecutor;
    private int processors;
    private int processorExecutor;
    private String charset;
    private long idleTimeout;

    // 不可配置参数
    private int txIsolation;
    private long processorCheckPeriod;
    private long dataNodeIdleCheckPeriod;
    private long dataNodeHeartbeatPeriod;
    private long clusterHeartbeatPeriod;
    private long clusterHeartbeatTimeout;
    private int clusterHeartbeatRetry;

    public ServerConfig(ServerModel model) {
        this.serverPort = DEFAULT_PORT;
        this.managerPort = DEFAULT_MANAGER_PORT;
        this.serverExecutor = DEFAULT_PROCESSORS * 2;
        this.managerExecutor = DEFAULT_PROCESSORS * 2;
        this.processors = DEFAULT_PROCESSORS;
        this.processorExecutor = DEFAULT_PROCESSORS * 2;
        this.charset = DEFAULT_CHARSET;
        this.idleTimeout = DEFAULT_IDLE_TIMEOUT;

        // 不可配置参数
        this.txIsolation = Isolations.REPEATED_READ;
        this.processorCheckPeriod = DEFAULT_PROCESSOR_CHECK_PERIOD;
        this.dataNodeIdleCheckPeriod = DEFAULT_DATANODE_IDLE_CHECK_PERIOD;
        this.dataNodeHeartbeatPeriod = DEFAULT_DATANODE_HEARTBEAT_PERIOD;
        this.clusterHeartbeatPeriod = DEFAULT_CLUSTER_HEARTBEAT_PERIOD;
        this.clusterHeartbeatTimeout = DEFAULT_CLUSTER_HEARTBEAT_TIMEOUT;
        this.clusterHeartbeatRetry = DEFAULT_CLUSTER_HEARTBEAT_RETRY;

        // 模型到配置的初始化
        this.init(model);
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getManagerPort() {
        return managerPort;
    }

    public int getServerExecutor() {
        return serverExecutor;
    }

    public int getManagerExecutor() {
        return managerExecutor;
    }

    public int getProcessors() {
        return processors;
    }

    public int getProcessorExecutor() {
        return processorExecutor;
    }

    public String getCharset() {
        return charset;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public int getTxIsolation() {
        return txIsolation;
    }

    public long getProcessorCheckPeriod() {
        return processorCheckPeriod;
    }

    public long getDataNodeIdleCheckPeriod() {
        return dataNodeIdleCheckPeriod;
    }

    public long getDataNodeHeartbeatPeriod() {
        return dataNodeHeartbeatPeriod;
    }

    public long getClusterHeartbeatPeriod() {
        return clusterHeartbeatPeriod;
    }

    public long getClusterHeartbeatTimeout() {
        return clusterHeartbeatTimeout;
    }

    public int getClusterHeartbeatRetry() {
        return clusterHeartbeatRetry;
    }

    protected void init(ServerModel model) {
        String serverPort = model.getServerPort();
        if (serverPort != null) {
            this.serverPort = Integer.parseInt(serverPort.trim());
        }
        String managerPort = model.getManagerPort();
        if (managerPort != null) {
            this.managerPort = Integer.parseInt(managerPort.trim());
        }
        String serverExecutor = model.getServerExecutor();
        if (serverExecutor != null) {
            this.serverExecutor = Integer.parseInt(serverExecutor.trim());
        }
        String managerExecutor = model.getManagerExecutor();
        if (managerExecutor != null) {
            this.managerExecutor = Integer.parseInt(managerExecutor.trim());
        }
        String processors = model.getProcessors();
        if (processors != null) {
            this.processors = Integer.parseInt(processors.trim());
        }
        String processorExecutor = model.getProcessorExecutor();
        if (processorExecutor != null) {
            this.processorExecutor = Integer.parseInt(processorExecutor.trim());
        }
        String charset = model.getCharset();
        if (charset != null) {
            this.charset = charset.trim();
        }
        String idleTimeout = model.getIdleTimeout();
        if (idleTimeout != null) {
            this.idleTimeout = Long.parseLong(idleTimeout.trim());
        }
    }

}
