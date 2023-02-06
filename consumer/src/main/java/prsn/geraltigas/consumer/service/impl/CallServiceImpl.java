package prsn.geraltigas.consumer.service.impl;

import org.springframework.stereotype.Service;
import prsn.geraltigas.common.service.TestService;
import prsn.geraltigas.consumer.service.CallService;
import prsn.geraltigas.rpc.annotation.RpcAutowired;

import java.util.logging.Logger;

@Service
public class CallServiceImpl implements CallService {
    @RpcAutowired
    private TestService testService;
    Logger logger = Logger.getLogger(CallServiceImpl.class.getName());
    @Override
    public void callRpc() {
        logger.info(testService.test("Geraltigas"));
    }
}
