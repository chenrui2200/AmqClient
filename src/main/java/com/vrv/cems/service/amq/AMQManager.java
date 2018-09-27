package com.vrv.cems.service.amq;


import com.vrv.cems.service.amq.common.MessageEntity;
import com.vrv.cems.service.amq.common.MessageType;
import com.vrv.cems.service.amq.common.PoolConfig;
import com.vrv.cems.service.amq.exception.AMQFactoryException;
import com.vrv.cems.service.amq.exception.AMQReceiverException;
import com.vrv.cems.service.amq.exception.AMQSendException;
import com.vrv.cems.service.amq.factory.AMQFactory;
import com.vrv.cems.service.amq.factory.AMQReceiver;
import com.vrv.cems.service.amq.factory.AMQSender;
import com.vrv.cems.service.amq.poolFactory.AMQPoolFactory;
import com.vrv.cems.service.amq.poolFactory.AMQPoolReceiver;
import com.vrv.cems.service.amq.poolFactory.AMQPoolSender;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class AMQManager {

	 private static volatile AMQManager instance = null;
	 private ReentrantLock lock = new ReentrantLock();
     private AMQFactory factory = null;
     private AMQPoolFactory poolFactory = null;
     private AMQReceiver rece = new AMQReceiver();
     private AMQSender sender = new AMQSender();
     private AMQPoolSender poolSender = new AMQPoolSender();
     private AMQPoolReceiver poolReceiver = new AMQPoolReceiver();
     public boolean ifInitalized = false;

     private AMQManager(){};
     
     /**
      * 单例模式创建
      * @return
      */
     public static AMQManager getInstance(){
    	 if(instance==null){
    		 synchronized(AMQManager.class){
    			 if (instance == null){
    				 instance = new AMQManager();
    			 }
    		 }
    	 }
         return instance;
     }
     
    /**
     * 初始化
     * @param poolConfig
     * @param ifUsePool 是否使用链接池
     * @throws AMQFactoryException
     */
     public void init(PoolConfig poolConfig, boolean ifUsePool) throws AMQFactoryException {

    	 lock.lock();
         try{
             if (!ifInitalized){
            	 if(ifUsePool){
            		 poolFactory = new AMQPoolFactory(poolConfig);
                     ifInitalized = true;
            	 }else {
            		 factory = new AMQFactory(poolConfig);
                     ifInitalized = true;
				}
                 
             }
         }catch (AMQFactoryException e){
             throw e;
         }finally{
        	 lock.unlock();
         }
     }

     /**
      * 发送消息
      *
      * @param msgEntity 消息体
      * @param topicName 消息主题
      * @param messageType 消息类型
      * @param ifVIP 优先策略
      * @param OnUse
      * @return
      * @throws AMQSendException
      * @throws AMQFactoryException
      */
     public boolean sendTextMsg(MessageEntity msgEntity, String topicName,String messageType,boolean ifVIP,boolean OnUse) throws AMQSendException, AMQFactoryException {
         return sendTextMsg(msgEntity, topicName ,messageType,ifVIP,OnUse);
     }
     
     /**
      * 发送消息
      * @param dicMap
      * @param topicName
      * @param messageType
      * @param ifVIP
      * @param OnUse
      * @return
      * @throws AMQSendException
      * @throws AMQFactoryException
      */
     public boolean sendMapMsg(Map<String, Object> dicMap, String topicName,String messageType,boolean ifVIP,boolean OnUse) throws AMQSendException, AMQFactoryException
     {
         return this.sendMapMsg(dicMap, topicName,messageType);
     }
     
     /**
      * 发送map
      * @param msgEntity
      * @param topicName 消息主题
      * @param messageType 消息类型
      * @return
      * @throws AMQSendException
      * @throws AMQFactoryException
      */
     public boolean sendMapMsg(MessageEntity msgEntity, String topicName,String messageType) throws AMQSendException, AMQFactoryException
     {
         return sendMapMsg(msgEntity, topicName,messageType);
     }
     
     /**
      * 发送map消息
      * @param dicMap 发送消息
      * @param topic 消息主题
      * @param messageType 消息类型
      * @return
      * @throws AMQSendException
      * @throws AMQFactoryException
      */
     public boolean sendMapMsg(Map<String, Object> dicMap, String topic,String messageType) throws AMQSendException, AMQFactoryException
     {
         return sendMapMsg(dicMap, topic,messageType);
     }
     
    /**
     * 发送消息
     * @param msgEntity
     * @param producerName
     * @param messageType
     * @param ifVIP
     * @param OnUse 是否使用已经存在的生产者
     * @return
     * @throws AMQSendException
     * @throws AMQFactoryException
     */
     public boolean sendMapMsg(MessageEntity msgEntity, String producerName, MessageType messageType, boolean ifVIP, boolean OnUse) throws AMQSendException, AMQFactoryException {
         if (ifInitalized){
    		 return poolSender.sendMapMsg(poolFactory, msgEntity, producerName, messageType, ifVIP, OnUse);
         }else{
             return false;
         }
     }

     
     /**
      *
      * @param text
      * @param topicName
      * @param messageType 消息类型
      * @param ifVIP			
      * @param OnUse
      * @return
      * @throws AMQSendException
      * @throws AMQFactoryException
      */
     public boolean sendTextMsg(String text, String topicName,MessageType messageType,boolean ifVIP,boolean OnUse) throws AMQSendException, AMQFactoryException
     {
         if (ifInitalized){
             return poolSender.sendTextMsg(poolFactory, text, topicName, messageType, ifVIP, OnUse);
         }else{
             return false;
         }
     }

    /**
     * 发送消息
     * @param text
     * @param name
     * @param messageType
     * @return
     * @throws AMQSendException
     * @throws AMQFactoryException
     */
    public boolean sendTextMsg(String text, String name,MessageType messageType) throws AMQSendException, AMQFactoryException
    {
        if (ifInitalized){
            return sender.sendTextMsg(factory, text, name,messageType);
        }else{
            return false;
        }
    }




     
     /**
      * 设置接收信息
      * @param topicName
      * @param cls
      * @param messageType 消息类型
      * @return
      * @throws AMQReceiverException
      * @throws AMQFactoryException
      */
     public boolean setReceive(String topicName, Class<?> cls,MessageType messageType) throws AMQReceiverException, AMQFactoryException{
         if (ifInitalized){
             return rece.setReceive(topicName, factory, cls,messageType);
         }else{
             return false;
         }
     }
     
     /**
      * 设置处理类
      * @param topicName
      * @param cls
      * @return
      * @throws AMQReceiverException
      * @throws AMQFactoryException
      */
     public boolean setListener(String topicName, Class<?> cls,MessageType messageType) throws AMQReceiverException, AMQFactoryException{
         if (ifInitalized){
             return rece.setListener(topicName, factory, cls,messageType);
         }else{
             return false;
         }
     }
     
     /**
      * 设置监听工具类
      * @param topicName
      * @param cls
      * @param messageType
      * @param ifVIP
      * @return
      * @throws AMQReceiverException
      * @throws AMQFactoryException
      */
     public boolean setReceive(String topicName, Class<?> cls, MessageType messageType, boolean ifVIP) throws AMQReceiverException, AMQFactoryException{
         if (ifInitalized){
             return poolReceiver.setReceive(topicName, poolFactory, cls,messageType,ifVIP);
         }else{
             return false;
         }
     }
     
     /**
      * 设置监听者
      * @param topicName
      * @param cls
      * @param messageType
      * @param ifVIP
      * @return
      * @throws AMQReceiverException
      * @throws AMQFactoryException
      */
     public boolean setListener(String topicName, Class<?> cls,MessageType messageType,boolean ifVIP) throws AMQReceiverException, AMQFactoryException{
         if (ifInitalized){
             return poolReceiver.setListener(topicName, poolFactory, cls,messageType,ifVIP);
         }else{
             return false;
         }
     }

     /**
      * 销毁所有对象
      * @return
      * @throws AMQFactoryException
      */
     public boolean disposeAll() throws AMQFactoryException {
        return factory.disposeAll();
     }
     
     public boolean disposePoolAll() throws AMQFactoryException, AMQSendException, AMQReceiverException {
         return  poolSender.disposeAllProducer() && poolReceiver.disposeAllConsumer() && poolFactory.disposeAll();
      }
     
     /**
      * 根据消费者名称进行销毁
      * @param consumerName
      * @return
      * @throws AMQFactoryException
      */
     public boolean disposeConsumerByName(String consumerName) throws AMQFactoryException{
    	 return factory.disposeConsumerByName(consumerName);
     }
     
     /**
      * 根据消费者名称进行销毁
      * @param consumerName
      * @return
      * @throws AMQReceiverException
      */
     public boolean disposePoolConsumerByName(String consumerName) throws  AMQReceiverException{
    	 return poolReceiver.disposeConsumer(poolFactory,consumerName);
     }
     
     /**
      * 根据名称销毁生产者
      * @param producerName
      * @return
      * @throws AMQReceiverException
      */
     public boolean disposePoolProducerByName(String producerName) throws  AMQReceiverException{
    	 return poolSender.disposeProducer(poolFactory,producerName);
     }

}
