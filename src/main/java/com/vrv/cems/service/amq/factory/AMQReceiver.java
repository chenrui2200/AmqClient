package com.vrv.cems.service.amq.factory;

import com.vrv.cems.service.amq.common.AbsListener;
import com.vrv.cems.service.amq.common.MessageType;
import com.vrv.cems.service.amq.exception.AMQFactoryException;
import com.vrv.cems.service.amq.exception.AMQReceiverException;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import java.util.concurrent.locks.ReentrantLock;

/**
 * AMQ接受消息管理类
 * @author chenrui
 * @version 1.0
 * @updated 08-七月-2016 16:00:21
 */
public class AMQReceiver{

	 private ReentrantLock lock = new ReentrantLock();//互斥锁
     //关联用户订阅消息与用户自定义业务监听类
     public boolean setListener(String topicName, AMQFactory factory, Class<?> className,MessageType messageType) throws AMQReceiverException, AMQFactoryException
     {
         if (null == factory) {
             throw new AMQReceiverException("AMQFactory is null");
         }
         if (null == className) {
             throw new AMQReceiverException("user Listener must be not null");
         }
         if (null == topicName || "".equals(topicName)) {
             throw new AMQReceiverException("parameter topicName must be not null");
         }
         //进入写模式锁定状态，防止误监听
        lock.lock();
         //利用反射机制实例化用户自定义监听类
		try {
			MessageListener rece = (MessageListener) className.newInstance();
			MessageConsumer consumer = factory.getConsumer(topicName,messageType);
			consumer.setMessageListener(rece);
			 return true;
		} catch (InstantiationException e) {
			throw new AMQReceiverException("newInstance listener error!",e);
		} catch (IllegalAccessException e) {
			throw new AMQReceiverException("newInstance listener error!",e);
		} catch (JMSException e) {
			throw new AMQReceiverException("set listener error!",e);
		}finally{
			lock.unlock();
		}
     }

	/**
	 * 关联用户订阅消息与用户自定义业务监听类
	 * @param topicName
	 * @param factory
	 * @param className
	 * @param messageType
	 * @return
	 * @throws AMQReceiverException
	 * @throws AMQFactoryException
	 */
     public boolean setReceive(String topicName, AMQFactory factory, Class<?> className,MessageType messageType) throws AMQReceiverException, AMQFactoryException
	 {
         if (null == factory) {
             throw new AMQReceiverException("AMQFactory is null");
         }
         if (null == className) {
             throw new AMQReceiverException("user Listener must be not null");
         }
         if (null == topicName || "".equals(topicName)) {
             throw new AMQReceiverException("parameter topicName must be not null");
         }
         //进入写模式锁定状态，防止误监听
        lock.lock();
         //利用反射机制实例化用户自定义监听类
		try {
			AbsListener rece = (AbsListener)className.newInstance();
			MessageConsumer consumer = factory.getConsumer(topicName,messageType);
			consumer.setMessageListener(rece);
			return true;
		} catch (InstantiationException e) {
			throw new AMQReceiverException("newInstance listener error!",e);
		} catch (IllegalAccessException e) {
			throw new AMQReceiverException("newInstance listener error!",e);
		} catch (JMSException e) {
			throw new AMQReceiverException("set listener error!",e);
		}finally{
			lock.unlock();
		}
     }
}
