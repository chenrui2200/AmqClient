package com.vrv.cems.service.amq.processor;

import com.vrv.cems.service.amq.common.MessageType;
import org.apache.log4j.Logger;
import org.springframework.expression.spel.SpelMessage;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

/**
 * <B>说       明</B>:处理注解。
 *
 * @author 作  者  名：陈  锐<br/>
 * E-mail ：chenming@vrvmail.com.cn
 * @version 版   本  号：1.0.0 <br/>
 * 创建时间 16:40
 */
@SupportedAnnotationTypes({"com.vrv.cems.service.amq.processor.AmqListener"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class MyProcessor extends AbstractProcessor {

    private static final Logger logger = Logger.getLogger(MyProcessor.class);

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        logger.info("MyProcessor is running");
        // 遍历annotations获取annotation类型
        for (TypeElement typeElement : annotations) {
            // 使用roundEnv.getElementsAnnotatedWith获取所有被某一类型注解标注的元素，依次遍历
            for (Element element : roundEnv.getElementsAnnotatedWith(typeElement)) {
                // 在元素上调用接口获取注解值
                String topic = element.getAnnotation(AmqListener.class).topic();
                MessageType messageType = element.getAnnotation(AmqListener.class).messageType();



                // 向当前环境输出warning信息
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "topic = " + topic + ", messageType = " + messageType, element);
            }
        }
        return false;

    }
}
