package com.vrv.cems.service.amq.exception;
/**
 * AMQ��Ϣ�����Զ����쳣��
 * @author chenrui
 * @version 1.0
 */
public class AMQSendException extends Exception{
	private static final long serialVersionUID = 1L;
	public AMQSendException(String message){
		super(message);
	}
	public AMQSendException(String message,Exception e){
		super(message,e);
	}
}
