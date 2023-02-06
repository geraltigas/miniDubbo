package prsn.geraltigas.consumer.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import prsn.geraltigas.rpc.format.RpcRequest;
import prsn.geraltigas.rpc.format.RpcResponse;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Component
public class ServiceProxy {

    Logger logger = Logger.getLogger(ServiceProxy.class.getName());

    @Autowired
    NettyRpcConsumer nettyRpcConsumer;

    @Autowired
    ObjectMapper objectMapper;


    Map<Class<?>, Object> serviceProxyCache = new ConcurrentHashMap<>();

    public Object getServiceProxy(Class clazz) {
        Object service = serviceProxyCache.get(clazz);
        if (service == null) {
            service = Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, (proxy, method, args) -> {
                logger.info("Proxy begin");
                RpcRequest rpcRequest = new RpcRequest();
                rpcRequest.setRequestId(UUID.randomUUID().toString());
                rpcRequest.setInterfaceName(clazz.getName());
                rpcRequest.setMethodName(method.getName());
                rpcRequest.setParameters(args);
                rpcRequest.setParameterTypes(method.getParameterTypes());
                logger.info("rpcRequest init : " + rpcRequest.toString());
                try {
                    logger.info("begin to send");
                    String rpcResponseString = nettyRpcConsumer.send(objectMapper.writeValueAsString(rpcRequest));
                    logger.info("send end");
                    logger.info("rpcResponseString : " + rpcResponseString);
                    RpcResponse rpcResponse = (RpcResponse) objectMapper.readValue(rpcResponseString, RpcResponse.class);
                    if (rpcResponse.getException() != null) {
                        throw new RuntimeException(rpcResponse.getException());
                    }
                    if (rpcResponse.getResult() != null) {
                        return rpcResponse.getResult();
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                return null;
            });
            serviceProxyCache.put(clazz, service);
        }
        return service;
    }
}
