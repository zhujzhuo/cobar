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
package com.alibaba.cobar.server.backend;

import java.io.IOException;

import com.alibaba.cobar.server.model.CobarModel;
import com.alibaba.cobar.server.model.DataSources.DataSource;
import com.alibaba.cobar.server.startup.CobarContainer;

/**
 * @author xianmao.hexm
 */
public class MySQLConnectionTest {

    public static void main(String[] args) throws IOException {
        // 启动容器
        CobarContainer container = CobarContainer.getInstance();
        container.startup();

        // 通过连接工厂创建连接
        CobarModel model = container.getConfigModel();
        DataSource dataSource = model.getDataSources().getDataSource("S1");
        MySQLConnectionFactory factory = new MySQLConnectionFactory();
        for (int i = 0; i < 0; i++) {
            factory.make(dataSource, new MySQLResponseHandlerTest());
        }

        // 通过连接池创建连接
        MySQLConnectionPool pool = container.getConnectionPool("S1");
        for (int i = 0; i < 2; i++) {
            pool.aquireConnection(new MySQLResponseHandlerTest());
        }

        // report
        while (true) {
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println("size=" + pool.getSize() + ",active=" + pool.getActiveCount() + ",idle="
                    + pool.getIdleCount());
        }
    }

}
