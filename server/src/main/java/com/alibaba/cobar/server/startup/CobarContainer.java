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
package com.alibaba.cobar.server.startup;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;

import com.alibaba.cobar.server.backend.MySQLConnectionPool;
import com.alibaba.cobar.server.frontend.ServerConnectionFactory;
import com.alibaba.cobar.server.heartbeat.CobarNodeConnectionFactory;
import com.alibaba.cobar.server.heartbeat.MySQLNodeConnectionFactory;
import com.alibaba.cobar.server.manager.ManagerConnectionFactory;
import com.alibaba.cobar.server.model.CobarModel;
import com.alibaba.cobar.server.model.DataSources.DataSource;
import com.alibaba.cobar.server.model.Server;
import com.alibaba.cobar.server.net.nio.NIOAcceptor;
import com.alibaba.cobar.server.net.nio.NIOConnector;
import com.alibaba.cobar.server.net.nio.NIOProcessor;
import com.alibaba.cobar.server.util.ExecutorUtil;
import com.alibaba.cobar.server.util.ExecutorUtil.NameableExecutor;
import com.alibaba.cobar.server.util.TimeUtil;

/**
 * @author xianmao.hexm 2011-4-19 下午02:58:59
 */
public final class CobarContainer {

    private static final String NAME = "Cobar";
    private static final long LOG_WATCH_DELAY = 60 * 1000L;
    private static final long TIME_UPDATE_PERIOD = 20L;
    private static final CobarContainer INSTANCE = new CobarContainer();
    private static final Logger LOGGER = Logger.getLogger(CobarContainer.class);

    public static final CobarContainer getInstance() {
        return INSTANCE;
    }

    private CobarModel cobar;
    private Timer timer;
    private NameableExecutor serverExecutor;
    private NameableExecutor managerExecutor;
    private NIOProcessor[] processors;
    private NIOConnector connector;
    private NIOAcceptor manager;
    private NIOAcceptor server;
    private Map<String, MySQLConnectionPool> connectionPools;
    private long startupTime;
    private AtomicBoolean online;
    private CobarNodeConnectionFactory cobarNodeFactory;
    private MySQLNodeConnectionFactory mysqlNodeFactory;

    private CobarContainer() {
        this.init();
    }

    private void init() {
        this.cobar = new CobarModel();
        this.timer = new Timer(NAME + "Timer", true);
        this.timer.schedule(updateTime(), 0L, TIME_UPDATE_PERIOD);
        Server sc = cobar.getServer();
        this.serverExecutor = ExecutorUtil.create("ServerExecutor", sc.getServerExecutor());
        this.managerExecutor = ExecutorUtil.create("ManagerExecutor", sc.getManagerExecutor());
        this.processors = new NIOProcessor[sc.getProcessors()];
        this.connectionPools = initConnectionPools();
        this.online = new AtomicBoolean(false);
        this.cobarNodeFactory = new CobarNodeConnectionFactory();
        this.mysqlNodeFactory = new MySQLNodeConnectionFactory();
    }

    public void startup() throws IOException {
        // ready to startup
        Server sc = cobar.getServer();
        String home = System.getProperty("cobar.home");
        if (home == null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            LogLog.warn(sdf.format(new Date()) + " [cobar.home] is not set.");
        } else {
            Log4jInitializer.configureAndWatch(home + "/conf/log4j.xml", LOG_WATCH_DELAY);
        }

        // startup processors
        LOGGER.info("==========================================");
        LOGGER.info(NAME + " is ready to startup ...");
        LOGGER.info("Startup processors ...");
        int processorExecutor = sc.getProcessorExecutor();
        for (int i = 0; i < processors.length; i++) {
            processors[i] = new NIOProcessor("Processor" + i, processorExecutor);
            processors[i].startup();
        }
        timer.schedule(processorCheck(), 0L, sc.getProcessorCheckPeriod());

        // startup connector
        LOGGER.info("Startup connector ...");
        connector = new NIOConnector(NAME + "Connector");
        connector.setProcessors(processors);
        connector.start();

        // startup manager
        LOGGER.info("Startup manager ... ");
        ManagerConnectionFactory factory = new ManagerConnectionFactory();
        factory.setCharset(sc.getCharset());
        factory.setIdleTimeout(sc.getIdleTimeout());
        manager = new NIOAcceptor(NAME + "Manager", sc.getManagerPort(), factory);
        manager.setProcessors(processors);
        manager.start();
        startupTime = TimeUtil.currentTimeMillis();

        // compeleted
        LOGGER.info("Startup compeleted and listening on " + manager.getPort());
        LOGGER.info("==========================================");
    }

    public void startupServer() throws IOException {
        LOGGER.info("Startup server ... ");
        Server sc = cobar.getServer();
        ServerConnectionFactory factory = new ServerConnectionFactory();
        factory.setCharset(sc.getCharset());
        factory.setIdleTimeout(sc.getIdleTimeout());
        server = new NIOAcceptor(NAME + "Server", sc.getServerPort(), factory);
        server.setProcessors(processors);
        server.start();
        online.set(true);
        //        timer.schedule(clusterHeartbeat(), 0L, sc.getClusterHeartbeatPeriod());
    }

    public CobarModel getConfigModel() {
        return cobar;
    }

    public NIOProcessor[] getProcessors() {
        return processors;
    }

    public NIOConnector getConnector() {
        return connector;
    }

    public NameableExecutor getServerExecutor() {
        return serverExecutor;
    }

    public NameableExecutor getManagerExecutor() {
        return managerExecutor;
    }

    public MySQLConnectionPool getConnectionPool(String name) {
        return connectionPools.get(name);
    }

    public AtomicBoolean getOnline() {
        return online;
    }

    public CobarNodeConnectionFactory getCobarNodeFactory() {
        return cobarNodeFactory;
    }

    public MySQLNodeConnectionFactory getMysqlNodeFactory() {
        return mysqlNodeFactory;
    }

    public long getStartupTime() {
        return startupTime;
    }

    public Timer getTimer() {
        return timer;
    }

    Map<String, MySQLConnectionPool> initConnectionPools() {
        Map<String, MySQLConnectionPool> pools = new HashMap<String, MySQLConnectionPool>();
        int connectionPoolSize = cobar.getServer().getConnectionPoolSize();
        for (DataSource ds : cobar.getDataSources().getDataSources().values()) {
            MySQLConnectionPool pool = new MySQLConnectionPool(ds, connectionPoolSize);
            pools.put(ds.getName(), pool);
        }
        return pools;
    }

    // 系统时间定时更新任务
    TimerTask updateTime() {
        return new TimerTask() {
            @Override
            public void run() {
                TimeUtil.update();
            }
        };
    }

    // 处理器定时检查任务
    TimerTask processorCheck() {
        return new TimerTask() {
            @Override
            public void run() {
                serverExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        for (NIOProcessor p : processors) {
                            p.check();
                        }
                    }
                });
            }
        };
    }

    //    // 数据节点定时连接空闲超时检查任务
    //    TimerTask dataNodeIdleCheck() {
    //        return new TimerTask() {
    //            @Override
    //            public void run() {
    //                serverExecutor.execute(new Runnable() {
    //                    @Override
    //                    public void run() {
    //                        Map<String, MySQLDataNode> nodes = config.getDataNodes();
    //                        for (MySQLDataNode node : nodes.values()) {
    //                            node.idleCheck();
    //                        }
    //                        Map<String, MySQLDataNode> _nodes = config.getBackupDataNodes();
    //                        if (_nodes != null) {
    //                            for (MySQLDataNode node : _nodes.values()) {
    //                                node.idleCheck();
    //                            }
    //                        }
    //                    }
    //                });
    //            }
    //        };
    //    }

    //    // 数据节点定时心跳任务
    //    TimerTask dataNodeHeartbeat() {
    //        return new TimerTask() {
    //            @Override
    //            public void run() {
    //                serverExecutor.execute(new Runnable() {
    //                    @Override
    //                    public void run() {
    //                        Map<String, MySQLDataNode> nodes = config.getDataNodes();
    //                        for (MySQLDataNode node : nodes.values()) {
    //                            node.doHeartbeat();
    //                        }
    //                    }
    //                });
    //            }
    //        };
    //    }

    //    // 集群节点定时心跳任务
    //    TimerTask clusterHeartbeat() {
    //        return new TimerTask() {
    //            @Override
    //            public void run() {
    //                serverExecutor.execute(new Runnable() {
    //                    @Override
    //                    public void run() {
    //                        Map<String, CobarNode> nodes = config.getCluster().getNodes();
    //                        for (CobarNode node : nodes.values()) {
    //                            node.doHeartbeat();
    //                        }
    //                    }
    //                });
    //            }
    //        };
    //    }

}
