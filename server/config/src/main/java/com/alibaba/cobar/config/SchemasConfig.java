package com.alibaba.cobar.config;

import java.util.List;

/**
 * @author xianmao.hexm
 */
public class SchemasConfig {

    private List<Schema> schemaList;

    public List<Schema> getSchemaList() {
        return schemaList;
    }

    public void setSchemaList(List<Schema> schemaList) {
        this.schemaList = schemaList;
    }

    public static class Schema {
        private String name;
        private String dataNodes;
        private String rule;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDataNodes() {
            return dataNodes;
        }

        public void setDataNodes(String dataNodes) {
            this.dataNodes = dataNodes;
        }

        public String getRule() {
            return rule;
        }

        public void setRule(String rule) {
            this.rule = rule;
        }

    }

}
