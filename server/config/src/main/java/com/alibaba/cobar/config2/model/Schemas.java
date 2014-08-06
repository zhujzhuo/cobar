package com.alibaba.cobar.config2.model;

import java.util.List;

/**
 * 
 * @author xianmao.hexm
 *
 */
public class Schemas {
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
		private String id;
		private String instance;
		private String name;
		private String user;
		private String password;

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
		 * @return the instance
		 */
		public String getInstance() {
			return instance;
		}

		/**
		 * @param instance
		 *            the instance to set
		 */
		public void setInstance(String instance) {
			this.instance = instance;
		}

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

		/**
		 * @return the password
		 */
		public String getPassword() {
			return password;
		}

		/**
		 * @param password
		 *            the password to set
		 */
		public void setPassword(String password) {
			this.password = password;
		}

	}

}
