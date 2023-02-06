package prsn.geraltigas.consumer.rpc;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

@Component
public class NettyRpcConsumer implements DisposableBean {

    static final String HOST = "localhost";
    static final int PORT = 8888;
    Channel channel = null;
    EventLoopGroup eventLoopGroup = null;
    Logger logger = Logger.getLogger(NettyRpcConsumer.class.getName());

    ExecutorService executorService = Executors.newCachedThreadPool();
    @Autowired
    NettyClientHandler nettyClientHandler;

    @PostConstruct
    public void init() {
        new Thread(this::start).start();
    }

    void start() {
        eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        channel = null;
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(new StringEncoder());
                        ch.pipeline().addLast(nettyClientHandler);
                    }
                });
        try {
            channel = bootstrap.connect(HOST,PORT).sync().channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
            if (channel != null) {
                channel.closeFuture();
            }
            if (eventLoopGroup != null) {
                eventLoopGroup.shutdownGracefully();
            }
        }
    }

    @Override
    public void destroy() {
        if (channel != null) {
            channel.closeFuture();
        }
        eventLoopGroup.shutdownGracefully();
    }

    public String send(String request) throws ExecutionException, InterruptedException {
        logger.info("send begin request: " + request);
        Future<String> future = executorService.submit(() -> {
            nettyClientHandler.setRequest(request);
            return nettyClientHandler.call();
        });
        return future.get();
    }

}
