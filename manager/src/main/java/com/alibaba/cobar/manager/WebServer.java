package com.alibaba.cobar.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * 
 * @author xianmao.hexm
 *
 */
public class WebServer {

	private Server server;
	private int port;
	private String bindInterface;
	private String warPath;
	private String logPath;

	public WebServer(int aPort) {
		this(aPort, null);
	}

	public WebServer(int aPort, String aBindInterface) {
		port = aPort;
		bindInterface = aBindInterface;
	}

	public String getWarPath() {
		return warPath;
	}

	public void setWarPath(String warPath) {
		this.warPath = warPath;
	}

	public String getLogPath() {
		return logPath;
	}

	public void setLogPath(String logPath) {
		this.logPath = logPath;
	}

	public void start() throws Exception {
		server = new Server();
		server.setThreadPool(createThreadPool());
		server.addConnector(createConnector());
		server.setHandler(createHandlers());
		server.setStopAtShutdown(true);
		server.start();
		server.join();
	}

	public void stop() throws Exception {
		server.stop();
	}

	private ThreadPool createThreadPool() {
		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setMinThreads(4);
		threadPool.setMaxThreads(10);
		return threadPool;
	}

	private Connector createConnector() {
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(port);
		connector.setHost(bindInterface);
		return connector;
	}

	private HandlerCollection createHandlers() {
		WebAppContext ctx = new WebAppContext();
		ctx.setContextPath("/");
		ctx.setWar(warPath);

		List<Handler> handlers = new ArrayList<Handler>();
		handlers.add(ctx);
		HandlerList contexts = new HandlerList();
		contexts.setHandlers(handlers.toArray(new Handler[0]));

		RequestLogHandler log = new RequestLogHandler();
		log.setRequestLog(createRequestLog());
		HandlerCollection result = new HandlerCollection();
		result.setHandlers(new Handler[] { contexts, log });

		return result;
	}

	private RequestLog createRequestLog() {
		NCSARequestLog log = new NCSARequestLog();
		File path = new File(logPath);
		path.getParentFile().mkdirs();
		log.setFilename(path.getPath());
		log.setRetainDays(90);
		log.setExtended(false);
		log.setAppend(true);
		log.setLogTimeZone("GMT");
		log.setLogLatency(true);
		return log;
	}

}
