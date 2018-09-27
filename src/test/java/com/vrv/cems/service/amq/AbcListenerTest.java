package com.vrv.cems.service.amq;

import com.vrv.cems.service.amq.common.AbsListener;

import java.util.Map;

/**
 * <B>说       明</B>:服务区域变更辅助类。
 *
 * @author 作  者  名：陈  锐<br/>
 * E-mail ：chenming@vrvmail.com.cn
 * @version 版   本  号：1.0.0 <br/>
 * 创建时间 17:13
 */
public class AbcListenerTest extends AbsListener {


    @Override
    public void dealWithMsgMapValue(Map<String, Object> dicMap) {
        System.out.println("listener 1:dicMap->"+dicMap.toString());
    }

    @Override
    public void dealWithMsgTextValue(String text) {
        System.out.println("listener 1:text->"+text);
    }
}
