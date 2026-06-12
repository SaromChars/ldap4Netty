package priv.sarom.ldap4Netty.ldap.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.ldap.model.message.Message;
import org.apache.directory.api.ldap.model.message.Request;
import priv.sarom.ldap4Netty.ldap.proxy.interceptor.InterceptorChain;

import java.util.Set;

/**
 * Core proxy handler: forwards decoded LDAP messages from the client
 * to an upstream LDAP server via UpstreamConnector, and relays responses
 * back through UpstreamConnector's UpstreamResponseHandler.
 *
 * One ProxyHandler instance per client connection (not @Sharable).
 */
@Slf4j
public class ProxyHandler extends ChannelInboundHandlerAdapter {

    private final ProxyConfig config;
    private final EventLoopGroup upstreamWorkerGroup;
    private final InterceptorChain interceptorChain;
    private final Set<io.netty.channel.Channel> upstreamChannels;
    private UpstreamConnector upstreamConnector;

    public ProxyHandler(ProxyConfig config, EventLoopGroup upstreamWorkerGroup, InterceptorChain interceptorChain) {
        this(config, upstreamWorkerGroup, interceptorChain, null);
    }

    public ProxyHandler(ProxyConfig config, EventLoopGroup upstreamWorkerGroup, InterceptorChain interceptorChain,
                        Set<io.netty.channel.Channel> upstreamChannels) {
        this.config = config;
        this.upstreamWorkerGroup = upstreamWorkerGroup;
        this.interceptorChain = interceptorChain;
        this.upstreamChannels = upstreamChannels;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        interceptorChain.fireConnect(ctx.channel());
        upstreamConnector = new UpstreamConnector(config, ctx.channel(), interceptorChain, upstreamChannels);
        upstreamConnector.connect(upstreamWorkerGroup).addListener(f -> {
            if (!f.isSuccess()) {
                log.error("cannot connect to upstream, closing client {}", ctx.channel().remoteAddress());
                ctx.close();
            }
        });
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof Message)) {
            ctx.fireChannelRead(msg);
            return;
        }

        Message message = (Message) msg;
        log.debug("proxy forwarding {} msgId={} from {}",
                message instanceof Request ? ((Request) message).getType() : "response",
                message.getMessageId(),
                ctx.channel().remoteAddress());

        Message filtered = interceptorChain.applyRequest(ctx.channel(), message);
        if (filtered == null) {
            log.info("request dropped by interceptor, msgId={}", message.getMessageId());
            return;
        }

        if (upstreamConnector == null || !upstreamConnector.isActive()) {
            log.error("upstream not available, closing client {}", ctx.channel().remoteAddress());
            ctx.close();
            return;
        }

        upstreamConnector.forward(filtered);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("client disconnected: {}", ctx.channel().remoteAddress());
        interceptorChain.fireDisconnect(ctx.channel());
        if (upstreamConnector != null) {
            upstreamConnector.close();
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("proxy error for client {}", ctx.channel().remoteAddress(), cause);
        if (upstreamConnector != null) {
            upstreamConnector.close();
        }
        ctx.close();
    }
}
