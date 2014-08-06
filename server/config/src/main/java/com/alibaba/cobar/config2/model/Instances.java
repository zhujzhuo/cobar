package com.alibaba.cobar.config2.model;

import java.util.List;

/**
 * 
 * @author xianmao.hexm
 *
 */
public class Instances {
	private List<Instance> instanceList;

	/**
	 * @return the instanceList
	 */
	public List<Instance> getInstanceList() {
		return instanceList;
	}

	/**
	 * @param instanceList
	 *            the instanceList to set
	 */
	public void setInstanceList(List<Instance> instanceList) {
		this.instanceList = instanceList;
	}

	public static class Instance {
		private String id;
		private String machine;
		private String port;

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
		 * @return the machine
		 */
		public String getMachine() {
			return machine;
		}

		/**
		 * @param machine
		 *            the machine to set
		 */
		public void setMachine(String machine) {
			this.machine = machine;
		}

		/**
		 * @return the port
		 */
		public String getPort() {
			return port;
		}

		/**
		 * @param port
		 *            the port to set
		 */
		public void setPort(String port) {
			this.port = port;
		}

	}

}
