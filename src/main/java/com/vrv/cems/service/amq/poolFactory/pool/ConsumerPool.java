package com.vrv.cems.service.amq.poolFactory.pool;

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.Session;

/**
 * 消费者池中对象
 * @author chenrui
 * @version 1.0
 */
public class ConsumerPool {

	private Connection connection;
	private Session session;
	private MessageConsumer consumer;
	
	public Connection getConnection() {
		return connection;
	}
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	public Session getSession() {
		return session;
	}
	public void setSession(Session session) {
		this.session = session;
	}
	public MessageConsumer getConsumer() {
		return consumer;
	}
	public void setConsumer(MessageConsumer consumer) {
		this.consumer = consumer;
	}
	
	
}
