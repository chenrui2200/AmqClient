package com.vrv.cems.service.amq.poolFactory;


import com.vrv.cems.service.amq.common.MessageEntity;
import com.vrv.cems.service.amq.common.MessageType;
import com.vrv.cems.service.amq.exception.AMQFactoryException;
import com.vrv.cems.service.amq.exception.AMQReceiverException;
import com.vrv.cems.service.amq.exception.AMQSendException;
import com.vrv.cems.service.amq.poolFactory.pool.ProducerPool;

import javax.jms.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class AMQPoolSender {

    /*定义锁*/
    private  ReentrantLock lock = new ReentrantLock();

    /*定义生产池的map对象*/
    private Map<String, ProducerPool> mapProducer = new HashMap<>();

    /**
     * 发送消息
     * @param amqFactory amq 工厂
     * @param dicMap    消息map
     * @param topicName  订阅主题
     * @param messageType 消息类型
     * @param ifVIP 是否优先生成
     * @param OnUse 是否从内存信息中查找
     * @return
     * @throws AMQSendException
     * @throws AMQFactoryException
     */
    public boolean sendMapMsg(AMQPoolFactory amqFactory, Map<String, Object> dicMap, String topicName, MessageType messageType, boolean ifVIP, boolean OnUse)
            throws AMQSendException, AMQFactoryException {
        if (null == amqFactory) {
            throw new AMQSendException("AMQPoolFactory is null");
        }
        if (dicMap.size() == 0) {
            throw new AMQSendException("Message Content must be not null");
        }
        if (null == topicName || "".equals(topicName)) {
            throw new AMQSendException("topicName must be not null");
        }

        lock.lock();


        ProducerPool producerPool = null;
        if (OnUse) {
            //在已经放入内存中的生产中寻找
            if (mapProducer.containsKey(topicName)) {
                producerPool = mapProducer.get(topicName);
            }
        }
        if (producerPool == null) {
            //如果producerPool依然为null
            producerPool = amqFactory.getProducer(topicName, messageType, ifVIP);
        }
        Session session = producerPool.getSession();
        MessageProducer producer = producerPool.getProducer();
        try {
            MapMessage mapMsg = session.createMapMessage();

            Iterator<Map.Entry<String, Object>> it = dicMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Object> entry = it.next();
                mapMsg.setObject(entry.getKey(),entry.getValue());
                producer.send(mapMsg);
                mapMsg.clearBody();
            }

            return true;
        } catch (Exception e) {
            amqFactory.disposeProducer(producer);
            amqFactory.producerDecreament(producerPool);
            throw new AMQSendException("send Map Message Error", e);
        } finally {
            if (!OnUse) {
                amqFactory.disposeProducer(producer);
                amqFactory.producerDecreament(producerPool);
            } else {
                mapProducer.put(topicName, producerPool);
            }
            lock.unlock();
        }
    }

    /**
     * 通过 MessageEntity发送map类型的消息
     *
     * @param amqFactory
     * @param entity
     * @param topicName
     * @param messageType
     * @param ifVIP
     * @param OnUse
     * @return
     * @throws AMQSendException
     * @throws AMQFactoryException
     */
    public boolean sendMapMsg(AMQPoolFactory amqFactory, MessageEntity entity, String topicName,
                              MessageType messageType, boolean ifVIP, boolean OnUse)
            throws AMQSendException, AMQFactoryException {
        if (null == amqFactory) {
            throw new AMQSendException("AMQPoolFactory is null");
        }
        if (entity == null) {
            throw new AMQSendException("Message Content must be not null");
        }
        if (null == topicName || "".equals(topicName)) {
            throw new AMQSendException("name must be not null");
        }

        Map<String, Object> headMap = entity.getHeadMap();
        Map<String, Object> bodyMap = entity.getBodyMap();
        ProducerPool producerPool = null;

        //上锁
        lock.lock();
        if (OnUse) {
            if (mapProducer.containsKey(topicName)) {
                producerPool = mapProducer.get(topicName);
            }
        }
        if (producerPool == null) {
            producerPool = amqFactory.getProducer(topicName, messageType, ifVIP);
        }
        Session session = producerPool.getSession();
        MessageProducer producer = producerPool.getProducer();

        try {
            MapMessage mapMsg = session.createMapMessage();
            if (!headMap.isEmpty() && headMap.size() > 0) {
                for (Iterator<String> it = headMap.keySet().iterator(); it.hasNext(); ) {
                    String key = it.next();
                    mapMsg.setObjectProperty(key, headMap.get(key));
                }
            }
            for (Iterator<String> it = bodyMap.keySet().iterator(); it.hasNext(); ) {
                String key = it.next();
                mapMsg.setObject(key,bodyMap.get(key));
            }
            //集合head 和body信息再发送
            producer.send(mapMsg);
            mapMsg.clearBody();
            return true;
        } catch (Exception e) {
            amqFactory.disposeProducer(producer);
            amqFactory.producerDecreament(producerPool);
            throw new AMQSendException("send Map Message Error", e);
        } finally {
            if (!OnUse) {

                amqFactory.disposeProducer(producer);
                amqFactory.producerDecreament(producerPool);
            } else {
                mapProducer.put(topicName, producerPool);
            }
            lock.unlock();
        }
    }

    /**
     * 通过 MessageEntity 发送文本格式信息
     *
     * @param amqFactory
     * @param entity
     * @param topicName
     * @param messageType
     * @param ifVIP
     * @param OnUse
     * @return
     * @throws AMQSendException
     * @throws AMQFactoryException
     */
    public boolean sendTextMsg(AMQPoolFactory amqFactory, MessageEntity entity, String topicName, MessageType messageType, boolean ifVIP, boolean OnUse)
            throws AMQSendException, AMQFactoryException {
        if (null == amqFactory) {
            throw new AMQSendException("AMQFactory is null");
        }
        if (null == entity) {
            throw new AMQSendException("Message Content must be not null");
        }
        if (null == topicName || "".equals(topicName)) {
            throw new AMQSendException("parameter name must be not null");
        }
        //加锁
        lock.lock();
        Map<String, Object> headMap = entity.getHeadMap();
        Map<String, Object> bodyMap = entity.getBodyMap();

        ProducerPool producerPool = null;
        if (OnUse) {
            if (mapProducer.containsKey(topicName)) {
                producerPool = mapProducer.get(topicName);
            }
        }
        if (producerPool == null) {
            producerPool = amqFactory.getProducer(topicName, messageType, ifVIP);
        }
        Session session = producerPool.getSession();
        MessageProducer producer = producerPool.getProducer();
        try {
            TextMessage textMsg = session.createTextMessage();
            if (!headMap.isEmpty() && headMap.size() > 0) {
                for (Iterator<String> it = headMap.keySet().iterator(); it.hasNext(); ) {
                    String key = it.next();
                    textMsg.setObjectProperty(key, headMap.get(key));
                }
            }
            for (Iterator<String> it = bodyMap.keySet().iterator(); it.hasNext(); ) {
                String key = it.next();
                textMsg.setText(String.valueOf(bodyMap.get(key)));
            }
            //集合head 和body信息再发送
            producer.send(textMsg);
            return true;
        } catch (Exception e) {
            amqFactory.disposeProducer(producer);
            amqFactory.producerDecreament(producerPool);
            throw new AMQSendException("send text Message error", e);
        } finally {
            if (!OnUse) {
                amqFactory.disposeProducer(producer);
                amqFactory.producerDecreament(producerPool);
            } else {
                mapProducer.put(topicName, producerPool);
            }
            lock.unlock();
        }
    }

    /**
     * 通过 text 字符串发送文本信息
     *
     * @param amqFactory amq工厂
     * @param text       文本字符串
     * @param topicName  订阅主题
     * @return
     * @throws AMQSendException
     * @throws AMQFactoryException
     */
    public boolean sendTextMsg(AMQPoolFactory amqFactory, String text, String topicName, MessageType messageType, boolean ifVIP, boolean OnUse)
            throws AMQSendException, AMQFactoryException {
        if (null == amqFactory) {
            throw new AMQSendException("AMQFactory is null");
        }
        if (null == text || "".equals(text)) {
            throw new AMQSendException("Message Content must be not null");
        }
        if (null == topicName || "".equals(topicName)) {
            throw new AMQSendException("parameter name must be not null");
        }
        //上锁
        lock.lock();
        ProducerPool producerPool = null;
        if (OnUse) {
            if (mapProducer.containsKey(topicName)) {
                producerPool = mapProducer.get(topicName);
            }
        }
        if (producerPool == null) {
            producerPool = amqFactory.getProducer(topicName, messageType, ifVIP);
        }
        Session session = producerPool.getSession();
        MessageProducer producer = producerPool.getProducer();
        try {
            TextMessage textMsg = session.createTextMessage();
            textMsg.setText(text);
            producer.send(textMsg);
            return true;
        } catch (Exception e) {
            amqFactory.disposeProducer(producer);
            amqFactory.producerDecreament(producerPool);
            throw new AMQSendException("send text Message error", e);
        } finally {
            if (!OnUse) {
                amqFactory.disposeProducer(producer);
                amqFactory.producerDecreament(producerPool);
            } else {
                mapProducer.put(topicName, producerPool);
            }
            lock.unlock();
        }
    }

    /**
     * 销毁所有生产者
     *
     * @return
     * @throws AMQSendException
     */
    public boolean disposeAllProducer() throws AMQSendException {
        if (mapProducer.size() > 0) {
            try {
                for (Iterator<String> it = mapProducer.keySet().iterator(); it.hasNext(); ) {
                    String key = it.next();
                    MessageProducer producer = mapProducer.get(key).getProducer();
                    producer.close();
                }
                mapProducer.clear();
            } catch (JMSException e) {
                throw new AMQSendException("disposeAllProducer.JMSException", e);
            }
        }
        return true;
    }

    /**
     * 根据名称销毁生产者
     * @param name
     * @return
     * @throws AMQReceiverException
     */
    public boolean disposeProducer(AMQPoolFactory amqFactory, String name) throws AMQReceiverException {
        if (mapProducer.size() > 0) {
            try {
                if (mapProducer.containsKey(name)) {
                    ProducerPool producerPool = mapProducer.get(name);
                    producerPool.getProducer().close();
                    amqFactory.producerDecreament(producerPool);
                    mapProducer.remove(name);
                }
            } catch (JMSException e) {
                throw new AMQReceiverException("disposeProducer.JMSException", e);
            }
        }
        return true;
    }
}
