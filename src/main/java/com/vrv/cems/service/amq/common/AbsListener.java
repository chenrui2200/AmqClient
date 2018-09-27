package com.vrv.cems.service.amq.common;

import org.apache.log4j.Logger;

import javax.jms.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 抽象类
 * @author chenrui
 * @version 1.0
 * @updated 08-����-2016 16:00:20
 */
public abstract class AbsListener implements MessageListener {

    private Logger log = Logger.getLogger(AbsListener.class);

	//处理map类型的消息
	public abstract void dealWithMsgMapValue(Map<String, Object> dicMap);
    public abstract void dealWithMsgTextValue(String text);


    @Override
    @SuppressWarnings("unchecked")
	public void onMessage(Message message){
    	 try {
 	        if (message instanceof MapMessage){
 	        	MapMessage mapMsg = (MapMessage) message;
 	            Map<String, Object> dicMap = new HashMap<String, Object>();
 	            
 					for (Enumeration<String> em = mapMsg.getMapNames();em.hasMoreElements();) {
 						String key = em.nextElement();
 					    dicMap.put(key, mapMsg.getObject(key));
 					}
				//子类通过继承dealWithMsgMapValue方法实现业务
 	            this.dealWithMsgMapValue(dicMap);
 	            return;
 	        }else if (message instanceof TextMessage){
 	        	TextMessage textMsg = (TextMessage) message;
 	            String text = textMsg.getText();
				//子类通过继承dealWithMsgTextValue方法实现业务
 	            this.dealWithMsgTextValue(text);
 	            return;
 	        }
         }catch (JMSException e) {
        	 log.error( e.getCause());
 		}
	}
}