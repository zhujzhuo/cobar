package com.alibaba.cobar.config;

/**
 * @author xianmao.hexm
 */
public class ServerConfig {

    private String serverPort;
    private String managerPort;
    private String serverExecutor;
    private String managerExecutor;
    private String processors;
    private String processorExecutor;
    private String charset;
    private String idleTimeout;

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

    public String getManagerPort() {
        return managerPort;
    }

    public void setManagerPort(String managerPort) {
        this.managerPort = managerPort;
    }

    public String getServerExecutor() {
        return serverExecutor;
    }

    public void setServerExecutor(String serverExecutor) {
        this.serverExecutor = serverExecutor;
    }

    public String getManagerExecutor() {
        return managerExecutor;
    }

    public void setManagerExecutor(String managerExecutor) {
        this.managerExecutor = managerExecutor;
    }

    public String getProcessors() {
        return processors;
    }

    public void setProcessors(String processors) {
        this.processors = processors;
    }

    public String getProcessorExecutor() {
        return processorExecutor;
    }

    public void setProcessorExecutor(String processorExecutor) {
        this.processorExecutor = processorExecutor;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(String idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

}
