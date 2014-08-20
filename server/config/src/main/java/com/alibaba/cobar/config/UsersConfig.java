package com.alibaba.cobar.config;

import java.util.List;

/**
 * @author xianmao.hexm
 */
public class UsersConfig {

    private List<User> userList;

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    public static class User {
        private String name;
        private String password;
        private String schemas;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getSchemas() {
            return schemas;
        }

        public void setSchemas(String schemas) {
            this.schemas = schemas;
        }

    }

}
