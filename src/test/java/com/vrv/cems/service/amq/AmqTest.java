package com.vrv.cems.service.amq;

import com.sys.common.test.BaseTest;
import com.vrv.cems.service.amq.common.MessageType;
import com.vrv.cems.service.amq.common.PoolConfig;
import com.vrv.cems.service.amq.exception.AMQFactoryException;
import com.vrv.cems.service.amq.exception.AMQReceiverException;
import org.junit.Before;
import org.junit.Test;

/**
 * <B>说       明</B>:服务区域变更辅助类。
 *
 * @author 作  者  名：陈  锐<br/>
 * E-mail ：chenming@vrvmail.com.cn
 * @version 版   本  号：1.0.0 <br/>
 * 创建时间 16:28
 */
public class AmqTest extends BaseTest {

    static PoolConfig poolConfig = null;

    static{
        poolConfig = new PoolConfig(
                "auto://192.168.133.133:61616",
                "admin",
                "admin",
                10,
                1,
                100,
                10,
                10,
                1,
                10,1);
    }

    /*
    该测试用例用来证明在不使用连接池时
    只有一个负载会收到消息
     */
    @Test
    public  void testGetActiveServices(){
        try {

            //注册两个监听处理
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        AMQManager manager2 = AMQManager.getInstance();
                        manager2.init(poolConfig,false);
                        manager2.setListener("topic1",AbcListenerTest.class,MessageType.Queue);
                    } catch (AMQReceiverException e) {
                        e.printStackTrace();
                    } catch (AMQFactoryException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


            //注册两个监听处理
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        AMQManager manager3 = AMQManager.getInstance();
                        manager3.init(poolConfig,false);
                        manager3.setListener("topic1",AbcListener2Test.class,MessageType.Queue);
                    } catch (AMQReceiverException e) {
                        e.printStackTrace();
                    } catch (AMQFactoryException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            Thread.sleep(1000);


            AMQManager manager = AMQManager.getInstance();
            manager.init(poolConfig,false);
            //测试单生产者发送给多个消费者
            for(int i=0;i<20;i++){
                Thread.sleep(300);
                manager.sendTextMsg("text_"+i,"topic1",MessageType.Queue);
            }




        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
