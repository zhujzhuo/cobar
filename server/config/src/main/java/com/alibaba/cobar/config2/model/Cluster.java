package com.alibaba.cobar.config2.model;

import java.util.List;

/**
 * 
 * @author xianmao.hexm
 *
 */
public class Cluster {
	private List<Node> nodeList;

	/**
	 * @return the nodeList
	 */
	public List<Node> getNodeList() {
		return nodeList;
	}

	/**
	 * @param nodeList
	 *            the nodeList to set
	 */
	public void setNodeList(List<Node> nodeList) {
		this.nodeList = nodeList;
	}

	public static class Node {
		private String name;
		private String host;
		private String weight;

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
		 * @return the host
		 */
		public String getHost() {
			return host;
		}

		/**
		 * @param host
		 *            the host to set
		 */
		public void setHost(String host) {
			this.host = host;
		}

		/**
		 * @return the weight
		 */
		public String getWeight() {
			return weight;
		}

		/**
		 * @param weight
		 *            the weight to set
		 */
		public void setWeight(String weight) {
			this.weight = weight;
		}

	}

}
