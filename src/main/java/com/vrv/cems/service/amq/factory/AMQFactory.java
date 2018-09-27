package com.vrv.cems.service.amq.factory;

import com.vrv.cems.service.amq.common.MessageType;
import com.vrv.cems.service.amq.common.PoolConfig;
import com.vrv.cems.service.amq.exception.AMQFactoryException;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * AM工厂创建模式
 *
 * @author chenrui
 * @version 1.0
 */
public class AMQFactory {

    private ConnectionFactory factory;
    private Connection connection;
    private Session producerSession;
    private Session consumerSession;
    boolean ifCreateP = false;
    boolean ifCreateC = false;
    private String URL = "tcp://127.0.0.1:61616";
    private String userName = "admin";
    private String password = "admin";
    //
    private Map<String, MessageProducer> producerPool = new HashMap<String, MessageProducer>();

    private Map<String, MessageConsumer> consumerPool = new HashMap<String, MessageConsumer>();

    public AMQFactory(PoolConfig poolConfig) throws AMQFactoryException {
        loadConfig(poolConfig);
        initFactory();
    }

    public AMQFactory() throws AMQFactoryException {
        initFactory();
    }

    private void loadConfig(PoolConfig poolConfig) {

        URL = poolConfig.getURL();
        userName = poolConfig.getUserName();
        password = poolConfig.getPassword();
    }

    private void initFactory() throws AMQFactoryException {
        try {
            factory = new ActiveMQConnectionFactory(URL);
            connection = factory.createConnection(userName, password);
            connection.start();
        } catch (Exception e) {
            throw new AMQFactoryException("ActiveMQ Connectioned is failed", e);
        }
    }

    /**
     * ampMessage
     *
     * @return
     * @throws AMQFactoryException
     */
    public MapMessage getMapMessage() throws AMQFactoryException {
        if (null == producerSession) {
            throw new AMQFactoryException("producerSession is null");
        }
        try {
            return producerSession.createMapMessage();
        } catch (JMSException e) {
            throw new AMQFactoryException("createMapMessage error!", e);
        }
    }

    /**
     * streamMessage
     *
     * @return
     * @throws AMQFactoryException
     */
    public StreamMessage getStreamMessage() throws AMQFactoryException {
        if (null == producerSession) {
            throw new AMQFactoryException("producerSession is null");
        }
        try {
            return producerSession.createStreamMessage();
        } catch (JMSException e) {
            throw new AMQFactoryException("createStreamMessage error!", e);
        }
    }

    /**
     * TextMessage
     *
     * @return
     * @throws AMQFactoryException
     */
    public TextMessage getTextMessage() throws AMQFactoryException {
        if (null == producerSession) {
            throw new AMQFactoryException("producerSession is null");
        }
        try {
            return producerSession.createTextMessage();
        } catch (JMSException e) {
            throw new AMQFactoryException("createTextMessage error!", e);
        }
    }


    /**
     * 根据名称和消息类型获取生产者
     *
     * @param topicName
     * @return
     * @throws AMQFactoryException
     */
    public MessageProducer getProducer(String topicName, MessageType messageType) throws AMQFactoryException {
        if (!ifCreateP) {
            try {
                producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                ifCreateP = true;
            } catch (JMSException e) {
                throw new AMQFactoryException("create AMQ session error!", e);
            }
        }
        MessageProducer producer = null;
        if (producerPool.size() > 0) {
            for (Iterator<String> it = producerPool.keySet().iterator(); it.hasNext(); ) {
                String key = it.next();
                if (topicName.equals(key)) {
                    producer = producerPool.get(key);
                    break;
                }
            }
        }
        if (producer == null) {
            try {
                Destination destination = null;
                if (MessageType.Topic.equals(messageType)) {
                    destination = producerSession.createTopic(topicName);
                }
                if (MessageType.Queue.equals(messageType)) {
                    destination = producerSession.createQueue(topicName);
                }
                producer = producerSession.createProducer(destination);

                producer.setDeliveryMode(DeliveryMode.PERSISTENT);
                if (!producerPool.containsKey(topicName)) {
                    producerPool.put(topicName, producer);
                }
            } catch (JMSException e) {
                throw new AMQFactoryException("create AMQ producer error!", e);
            }
        }
        return producer;
    }

    /**
     * 根据名字和消息类型获取消费者
     *
     * @param topicName
     * @return
     * @throws AMQFactoryException
     */
    public MessageConsumer getConsumer(String topicName, MessageType messageType) throws AMQFactoryException {
        if (!ifCreateC) {
            try {
                consumerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            } catch (JMSException e) {
                throw new AMQFactoryException("create AMQ session error!", e);
            }
            ifCreateC = true;
        }
        MessageConsumer consumer = null;
        if (consumerPool.size() > 0) {
            for (Iterator<String> it = consumerPool.keySet().iterator(); it.hasNext(); ) {
                String key = it.next();
                if (topicName.equals(key)) {
                    consumer = consumerPool.get(key);
                    break;
                }
            }
        }
        if (consumer == null) {
            try {
                Destination destination = null;
                if (MessageType.Topic.equals(messageType)) {
                    destination = consumerSession.createTopic(topicName);
                }
                if (MessageType.Queue.equals(messageType)) {
                    destination = consumerSession.createQueue(topicName);
                }
                consumer = consumerSession.createConsumer(destination);
                if (!consumerPool.containsKey(topicName)) {
                    consumerPool.put(topicName, consumer);
                }
            } catch (JMSException e) {
                throw new AMQFactoryException("create AMQ consumer error!", e);
            }
        }
        return consumer;
    }

    /**
     * 销毁所有消费者和生产者和连接
     *
     * @return
     * @throws AMQFactoryException
     */
    public boolean disposeAll() throws AMQFactoryException {
        try {
            if (producerPool.size() > 0) {
                //将MessageProducer都关闭
                for (String key : producerPool.keySet()) {
                    producerPool.get(key).close();
                }
                producerPool.clear();
            }
            if (consumerPool.size() > 0) {
                //将MessageConsumer 都关闭
                for (String key : consumerPool.keySet()) {
                    consumerPool.get(key).close();
                }
                consumerPool.clear();
            }
            if (producerSession != null) {
                producerSession.close();
            }
            if (consumerSession != null) {
                consumerSession.close();
            }
            if (connection != null) {
                connection.close();
            }
            return true;
        } catch (Exception e) {
            throw new AMQFactoryException("Dispose AMQ All Connection Error", e);
        }
    }

    /**
     * 根据名称销毁
     *
     * @param consumerName
     * @return
     * @throws AMQFactoryException
     */
    public boolean disposeConsumerByName(String consumerName) throws AMQFactoryException {
        if (consumerPool.size() > 0) {

            try {
                for (String key : consumerPool.keySet()) {
                    if (key.equals(consumerName)) {
                        consumerPool.get(key).close();
                        consumerPool.remove(key);
                        return true;
                    }
                }
            } catch (JMSException e) {
                throw new AMQFactoryException("Dispose Consumer Error", e);
            }
        }
        return false;
    }
}
