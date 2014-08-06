package com.alibaba.cobar.config2.model;

/**
 * 
 * @author xianmao.hexm
 *
 */
public class Server {

	private String serverPort;
	private String managerPort;
	private String initExecutor;
	private String timerExecutor;
	private String managerExecutor;
	private String processors;
	private String processorHandler;
	private String processorExecutor;
	private String clusterHeartbeatUser;
	private String clusterHeartbeatPass;

	/**
	 * @return the serverPort
	 */
	public String getServerPort() {
		return serverPort;
	}

	/**
	 * @param serverPort
	 *            the serverPort to set
	 */
	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}

	/**
	 * @return the managerPort
	 */
	public String getManagerPort() {
		return managerPort;
	}

	/**
	 * @param managerPort
	 *            the managerPort to set
	 */
	public void setManagerPort(String managerPort) {
		this.managerPort = managerPort;
	}

	/**
	 * @return the initExecutor
	 */
	public String getInitExecutor() {
		return initExecutor;
	}

	/**
	 * @param initExecutor
	 *            the initExecutor to set
	 */
	public void setInitExecutor(String initExecutor) {
		this.initExecutor = initExecutor;
	}

	/**
	 * @return the timerExecutor
	 */
	public String getTimerExecutor() {
		return timerExecutor;
	}

	/**
	 * @param timerExecutor
	 *            the timerExecutor to set
	 */
	public void setTimerExecutor(String timerExecutor) {
		this.timerExecutor = timerExecutor;
	}

	/**
	 * @return the managerExecutor
	 */
	public String getManagerExecutor() {
		return managerExecutor;
	}

	/**
	 * @param managerExecutor
	 *            the managerExecutor to set
	 */
	public void setManagerExecutor(String managerExecutor) {
		this.managerExecutor = managerExecutor;
	}

	/**
	 * @return the processors
	 */
	public String getProcessors() {
		return processors;
	}

	/**
	 * @param processors
	 *            the processors to set
	 */
	public void setProcessors(String processors) {
		this.processors = processors;
	}

	/**
	 * @return the processorHandler
	 */
	public String getProcessorHandler() {
		return processorHandler;
	}

	/**
	 * @param processorHandler
	 *            the processorHandler to set
	 */
	public void setProcessorHandler(String processorHandler) {
		this.processorHandler = processorHandler;
	}

	/**
	 * @return the processorExecutor
	 */
	public String getProcessorExecutor() {
		return processorExecutor;
	}

	/**
	 * @param processorExecutor
	 *            the processorExecutor to set
	 */
	public void setProcessorExecutor(String processorExecutor) {
		this.processorExecutor = processorExecutor;
	}

	/**
	 * @return the clusterHeartbeatUser
	 */
	public String getClusterHeartbeatUser() {
		return clusterHeartbeatUser;
	}

	/**
	 * @param clusterHeartbeatUser
	 *            the clusterHeartbeatUser to set
	 */
	public void setClusterHeartbeatUser(String clusterHeartbeatUser) {
		this.clusterHeartbeatUser = clusterHeartbeatUser;
	}

	/**
	 * @return the clusterHeartbeatPass
	 */
	public String getClusterHeartbeatPass() {
		return clusterHeartbeatPass;
	}

	/**
	 * @param clusterHeartbeatPass
	 *            the clusterHeartbeatPass to set
	 */
	public void setClusterHeartbeatPass(String clusterHeartbeatPass) {
		this.clusterHeartbeatPass = clusterHeartbeatPass;
	}

}
