package priv.sarom.ldap4Netty.ldap.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.ldap.model.message.Message;
import priv.sarom.ldap4Netty.ldap.codec.LDAPDecoder;
import priv.sarom.ldap4Netty.ldap.codec.LDAPEncoder;
import priv.sarom.ldap4Netty.ldap.proxy.interceptor.InterceptorChain;

import java.util.Set;

/**
 * Manages one Netty client connection to an upstream LDAP server.
 * Each client-side channel gets its own UpstreamConnector (1:1 model).
 */
@Slf4j
public class UpstreamConnector {

    private final ProxyConfig config;
    private final Channel clientChannel;
    private final InterceptorChain interceptorChain;
    private final Set<Channel> upstreamChannels;
    private volatile Channel upstreamChannel;

    public UpstreamConnector(ProxyConfig config, Channel clientChannel, InterceptorChain interceptorChain) {
        this(config, clientChannel, interceptorChain, null);
    }

    public UpstreamConnector(ProxyConfig config, Channel clientChannel, InterceptorChain interceptorChain,
                             Set<Channel> upstreamChannels) {
        this.config = config;
        this.clientChannel = clientChannel;
        this.interceptorChain = interceptorChain;
        this.upstreamChannels = upstreamChannels;
    }

    public ChannelFuture connect(EventLoopGroup workerGroup) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) config.getConnectTimeoutMillis())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new LDAPEncoder())
                                .addLast(new LDAPDecoder())
                                .addLast(new UpstreamResponseHandler());
                    }
                });

        ChannelFuture future = bootstrap.connect(config.getUpstreamHost(), config.getUpstreamPort());
        future.addListener(f -> {
            if (f.isSuccess()) {
                upstreamChannel = future.channel();
                if (upstreamChannels != null) {
                    upstreamChannels.add(upstreamChannel);
                }
                log.info("connected to upstream {}:{}", config.getUpstreamHost(), config.getUpstreamPort());
            } else {
                log.error("failed to connect upstream {}:{}",
                        config.getUpstreamHost(), config.getUpstreamPort(), f.cause());
                clientChannel.close();
            }
        });
        return future;
    }

    public void forward(Message message) {
        if (upstreamChannel != null && upstreamChannel.isActive()) {
            upstreamChannel.writeAndFlush(message);
        } else {
            log.error("upstream channel not available, closing client channel");
            clientChannel.close();
        }
    }

    public void close() {
        if (upstreamChannel != null) {
            upstreamChannel.close();
            if (upstreamChannels != null) {
                upstreamChannels.remove(upstreamChannel);
            }
        }
    }

    public boolean isActive() {
        return upstreamChannel != null && upstreamChannel.isActive();
    }

    private class UpstreamResponseHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (!clientChannel.isActive()) {
                return;
            }

            if (msg instanceof Message) {
                Message response = interceptorChain.applyResponse(clientChannel, (Message) msg);
                if (response != null) {
                    clientChannel.writeAndFlush(response);
                } else {
                    log.info("response dropped by interceptor, msgId={}", ((Message) msg).getMessageId());
                }
            } else {
                clientChannel.writeAndFlush(msg);
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            log.info("upstream connection closed, closing client channel");
            if (upstreamChannels != null) {
                upstreamChannels.remove(ctx.channel());
            }
            clientChannel.close();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("upstream error", cause);
            ctx.close();
            clientChannel.close();
        }
    }
}
