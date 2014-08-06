package com.alibaba.cobar.config2.model;


/**
 * 
 * @author xianmao.hexm
 *
 */
public class CobarConfig {

	private Server server;
	private Cluster cluster;
	private Quarantine quarantine;
	private Users users;
	private CobarSchemas cobarSchemas;
	private DataNodes dataNodes;
	private Schemas schemas;
	private Instances instances;
	private Machines machines;

	/**
	 * @return the server
	 */
	public Server getServer() {
		return server;
	}

	/**
	 * @param server
	 *            the server to set
	 */
	public void setServer(Server server) {
		this.server = server;
	}

	/**
	 * @return the cluster
	 */
	public Cluster getCluster() {
		return cluster;
	}

	/**
	 * @param cluster
	 *            the cluster to set
	 */
	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	/**
	 * @return the quarantine
	 */
	public Quarantine getQuarantine() {
		return quarantine;
	}

	/**
	 * @param quarantine
	 *            the quarantine to set
	 */
	public void setQuarantine(Quarantine quarantine) {
		this.quarantine = quarantine;
	}

	/**
	 * @return the users
	 */
	public Users getUsers() {
		return users;
	}

	/**
	 * @param users
	 *            the users to set
	 */
	public void setUsers(Users users) {
		this.users = users;
	}

	/**
	 * @return the cobarSchemas
	 */
	public CobarSchemas getCobarSchemas() {
		return cobarSchemas;
	}

	/**
	 * @param cobarSchemas
	 *            the cobarSchemas to set
	 */
	public void setCobarSchemas(CobarSchemas cobarSchemas) {
		this.cobarSchemas = cobarSchemas;
	}

	/**
	 * @return the dataNodes
	 */
	public DataNodes getDataNodes() {
		return dataNodes;
	}

	/**
	 * @param dataNodes
	 *            the dataNodes to set
	 */
	public void setDataNodes(DataNodes dataNodes) {
		this.dataNodes = dataNodes;
	}

	/**
	 * @return the schemas
	 */
	public Schemas getSchemas() {
		return schemas;
	}

	/**
	 * @param schemas
	 *            the schemas to set
	 */
	public void setSchemas(Schemas schemas) {
		this.schemas = schemas;
	}

	/**
	 * @return the instances
	 */
	public Instances getInstances() {
		return instances;
	}

	/**
	 * @param instances
	 *            the instances to set
	 */
	public void setInstances(Instances instances) {
		this.instances = instances;
	}

	/**
	 * @return the machines
	 */
	public Machines getMachines() {
		return machines;
	}

	/**
	 * @param machines
	 *            the machines to set
	 */
	public void setMachines(Machines machines) {
		this.machines = machines;
	}

}
