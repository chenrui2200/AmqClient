package com.vrv.cems.service.amq.poolFactory.pool;


import javax.jms.Connection;

/**
 * Connection�ض���
 * @author my
 * @version 1.0
 * @updated 08-����-2016 16:00:22
 */
public class ConnectionPool {

	private Connection connection;
	private int activeSessions;

	public Connection getConnection() {
		return connection;
	}
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	public int getActiveSessions() {
		return activeSessions;
	}
	public void setActiveSessions(int activeSessions) {
		this.activeSessions = activeSessions;
	}
	
	
	
}
