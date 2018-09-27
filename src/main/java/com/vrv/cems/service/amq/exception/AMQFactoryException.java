package com.vrv.cems.service.amq.exception;
/**
 * AMQFactoryException
 * @author chenrui
 * @version 1.0
 */
public class AMQFactoryException extends Exception{
	private static final long serialVersionUID = 1L;

	public AMQFactoryException(String message){
		super(message);
	}
	
	public AMQFactoryException(String message,Exception e){
		super(message,e);
	}
	
}
