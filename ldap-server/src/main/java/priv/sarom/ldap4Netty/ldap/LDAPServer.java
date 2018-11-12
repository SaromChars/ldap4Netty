package priv.sarom.ldap4Netty.ldap;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.sarom.ldap4Netty.ldap.exception.LDAPException;
import priv.sarom.ldap4Netty.ldap.initializer.OrdinaryInitializer;
import priv.sarom.ldap4Netty.ldap.initializer.SSLInitializer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @descriptions:
 * @date: 2018/11/2
 * @author: SaromChars
 */
public class LDAPServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(LDAPServer.class);

    private Integer port;
    private static final Integer DEFAULT_PORT = 389;

    private List<ChannelHandler> handlers = null;
    private Class decoderClass = null;
    private Class encoderClass = null;

    private static EventLoopGroup bossGroup = null;
    private static EventLoopGroup workerGroup = null;

    private static ChannelInitializer initializer = null;

    public Boolean start() throws LDAPException {

        if (initializer == null) {
            throw new LDAPException("must be init");
        }

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        try {

            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(initializer)
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(this.port).sync();// (7)

            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
        } catch (Exception e) {

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

        return Boolean.TRUE;
    }

    public void stop() throws InterruptedException {

        LOGGER.info("clear the workerGroup");
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }

        LOGGER.info("clear the boosGroup");
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }

        LOGGER.info("success");
    }

    public LDAPServer appendHandler(ChannelHandler handler) {
        if (handlers == null) {
            handlers = new ArrayList<>();
        }
        handlers.add(handler);
        return this;
    }

    public LDAPServer appendDecoder(Class decoderClazz) {
        this.decoderClass = decoderClazz;
        return this;
    }

    public LDAPServer appendEncoder(Class encoderClazz) {
        this.encoderClass = encoderClazz;
        return this;
    }

    public LDAPServer(Integer port) {
        this.port = Optional.ofNullable(port).orElse(DEFAULT_PORT);
        LOGGER.info("target port is {}, the server instance is creating...", port);
    }

    public LDAPServer init(SSLContext sslContext, Map<String,SSLEngine> sslEngineMap) throws LDAPException {
        if (sslContext == null) {
            initializer = new OrdinaryInitializer(decoderClass, encoderClass, handlers);
        } else {
            SSLInitializer sslInitializer = new SSLInitializer(decoderClass, encoderClass, handlers, sslContext, sslEngineMap);

            sslInitializer.setClient(false);
            sslInitializer.setVerifyClient(true);

            initializer = sslInitializer;
        }
        return this;
    }
}
