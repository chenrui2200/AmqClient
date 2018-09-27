package com.vrv.cems.service.amq.exception;
/**
 * AMQ 收取消息异常
 * @author chenrui
 * @version 1.0
 */
public class AMQReceiverException extends Exception{

	private static final long serialVersionUID = 1L;

	public AMQReceiverException(String message){
		super(message);
	}
	public AMQReceiverException(String message,Exception e){
		super(message,e);
	}

}
