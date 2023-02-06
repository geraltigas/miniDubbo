package prsn.geraltigas.consumer.rpc;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.logging.Logger;

@Component
public class NettyClientHandler extends SimpleChannelInboundHandler<String> implements Callable<String> {

    ChannelHandlerContext ctx;

    @Setter
    String request;
    @Getter
    String response;

    Logger logger = Logger.getLogger(NettyClientHandler.class.getName());

    @Override
    protected synchronized void channelRead0(ChannelHandlerContext channelHandlerContext, String s) {
        response = s;
        notify();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        logger.info("channelActive");
    }

    @Override
    public synchronized String call() throws Exception {
        if (ctx == null) {
            logger.info("ctx is null");
            return "";
        }
        logger.info("begin to flush : " + request);
        logger.info("ctx : " + ctx);
        ctx.writeAndFlush(request);
        wait();
        logger.info("response : " + response);
        return response;
    }
}
