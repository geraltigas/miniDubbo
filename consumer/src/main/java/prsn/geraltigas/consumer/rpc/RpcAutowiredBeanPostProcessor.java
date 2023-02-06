package prsn.geraltigas.consumer.rpc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import prsn.geraltigas.rpc.annotation.RpcAutowired;

import java.lang.reflect.Field;
import java.util.logging.Logger;

@Component
public class RpcAutowiredBeanPostProcessor implements BeanPostProcessor {

    @Autowired
    private ServiceProxy serviceProxy;
    Logger logger = Logger.getLogger("RpcAutowiredBeanPostProcessor");

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            RpcAutowired annotation = field.getAnnotation(RpcAutowired.class);
            if (annotation != null) {
                Class<?> type = field.getType();
                Object proxy = serviceProxy.getServiceProxy(type);
                field.setAccessible(true);
                try {
                    field.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    logger.warning("RpcAutowiredBeanPostProcessor postProcessAfterInitialization error, " + e.getMessage());
                }
            }
        }
        return bean;
    }
}
