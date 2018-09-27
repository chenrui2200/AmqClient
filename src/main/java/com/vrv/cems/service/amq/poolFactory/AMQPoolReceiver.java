package com.vrv.cems.service.amq.poolFactory;


import com.vrv.cems.service.amq.common.AbsListener;
import com.vrv.cems.service.amq.common.MessageType;
import com.vrv.cems.service.amq.exception.AMQFactoryException;
import com.vrv.cems.service.amq.exception.AMQReceiverException;
import com.vrv.cems.service.amq.poolFactory.pool.ConsumerPool;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class AMQPoolReceiver {

    private ReentrantLock lock = new ReentrantLock();
    private Map<String, ConsumerPool> mapConsumer = new HashMap<String, ConsumerPool>();


    /**
     * 创建监听
     *
     * @param topicName
     * @param factory     工厂
     * @param className   注册实现MessageListener 的类
     * @param messageType 消息对象类型
     * @param ifVIP
     * @return
     * @throws AMQReceiverException
     * @throws AMQFactoryException
     */
    public boolean setListener(String topicName, AMQPoolFactory factory, Class<?> className, MessageType messageType, boolean ifVIP)
            throws AMQReceiverException, AMQFactoryException {
        if (null == factory) {
            throw new AMQReceiverException("AMQFactory is null");
        }
        if (null == className) {
            throw new AMQReceiverException("user Listener must be not null");
        }
        if (null == topicName || "".equals(topicName)) {
            throw new AMQReceiverException("parameter name must be not null");
        }
        //加锁
        lock.lock();
        //根据名称获从工厂中获取消费池
        ConsumerPool consumerPool = factory.getConsumer(topicName, messageType, ifVIP);
        MessageConsumer consumer = null;
        try {
            //创建监听
            MessageListener rece = (MessageListener) className.newInstance();
            consumer = consumerPool.getConsumer();
            mapConsumer.put(topicName, consumerPool);
            consumer.setMessageListener(rece);
            return true;
        } catch (InstantiationException e) {

            //工厂对象销魂消费者
            factory.disposeConsumer(consumer);
            //出现异常检查是否有必要减少消费者
            factory.consumerDecreament(consumerPool);
            throw new AMQReceiverException("newInstance listener error!", e);
        } catch (IllegalAccessException e) {
            //操作同上
            factory.disposeConsumer(consumer);
            factory.consumerDecreament(consumerPool);
            throw new AMQReceiverException("newInstance listener error!", e);
        } catch (JMSException e) {
            //操作同上
            factory.disposeConsumer(consumer);
            factory.consumerDecreament(consumerPool);
            throw new AMQReceiverException("set listener error!", e);
        } finally {
            //释放锁
            lock.unlock();
        }
    }

    /**
     * 将封装AbsListener传入注册到消费处理上
     *
     * @param name
     * @param factory
     * @param className   AbsListener 的实现类
     * @param messageType 消息类型
     * @param ifVIP       优先获得获取sessionPool
     * @return
     * @throws AMQReceiverException
     * @throws AMQFactoryException
     */
    public boolean setReceive(String name, AMQPoolFactory factory, Class<?> className, MessageType messageType, boolean ifVIP)
            throws AMQReceiverException, AMQFactoryException {

        if (null == factory) {
            throw new AMQReceiverException("AMQFactory is null");
        }
        if (null == className) {
            throw new AMQReceiverException("user Listener must be not null");
        }
        if (null == name || "".equals(name)) {
            throw new AMQReceiverException("parameter name must be not null");
        }
        //枷锁
        lock.lock();
        ConsumerPool consumerPool = factory.getConsumer(name, messageType, ifVIP);

        MessageConsumer consumer = null;
        try {
            AbsListener rece = (AbsListener) className.newInstance();
            consumer = consumerPool.getConsumer();
            mapConsumer.put(name, consumerPool);
            consumer.setMessageListener(rece);
            return true;
        } catch (InstantiationException e) {
            factory.disposeConsumer(consumer);
            factory.consumerDecreament(consumerPool);
            throw new AMQReceiverException("newInstance listener error!", e);
        } catch (IllegalAccessException e) {
            factory.disposeConsumer(consumer);
            factory.consumerDecreament(consumerPool);
            throw new AMQReceiverException("newInstance listener error!", e);
        } catch (JMSException e) {
            factory.disposeConsumer(consumer);
            factory.consumerDecreament(consumerPool);
            throw new AMQReceiverException("set listener error!", e);
        } finally {
            lock.unlock();
        }

    }

    /**
     * 销毁所以的消费者
     *
     * @return
     * @throws AMQReceiverException
     */
    public boolean disposeAllConsumer() throws AMQReceiverException {
        if (mapConsumer.size() > 0) {
            try {
                for (Iterator<String> it = mapConsumer.keySet().iterator(); it.hasNext(); ) {
                    String key = it.next();
                    ConsumerPool consumerPool = mapConsumer.get(key);
                    consumerPool.getConsumer().close();
                }
                mapConsumer.clear();
            } catch (JMSException e) {
                throw new AMQReceiverException("AMQReceiverException", e);
            }
        }
        return true;
    }

    /**
     * 销毁所以的消费者
     *
     * @param name
     * @return
     * @throws AMQReceiverException
     */
    public boolean disposeConsumer(AMQPoolFactory poolFactory, String name) throws AMQReceiverException {
        if (mapConsumer.size() > 0) {
            try {
                if (mapConsumer.containsKey(name)) {
                    ConsumerPool consumerPool = mapConsumer.get(name);
                    consumerPool.getConsumer().close();
                    poolFactory.consumerDecreament(consumerPool);//���п���consumer��1
                    mapConsumer.remove(name);
                }
            } catch (JMSException e) {
                throw new AMQReceiverException("disposeConsumer.JMSException", e);
            }
        }
        return true;
    }
}
