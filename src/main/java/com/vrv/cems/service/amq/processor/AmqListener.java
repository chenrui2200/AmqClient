package com.vrv.cems.service.amq.processor;

import com.vrv.cems.service.amq.common.MessageType;

/**
 * <B>说       明</B>:为jin。
 *
 * @author 作  者  名：陈  锐<br/>
 * E-mail ：chenming@vrvmail.com.cn
 * @version 版   本  号：1.0.0 <br/>
 * 创建时间 16:36
 */
public @interface AmqListener {

    String topic();
    MessageType messageType();

}
