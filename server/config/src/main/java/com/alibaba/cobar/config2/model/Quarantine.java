package com.alibaba.cobar.config2.model;

import java.util.List;

/**
 * 
 * @author xianmao.hexm
 *
 */
public class Quarantine {

	private List<Host> hostList;

	/**
	 * @return the hostList
	 */
	public List<Host> getHostList() {
		return hostList;
	}

	/**
	 * @param hostList
	 *            the hostList to set
	 */
	public void setHostList(List<Host> hostList) {
		this.hostList = hostList;
	}

	public static class Host {
		private String name;
		private String user;

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
		 * @return the user
		 */
		public String getUser() {
			return user;
		}

		/**
		 * @param user
		 *            the user to set
		 */
		public void setUser(String user) {
			this.user = user;
		}

	}
}
