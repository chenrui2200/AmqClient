package com.vrv.cems.service.amq.poolFactory.pool;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * producer�ض���
 * @author chenrui
 * @version 1.0
 * @updated 08-����-2016 16:00:25
 */
public class ProducerPool {

	private Connection connection;
	private Session session;
	private MessageProducer producer;
	
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
	public MessageProducer getProducer() {
		return producer;
	}
	public void setProducer(MessageProducer producer) {
		this.producer = producer;
	}
	
}
