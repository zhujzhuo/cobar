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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alibaba.cobar.config.model.UsersModel;
import com.alibaba.cobar.util.SplitUtil;

/**
 * @author xianmao.hexm 2011-1-11 下午02:26:09
 */
public class UsersConfig {

    private List<User> userList;

    public UsersConfig(UsersModel model) {
        userList = new ArrayList<UsersConfig.User>();
        for (UsersModel.User user : model.getUserList()) {
            userList.add(new User(user));
        }
    }

    public List<User> getUserList() {
        return userList;
    }

    public static class User {
        private String name;
        private String password;
        private Set<String> schemas;

        public User(UsersModel.User user) {
            String name = user.getName();
            if (name != null) {
                this.name = name.trim();
            }
            String password = user.getPassword();
            if (password != null) {
                this.password = password.trim();
            }
            this.schemas = new HashSet<String>();
            String schemas = user.getSchemas();
            if (schemas != null) {
                for (String u : SplitUtil.split(schemas, ',', true)) {
                    this.schemas.add(u);
                }
            }
        }

        public String getName() {
            return name;
        }

        public String getPassword() {
            return password;
        }

        public Set<String> getSchemas() {
            return schemas;
        }

    }

}
