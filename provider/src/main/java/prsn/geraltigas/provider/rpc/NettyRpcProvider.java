package prsn.geraltigas.provider.rpc;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class NettyRpcProvider implements DisposableBean {

    @Autowired
    NettyServerHandler nettyServerHandler;
    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;
    static final String HOST = "localhost";
    static final int PORT = 8888;

    private static final Logger logger = Logger.getLogger(NettyRpcProvider.class.getName());

    @PostConstruct
    void startServer() {
        new Thread(this::start).start();
    }

    public void start() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        channel.pipeline().addLast(new StringDecoder());
                        channel.pipeline().addLast(new StringEncoder());
                        channel.pipeline().addLast(nettyServerHandler);
                    }
                });
        try {
            ChannelFuture channelFuture = serverBootstrap.bind(HOST, PORT).sync();
            logger.info("NettyRpcProviderServer start at " + HOST + ":" + PORT);
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.info("NettyRpcProviderServer start error");
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() throws Exception {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        logger.info("NettyRpcProviderServer destroy");
    }
}
