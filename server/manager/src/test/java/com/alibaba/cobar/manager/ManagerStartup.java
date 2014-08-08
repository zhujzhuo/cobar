package com.alibaba.cobar.manager;

public class ManagerStartup {

	public static void main(String[] args) throws Exception {
		WebServer server = new WebServer(80);
		server.setWarPath("src/main/webapp");
		server.setLogPath("logs/yyyy_mm_dd.log");
		server.start();
	}

}
