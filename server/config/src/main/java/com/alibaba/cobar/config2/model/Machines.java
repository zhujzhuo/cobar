package com.alibaba.cobar.config2.model;

import java.util.List;

/**
 * 
 * @author xianmao.hexm
 *
 */
public class Machines {
	private List<Machine> machineList;

	/**
	 * @return the machineList
	 */
	public List<Machine> getMachineList() {
		return machineList;
	}

	/**
	 * @param machineList
	 *            the machineList to set
	 */
	public void setMachineList(List<Machine> machineList) {
		this.machineList = machineList;
	}

	public static class Machine {
		private String id;
		private String host;

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

	}

}
