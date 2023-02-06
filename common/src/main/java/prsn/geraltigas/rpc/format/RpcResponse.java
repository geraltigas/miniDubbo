package prsn.geraltigas.rpc.format;

import lombok.Data;

@Data
public class RpcResponse {
    Object result;
    String exception;
    String responseId;
}
