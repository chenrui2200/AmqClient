package com.vrv.cems.service.amq.poolFactory;


import com.vrv.cems.service.amq.common.MessageType;
import com.vrv.cems.service.amq.common.PoolConfig;
import com.vrv.cems.service.amq.exception.AMQFactoryException;
import com.vrv.cems.service.amq.poolFactory.pool.ConnectionPool;
import com.vrv.cems.service.amq.poolFactory.pool.ConsumerPool;
import com.vrv.cems.service.amq.poolFactory.pool.ProducerPool;
import com.vrv.cems.service.amq.poolFactory.pool.SessionPool;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.util.Assert;

import javax.jms.*;
import java.util.LinkedList;

/**
 * activeMQ 连接池
 * @author chenrui
 * @version 1.0
 */
@SuppressWarnings("unused")
public class AMQPoolFactory {

    /*amq连接url*/
	private String URL ;
    /*用户名*/
	private String userName ;
    /*密码*/
	private String password ;
    /*最大连接数*/
	private int maxConnection = 10;
    /*最小连接数*/
	private int minConnection = 1;
    /*最大会话连接数*/
	private int maxSesstionPerConnection = 100;
    /*最小会话连接数*/
	private int minSesstionPerConnection = 10;

	private int maxProducerPerSession = 10;

	private int minProducerPerSession = 1;

	private int maxConsumerPerSession = 10;

	private int minConsumerPerSession = 1;

	/*connection工厂*/
	private ConnectionFactory factory;

	/*填装ConnectionPool 对象的list*/
	private LinkedList<ConnectionPool> pooledConnection = new LinkedList<>();
    /*填装SessionPool 对象的list*/
	private LinkedList<SessionPool> pooledSession = new LinkedList<>();
	
	/**
	 * amq 连接池工厂
	 * @param poolConfig
	 * @throws AMQFactoryException
	 */
	public AMQPoolFactory(PoolConfig poolConfig) throws AMQFactoryException {
		LoadConfig(poolConfig);
	}
	
	/**
	 * 构造函数初始化
	 * @throws AMQFactoryException
	 */
	public AMQPoolFactory() throws AMQFactoryException{
		initFactory();
	}
	
	/**
	 * 构造函数初始化
	 * @param poolConfig
	 * @throws AMQFactoryException
	 */
	private void LoadConfig(PoolConfig poolConfig) throws AMQFactoryException{

		URL =  poolConfig.getURL();
		userName = poolConfig.getUserName();
		password = poolConfig.getPassword();
		maxConnection = poolConfig.getMaxConnection();
		minConnection = poolConfig.getMinConnection();
		maxSesstionPerConnection = poolConfig.getMaxSesstionPerConnection();
		minSesstionPerConnection = poolConfig.getMinSesstionPerConnection();
		maxProducerPerSession = poolConfig.getMaxProducerPerSession();
		minProducerPerSession = poolConfig.getMinProducerPerSession();
		maxConsumerPerSession = poolConfig.getMaxConsumerPerSession();
		minConsumerPerSession = poolConfig.getMinConsumerPerSession();

		Assert.notNull(URL,"URL不能为null");
		Assert.notNull(userName,"userName不能为null");
		Assert.notNull(password,"password不能为null");
		Assert.notNull(maxConnection,"maxConnection不能为null");
		Assert.notNull(minConnection,"minConnection不能为null");
		Assert.notNull(maxSesstionPerConnection,"maxSesstionPerConnection不能为null");
		Assert.notNull(minSesstionPerConnection,"minSesstionPerConnection不能为null");
		Assert.notNull(maxProducerPerSession,"maxProducerPerSession不能为null");
		Assert.notNull(minProducerPerSession,"minProducerPerSession不能为null");
		Assert.notNull(maxConsumerPerSession,"maxConsumerPerSession不能为null");
		Assert.notNull(minConsumerPerSession,"minConsumerPerSession不能为null");
		//初始化工厂
		initFactory();
	}
	
	/**
	 * 初始化工厂对象
	 * @throws AMQFactoryException
	 */
	private void initFactory() throws AMQFactoryException{
		try {
		    //从工厂中创建连接
			factory = new ActiveMQConnectionFactory (URL);
			if(minConnection>0 && minConnection<=maxConnection){
				for(int i=0;i<minConnection;i++){
					Connection connection = factory.createConnection(userName,password);
					connection.start();
					ConnectionPool connPool = new ConnectionPool();
					connPool.setConnection(connection);
					if(minSesstionPerConnection>0 && minSesstionPerConnection<=maxSesstionPerConnection){
						connPool.setActiveSessions(minSesstionPerConnection);
						for (int j = 0; j < minSesstionPerConnection; j++) {
							Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
							SessionPool sessionPool = new SessionPool();
							sessionPool.setConnection(connection);
							sessionPool.setSession(session);
							pooledSession.addLast(sessionPool);
						}
						pooledConnection.addLast(connPool);
					}else {
						throw new AMQFactoryException("minSessionPerConnection大于maxSessionPerConnection");
					}
				}
			}else{
				throw new AMQFactoryException("minConnections大于maxConnections");
			}
		} catch (JMSException e) {
			throw new AMQFactoryException("initFactory.JMSException",e);
		}
	}
	
	/**
	 * 获取连接池
	 * @return
	 * @throws AMQFactoryException
	 */
	private ConnectionPool getConnection() throws AMQFactoryException{
		ConnectionPool connPool = null;
		//如果初始化有问题会返回null
		if(pooledConnection!=null && pooledConnection.size()>0){
            //在pooledConnection 寻找有效的连接并转移指针到 connPool 对象
		    for(ConnectionPool connectionPool : pooledConnection){
				int poolSessionSize = connectionPool.getActiveSessions();
				if(poolSessionSize<maxSesstionPerConnection){
				    //链接池中活跃连接数小于阈值
					connPool = connectionPool;
					break;
				}
			}

			//如果由于 pooledConnection 为空数组导致 connPool 没有有效对象
			if(connPool==null && pooledConnection.size()<maxConnection){
				try {
					Connection conn = factory.createConnection(userName, password);
					conn.start();
					//尝试建立新amq连接,并赋给新connPool对象
					connPool = new ConnectionPool();
					connPool.setConnection(conn);
					//判断配置是否正确
					if(minSesstionPerConnection>0 && minSesstionPerConnection<=maxSesstionPerConnection){
						//初始新connPool时，用最小会话连接数
					    connPool.setActiveSessions(minSesstionPerConnection);
						for (int j = 0; j < minSesstionPerConnection; j++) {
							Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
							SessionPool sessionPool = new SessionPool();
							sessionPool.setSession(session);
							sessionPool.setConnection(conn);
							pooledSession.addLast(sessionPool);
						}
					}
					pooledConnection.addLast(connPool);
				} catch (JMSException e) {
					throw new AMQFactoryException("getConnection.JMSException",e);
				}
			}
		}
		return connPool;
	}
	
	/**
	 * 会话链接池
	 * @param ifVIP
	 * @return
	 * @throws AMQFactoryException
	 */
	private SessionPool getProducerSession(boolean ifVIP) throws AMQFactoryException{

	    SessionPool sesPool = null;

	    if(pooledSession!=null && pooledSession.size()>0){
			ConnectionPool connPool = getConnection();
			for(SessionPool sessionPool : pooledSession){

				if(sessionPool.getConnection()==connPool.getConnection()){
					int poolProducerSize = sessionPool.getAvailableProducer();
					if(ifVIP){
					    //如果是优先且保证sessionPool中有效生产者数量为0
						if(poolProducerSize == 0){
							sesPool = sessionPool;
							break;
						}
					}else{
					    //非优先原则下，保证生产者数量不越界
						if(poolProducerSize<maxProducerPerSession){
							sesPool = sessionPool;
                            break;
						}
					}
				}
			}
            //经过上面步骤如果sessionPool依然为null，可以new个新的放数组中
			if(sesPool == null && connPool.getActiveSessions() < maxSesstionPerConnection){
				try {
					Connection conn = connPool.getConnection();
					Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
					//加入一个新的sessionPool
					sesPool = new SessionPool();
					sesPool.setConnection(conn);
					sesPool.setSession(session);
					//加入到全局数组pooledSession中
					pooledSession.addLast(sesPool);
				} catch (JMSException e) {
					throw new AMQFactoryException("getProducerSession.JMSException",e);
				}
			}
		}
		return sesPool;
	}
	
	/**
	 * 获取消费者的会话
	 * @param ifVIP
	 * @return
	 * @throws AMQFactoryException
	 */
	private SessionPool getConsumerSession(boolean ifVIP) throws AMQFactoryException{
		SessionPool sespool = null;
		if(pooledSession!=null && pooledSession.size()>0){
			ConnectionPool connPool = getConnection();
			for(SessionPool sessionPool : pooledSession){
                //会话池对象和连接池对象的连接一致时
                if(sessionPool.getConnection()==connPool.getConnection()){
				    //当前会话中有效消费者数量
                    int poolConsumerSize = sessionPool.getAvailableConsumer();
					if(ifVIP){
						//如果有效消费者为0，可以视同新的有效sessionPool
					    if(poolConsumerSize == 0){
							sespool = sessionPool;
							break;
						}
					}else{
					    //保证有效消费者小于最大消费者数量限制
						if(poolConsumerSize<maxConsumerPerSession){
							sespool = sessionPool;
                            break;
						}
					}
				}
			}
			//经过上面步骤如果sessionPool依然为null，可以new个新的放数组中
			if(sespool == null && connPool.getActiveSessions()<maxSesstionPerConnection){
                Connection conn = connPool.getConnection();
			    try {

					Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
					sespool = new SessionPool();
					sespool.setConnection(conn);
					sespool.setSession(session);
					pooledSession.addLast(sespool);
				} catch (JMSException e) {
					throw new AMQFactoryException("getConsumerSession��������session����",e);
				}
			}
		}
		return sespool;
	}
	
	/**
	 * 获取生产者池
	 * @param topicName
	 * @param messageType
	 * @param ifVIP
	 * @return
	 * @throws AMQFactoryException
	 */
	public ProducerPool getProducer(String topicName, MessageType messageType, boolean ifVIP) throws AMQFactoryException{
		//
	    SessionPool sessionPool = getProducerSession(ifVIP);

	    Session session = sessionPool.getSession();
		try {
		    //目的地
			Destination ds = null;
			//根据消息不同，在session下创建不同的队列
			if(messageType.equals(MessageType.Queue)){
				ds = session.createQueue(topicName);
			}
			if(messageType.equals(MessageType.Topic)){
				ds = session.createTopic(topicName);
			}

			MessageProducer producer = session.createProducer(ds);
			ProducerPool producerPool = new ProducerPool();
			producerPool.setProducer(producer);
			producerPool.setConnection(sessionPool.getConnection());
			producerPool.setSession(session);
            //
			producerIncreament(sessionPool);
			return producerPool;
		} catch (JMSException e) {
			throw new AMQFactoryException("",e);
		}
	}
	
	/**
	 * 获取消费者
	 * @param name
	 * @param messageType
	 * @param ifVIP
	 * @return
	 * @throws AMQFactoryException
	 */
	public ConsumerPool getConsumer(String name, MessageType messageType, boolean ifVIP) throws AMQFactoryException{
		SessionPool sessionPool = getConsumerSession(ifVIP);
		Session session = sessionPool.getSession();
		try {
		    //
			Destination ds = null;
			if(messageType.equals(MessageType.Queue)){
				ds = session.createQueue(name);
			}
			if(messageType.equals(MessageType.Topic)){
				ds = session.createTopic(name);
			}
			MessageConsumer consumer = session.createConsumer(ds);
			ConsumerPool consumerPool = new ConsumerPool();
			consumerPool.setConnection(sessionPool.getConnection());
			consumerPool.setSession(session);
			consumerPool.setConsumer(consumer);
			consumerIncreament(sessionPool);
			return consumerPool;
		} catch (JMSException e) {
			throw new AMQFactoryException("getConsumer.JMSException",e);
		}
	}
	
	/**
	 * 生产者自增检查
	 * @param sessionPool
	 */
	private void producerIncreament(SessionPool sessionPool){
		if(sessionPool!=null){
			for(SessionPool sePool : pooledSession){
				if(sePool==sessionPool){
					int cnt = sePool.getAvailableProducer();
					cnt++;
					sePool.setAvailableProducer(cnt);
				}
			}
		}
	}
	
	/**
	 * 减少生产者
	 * @param producerPool
	 */
	public void producerDecreament(ProducerPool producerPool){
		if(producerPool!=null){
			for(SessionPool sessionPool : pooledSession){

			    //如果会话和生产者的连接是同一个
				if(sessionPool.getConnection() == producerPool.getConnection()
						&& sessionPool.getSession() == producerPool.getSession()){
					int cnt = sessionPool.getAvailableProducer();
					cnt--;
					//更新有效生产者个数
					sessionPool.setAvailableProducer(cnt);
				}
			}
		}
	}
	
	/**
	 * 检查当前consumerPool，并修改有效的消费者数量
	 * @param sessionPool
	 */
	private void consumerIncreament(SessionPool sessionPool){
		if(sessionPool!=null){
			for(SessionPool sePool : pooledSession){
			    //
				if(sePool==sessionPool){
					int cnt = sePool.getAvailableConsumer();
					cnt++;
					sePool.setAvailableConsumer(cnt);
				}
			}
		}
	}
	
	/**
	 * 检查当前consumerPool，并修改有效的消费者数量
	 * @param consumerPool
	 */
	public void consumerDecreament(ConsumerPool consumerPool){
		if(consumerPool!=null){
			for(SessionPool sessionPool : pooledSession){
				if(sessionPool.getConnection()==consumerPool.getConnection() 
						&& sessionPool.getSession()==consumerPool.getSession()){
					int cnt = sessionPool.getAvailableConsumer();
					cnt--;
					sessionPool.setAvailableConsumer(cnt);
				}
			}
		}
	}
	
	/**
	 * 销毁所有连接
	 * @return
	 * @throws AMQFactoryException
	 */
	public boolean disposeAll() throws AMQFactoryException{
		try {
			if(pooledSession!=null && pooledSession.size()>0){
				for (SessionPool sessionPool : pooledSession) {
					sessionPool.getSession().close();
				}
				pooledSession.clear();
			}
			if(pooledConnection!=null && pooledConnection.size()>0){
				for(ConnectionPool connectionPool : pooledConnection){
					connectionPool.getConnection().stop();
					connectionPool.getConnection().close();
				}
				pooledConnection.clear();
			}
			return true;
		} catch (JMSException e) {
			throw new AMQFactoryException("disposeAll.JMSException",e);
		}
	}
	
	/**
     * 销毁传入的 producer
	 * @param producer
	 * @throws AMQFactoryException
	 */
	public void disposeProducer(MessageProducer producer) throws AMQFactoryException{
		if(producer!=null){
			try {
				producer.close();
			} catch (JMSException e) {
				throw new AMQFactoryException("disposeProducer.JMSException",e);
			}
		}
	}
	
	/**
	 * 销毁传入的consumer
	 * @param consumer
	 * @throws AMQFactoryException
	 */
	public void disposeConsumer(MessageConsumer consumer) throws AMQFactoryException{
		if(consumer!=null){
			try {
				consumer.close();
			} catch (JMSException e) {
				throw new AMQFactoryException("disposeConsumer.close.JMSException",e);
			}
		}
	}
	
}
