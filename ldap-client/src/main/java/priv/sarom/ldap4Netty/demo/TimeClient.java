package priv.sarom.ldap4Netty.demo;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.lang3.math.NumberUtils;
import priv.sarom.ldap4Netty.demo.handler.TimeClientHandler;

/**
 * 说明:
 * 实现rdate解析时间戳
 *
 * @author: cxy, 2018/10/30
 */
public class TimeClient {

    public void run(String ip,String port) throws InterruptedException {

        int portNum = NumberUtils.toInt(port);

        EventLoopGroup eventExecutors = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventExecutors)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new TimeClientHandler());
                        }
                    });

            ChannelFuture connectFuture = bootstrap.connect(ip, portNum).sync();
            connectFuture.channel().closeFuture().sync();
        } finally {
            eventExecutors.shutdownGracefully();
        }

    }
}
