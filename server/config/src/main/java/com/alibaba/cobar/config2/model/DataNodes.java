package com.alibaba.cobar.config2.model;

import java.util.List;

/**
 * 
 * @author xianmao.hexm
 *
 */
public class DataNodes {
	private List<DataNode> dataNodeList;

	/**
	 * @return the dataNodeList
	 */
	public List<DataNode> getDataNodeList() {
		return dataNodeList;
	}

	/**
	 * @param dataNodeList
	 *            the dataNodeList to set
	 */
	public void setDataNodeList(List<DataNode> dataNodeList) {
		this.dataNodeList = dataNodeList;
	}

	public static class DataNode {
		private String id;
		private String schema;

		/**
		 * @return the id
		 */
		public String getId() {
			return id;
		}

		/**
		 * @param id
		 *            the id to set
		 */
		public void setId(String id) {
			this.id = id;
		}

		/**
		 * @return the schema
		 */
		public String getSchema() {
			return schema;
		}

		/**
		 * @param schema
		 *            the schema to set
		 */
		public void setSchema(String schema) {
			this.schema = schema;
		}

	}

}
