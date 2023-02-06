package prsn.geraltigas.rpc.format;

import lombok.Data;

@Data
public class RpcRequest {
    String requestId;
    String interfaceName;
    String methodName;
    Class<?>[] parameterTypes;
    Object[] parameters;
}
