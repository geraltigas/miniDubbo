package prsn.geraltigas.provider.rpc;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import prsn.geraltigas.rpc.annotation.RpcService;
import prsn.geraltigas.rpc.exception.CallServiceException;
import prsn.geraltigas.rpc.format.RpcRequest;
import prsn.geraltigas.rpc.format.RpcResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


@Component
@ChannelHandler.Sharable
/*
  1. cache the class with @RpcService annotation
  2. receive the request from client
  3. find the class with @RpcService annotation\
  4. invoke the method
  5. return the result
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<String> implements ApplicationContextAware {

    @Autowired
    ObjectMapper objectMapper;

    static Map<String,Object> SERVICE_CACHE = new HashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws JsonProcessingException {
        RpcRequest rpcRequest = objectMapper.readValue(s, RpcRequest.class);
        RpcResponse rpcResponse = new RpcResponse();

        rpcResponse.setResponseId(rpcRequest.getRequestId());

        try {
            rpcResponse.setResult(invoke(rpcRequest));
        } catch (CallServiceException | InvocationTargetException |NoSuchMethodException | IllegalAccessException e) {
            rpcResponse.setException(e.getMessage());
        }finally {
            channelHandlerContext.writeAndFlush(objectMapper.writeValueAsString(rpcResponse));
        }
    }

    /**
     * Scan the class with @RpcService annotation in certain package
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        Map<String, Object> maps = applicationContext.getBeansWithAnnotation(RpcService.class);
        for (Object service : maps.values()) {
            if (service.getClass().getInterfaces().length == 0) {
                throw new RuntimeException("The class with @RpcService annotation must implement an interface");
            }
            String interfaceName = service.getClass().getInterfaces()[0].getName();
            SERVICE_CACHE.put(interfaceName, service);
        }
    }

    /**
     * Invoke the method in the class with @RpcService annotation
     * @param rpcRequest
     * @return the result of the method
     */
    private Object invoke(RpcRequest rpcRequest) throws CallServiceException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        // 1. find the corresponding class with @RpcService annotation
        // 2. invoke the method
        // 3. return the result

        Object serviceBean = SERVICE_CACHE.get(rpcRequest.getInterfaceName());
        if (serviceBean == null) {
            throw new CallServiceException("No such service");
        }
        // use reflection to invoke the method

        Method method = serviceBean.getClass().getDeclaredMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
        return method.invoke(serviceBean, rpcRequest.getParameters());

//        // TODO: why use FastClass?, not the reflection
//        FastClass serviceFastClass = FastClass.create(serviceBean.getClass());
//        FastMethod serviceFastClassMethod = serviceFastClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
//        return serviceFastClassMethod.invoke(serviceBean, rpcRequest.getParameters());
    }
}
