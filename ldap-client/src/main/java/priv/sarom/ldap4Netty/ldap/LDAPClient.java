package priv.sarom.ldap4Netty.ldap;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.sarom.ldap4Netty.ldap.exception.LDAPException;
import priv.sarom.ldap4Netty.ldap.initializer.OrdinaryInitializer;
import priv.sarom.ldap4Netty.ldap.initializer.SSLInitializer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private Class encoderClass = null;

    private EventLoopGroup eventExecutors = null;

    private ChannelInitializer initializer = null;

    public void start() throws InterruptedException {

        eventExecutors = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventExecutors)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(initializer);

        System.out.println("want send");
        Channel channel = bootstrap.connect(ip, port).sync().channel();
        ChannelFuture channelFuture = channel.closeFuture().sync();

        this.stop();

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
        this.encoderClass = encoderClazz;
        return this;
    }

    public LDAPClient(String ip, Integer port) {
        this.ip = ip;
        this.port = Optional.ofNullable(port).orElse(DEFAULT_PORT);
        LOGGER.info("target port is {}, the server instance is creating...", port);
    }

    public LDAPClient init(SSLContext sslContext, Map<String, SSLEngine> sslEngineMap) throws LDAPException {
        if (sslContext == null) {
            initializer = new OrdinaryInitializer(decoderClass, encoderClass, handlers);
        } else {
            SSLInitializer sslInitializer = new SSLInitializer(decoderClass, encoderClass, handlers, sslContext, sslEngineMap);

            sslInitializer.setClient(true);
            initializer = sslInitializer;
        }
        return this;
    }
}
