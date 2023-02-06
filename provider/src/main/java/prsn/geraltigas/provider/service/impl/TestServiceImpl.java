package prsn.geraltigas.provider.service.impl;

import org.springframework.stereotype.Service;
import prsn.geraltigas.common.service.TestService;
import prsn.geraltigas.rpc.annotation.RpcService;

@RpcService
@Service
public class TestServiceImpl implements TestService {
    @Override
    public String test(String name) {
        return "Hello " + name;
    }
}
