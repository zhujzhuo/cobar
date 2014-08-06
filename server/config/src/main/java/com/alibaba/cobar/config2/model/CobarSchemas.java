package com.alibaba.cobar.config2.model;

import java.util.List;

/**
 * 
 * @author xianmao.hexm
 *
 */
public class CobarSchemas {
	private List<Schema> schemaList;

	/**
	 * @return the schemaList
	 */
	public List<Schema> getSchemaList() {
		return schemaList;
	}

	/**
	 * @param schemaList
	 *            the schemaList to set
	 */
	public void setSchemaList(List<Schema> schemaList) {
		this.schemaList = schemaList;
	}

	public static class Schema {
		private String name;
		private String dataNode;
		private String ruleClass;

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param name
		 *            the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @return the dataNode
		 */
		public String getDataNode() {
			return dataNode;
		}

		/**
		 * @param dataNode
		 *            the dataNode to set
		 */
		public void setDataNode(String dataNode) {
			this.dataNode = dataNode;
		}

		/**
		 * @return the ruleClass
		 */
		public String getRuleClass() {
			return ruleClass;
		}

		/**
		 * @param ruleClass
		 *            the ruleClass to set
		 */
		public void setRuleClass(String ruleClass) {
			this.ruleClass = ruleClass;
		}

	}

}
