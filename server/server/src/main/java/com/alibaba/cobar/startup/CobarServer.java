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
package com.alibaba.cobar.startup;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;

import com.alibaba.cobar.frontend.manager.ManagerConnectionFactory;
import com.alibaba.cobar.frontend.server.ServerConnectionFactory;
import com.alibaba.cobar.model.Cobar;
import com.alibaba.cobar.model.Server;
import com.alibaba.cobar.net.nio.NIOAcceptor;
import com.alibaba.cobar.net.nio.NIOConnector;
import com.alibaba.cobar.net.nio.NIOProcessor;
import com.alibaba.cobar.util.ExecutorUtil;
import com.alibaba.cobar.util.ExecutorUtil.NameableExecutor;
import com.alibaba.cobar.util.TimeUtil;

/**
 * @author xianmao.hexm 2011-4-19 下午02:58:59
 */
public final class CobarServer {

    public static final String NAME = "Cobar";
    private static final long LOG_WATCH_DELAY = 60000L;
    private static final long TIME_UPDATE_PERIOD = 20L;
    private static final CobarServer INSTANCE = new CobarServer();
    private static final Logger LOGGER = Logger.getLogger(CobarServer.class);

    public static final CobarServer getInstance() {
        return INSTANCE;
    }

    private final Cobar cobar;
    private final Timer timer;
    private final NameableExecutor serverExecutor;
    private final NameableExecutor managerExecutor;
    private final NIOProcessor[] processors;
    private NIOConnector connector;
    private NIOAcceptor manager;
    private NIOAcceptor server;
    private final AtomicBoolean isOnline;
    private final long startupTime;

    private CobarServer() {
        this.cobar = new Cobar();
        this.timer = new Timer(NAME + "Timer", true);
        Server sc = cobar.getServer();
        this.serverExecutor = ExecutorUtil.create("ServerExecutor", sc.getServerExecutor());
        this.managerExecutor = ExecutorUtil.create("ManagerExecutor", sc.getManagerExecutor());
        this.processors = new NIOProcessor[sc.getProcessors()];
        this.isOnline = new AtomicBoolean(true);
        this.startupTime = TimeUtil.currentTimeMillis();
    }

    public void startup() throws IOException {
        // before startup
        Server sc = cobar.getServer();
        String home = System.getProperty("cobar.home");
        if (home == null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            LogLog.warn(sdf.format(new Date()) + " [cobar.home] is not set.");
        } else {
            Log4jInitializer.configureAndWatch(home + "/conf/log4j.xml", LOG_WATCH_DELAY);
        }

        // server startup
        LOGGER.info("==========================================");
        LOGGER.info(NAME + " is ready to startup ...");
        timer.schedule(updateTime(), 0L, TIME_UPDATE_PERIOD);

        // startup processors
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
        ManagerConnectionFactory mf = new ManagerConnectionFactory();
        mf.setCharset(sc.getCharset());
        mf.setIdleTimeout(sc.getIdleTimeout());
        manager = new NIOAcceptor(NAME + "Manager", sc.getManagerPort(), mf);
        manager.setProcessors(processors);
        manager.start();
        LOGGER.info(manager.getName() + " is started and listening on " + manager.getPort());

        //        // init dataNodes
        //        Map<String, MySQLDataNode> dataNodes = config.getDataNodes();
        //        LOGGER.warn("Initialize dataNodes ...");
        //        for (MySQLDataNode node : dataNodes.values()) {
        //            node.init(1, 0);
        //        }
        //        timer.schedule(dataNodeIdleCheck(), 0L, system.getDataNodeIdleCheckPeriod());
        //        timer.schedule(dataNodeHeartbeat(), 0L, system.getDataNodeHeartbeatPeriod());

        // startup server
        ServerConnectionFactory sf = new ServerConnectionFactory();
        sf.setCharset(sc.getCharset());
        sf.setIdleTimeout(sc.getIdleTimeout());
        server = new NIOAcceptor(NAME + "Server", sc.getServerPort(), sf);
        server.setProcessors(processors);
        server.start();
        //        timer.schedule(clusterHeartbeat(), 0L, sc.getClusterHeartbeatPeriod());

        // server started
        LOGGER.info(server.getName() + " is started and listening on " + server.getPort());
        LOGGER.info("==========================================");
    }

    public Cobar getCobar() {
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

    public boolean isOnline() {
        return isOnline.get();
    }

    public void offline() {
        isOnline.set(false);
    }

    public void online() {
        isOnline.set(true);
    }

    public long getStartupTime() {
        return startupTime;
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
