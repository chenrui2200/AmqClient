package com.vrv.cems.service.amq.common;

import java.util.Map;

/**
 * 接收消息体
 * @author chenrui
 * @version 1.0
 */
public class MessageEntity {


	private Map<String,Object> headMap;
	private Map<String,Object> bodyMap;
	public Map<String, Object> getHeadMap() {
		return headMap;
	}
	public void setHeadMap(Map<String, Object> headMap) {
		this.headMap = headMap;
	}
	public Map<String, Object> getBodyMap() {
		return bodyMap;
	}
	public void setBodyMap(Map<String, Object> bodyMap) {
		this.bodyMap = bodyMap;
	}
	
}
