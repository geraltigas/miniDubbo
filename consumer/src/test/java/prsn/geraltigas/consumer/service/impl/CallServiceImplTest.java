package prsn.geraltigas.consumer.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.logging.Logger;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CallServiceImplTest {

    Logger logger = Logger.getLogger(CallServiceImplTest.class.getName());

    @Autowired
    private CallServiceImpl callService;

    @Test
    void callRpc() throws InterruptedException {
        logger.info("Start");
        callService.callRpc();
        sleep(1000);
        logger.info("End");
    }
}