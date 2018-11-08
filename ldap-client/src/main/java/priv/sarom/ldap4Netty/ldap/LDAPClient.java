package priv.sarom.ldap4Netty.ldap;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.directory.api.ldap.model.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.sarom.ldap4Netty.ldap.codec.LDAPDecoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @descriptions:
 * @date: 2018/11/9
 * @author: SaromChars
 */
public class LDAPClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(LDAPClient.class);

    private String ip;
    private Integer port;
    private static final Integer DEFAULT_PORT = 389;

    private List<ChannelHandler> handlers = null;
    private Class decoderClass = null;
    private Class encodeClass = null;

    private static EventLoopGroup eventExecutors = null;

    private Channel channel;

    public void start() throws InterruptedException {

        eventExecutors = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventExecutors)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();

                            pipeline.addLast((ChannelHandler) encodeClass.newInstance())
                                    .addLast((ChannelHandler) decoderClass.newInstance());

                            handlers.stream().forEach(handler -> pipeline.addLast(handler));

                        }
                    });

            ChannelFuture connectFuture = bootstrap.connect(ip, port).sync();

            channel = connectFuture.channel();

            channel.closeFuture().sync();
        } finally {
            eventExecutors.shutdownGracefully();
        }

    }

    public void stop() throws InterruptedException {

        LOGGER.info("clear the eventExecutors");
        if (eventExecutors != null) {
            eventExecutors.shutdownGracefully();
        }
        LOGGER.info("success");
    }

    public LDAPClient appendHandler(ChannelHandler handler) {
        if (handlers == null) {
            handlers = new ArrayList<>();
        }
        handlers.add(handler);
        return this;
    }

    public LDAPClient appendDecoder(Class decoderClazz) {
        this.decoderClass = decoderClazz;
        return this;
    }

    public LDAPClient appendEncoder(Class encoderClazz) {
        this.encodeClass = encoderClazz;
        return this;
    }

    public LDAPClient(Integer port) {
        this.port = Optional.ofNullable(port).orElse(DEFAULT_PORT);
        LOGGER.info("target port is {}, the server instance is creating...", port);
    }

    public void writeAndFlush(Message message) {

        //TODO
        channel.writeAndFlush(message).addListener(future -> future.);
    }
}
