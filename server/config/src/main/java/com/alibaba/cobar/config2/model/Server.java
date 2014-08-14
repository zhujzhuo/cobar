package com.alibaba.cobar.config2.model;

/**
 * @author xianmao.hexm
 */
public class Server {

    private String serverPort;
    private String managerPort;
    private String serverExecutor;
    private String managerExecutor;
    private String processors;
    private String processorExecutor;
    private String charset;
    private String idleTimeout;
    private String txIsolation;

    /**
     * @return the serverPort
     */
    public String getServerPort() {
        return serverPort;
    }

    /**
     * @param serverPort the serverPort to set
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
     * @param managerPort the managerPort to set
     */
    public void setManagerPort(String managerPort) {
        this.managerPort = managerPort;
    }

    /**
     * @return the serverExecutor
     */
    public String getServerExecutor() {
        return serverExecutor;
    }

    /**
     * @param serverExecutor the serverExecutor to set
     */
    public void setServerExecutor(String serverExecutor) {
        this.serverExecutor = serverExecutor;
    }

    /**
     * @return the managerExecutor
     */
    public String getManagerExecutor() {
        return managerExecutor;
    }

    /**
     * @param managerExecutor the managerExecutor to set
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
     * @param processors the processors to set
     */
    public void setProcessors(String processors) {
        this.processors = processors;
    }

    /**
     * @return the processorExecutor
     */
    public String getProcessorExecutor() {
        return processorExecutor;
    }

    /**
     * @param processorExecutor the processorExecutor to set
     */
    public void setProcessorExecutor(String processorExecutor) {
        this.processorExecutor = processorExecutor;
    }

    /**
     * @return the charset
     */
    public String getCharset() {
        return charset;
    }

    /**
     * @param charset the charset to set
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * @return the idleTimeout
     */
    public String getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * @param idleTimeout the idleTimeout to set
     */
    public void setIdleTimeout(String idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    /**
     * @return the txIsolation
     */
    public String getTxIsolation() {
        return txIsolation;
    }

    /**
     * @param txIsolation the txIsolation to set
     */
    public void setTxIsolation(String txIsolation) {
        this.txIsolation = txIsolation;
    }

}
