package priv.sarom.ldap4Netty.ldap.proxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import priv.sarom.ldap4Netty.ldap.codec.LDAPDecoder;
import priv.sarom.ldap4Netty.ldap.codec.LDAPEncoder;
import priv.sarom.ldap4Netty.ldap.proxy.interceptor.InterceptorChain;
import priv.sarom.ldap4Netty.ldap.proxy.interceptor.ProxyInterceptor;

import io.netty.util.concurrent.EventExecutor;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * LDAP Proxy Server: accepts client connections and forwards all LDAP
 * operations to an upstream LDAP server (1:1 connection model).
 */
@Slf4j
public class LDAPProxyServer {

    private final ProxyConfig config;
    private final InterceptorChain interceptorChain = new InterceptorChain();
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private EventLoopGroup upstreamWorkerGroup;
    private final Set<Channel> upstreamChannels = ConcurrentHashMap.newKeySet();
    private volatile Channel listenChannel;
    private volatile CountDownLatch readyLatch = new CountDownLatch(1);
    private volatile Throwable startupFailure;
    private volatile boolean running;

    public LDAPProxyServer(ProxyConfig config) {
        this.config = config.validate();
    }

    public LDAPProxyServer addInterceptor(ProxyInterceptor interceptor) {
        interceptorChain.addInterceptor(interceptor);
        return this;
    }

    public synchronized void startAsync() {
        if (running) {
            return;
        }
        config.validate();
        readyLatch = new CountDownLatch(1);
        startupFailure = null;
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        upstreamWorkerGroup = new NioEventLoopGroup();

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new LDAPEncoder())
                                .addLast(new LDAPDecoder())
                                .addLast(new ProxyHandler(config, upstreamWorkerGroup, interceptorChain, upstreamChannels));
                    }
                });

        log.info("LDAP Proxy starting on port {}, upstream={}:{}",
                config.getListenPort(), config.getUpstreamHost(), config.getUpstreamPort());

        b.bind(config.getListenPort()).addListener(f -> {
            if (f.isSuccess()) {
                listenChannel = ((ChannelFuture) f).channel();
                running = true;
                readyLatch.countDown();
                log.info("LDAP Proxy started successfully");
            } else {
                startupFailure = f.cause();
                readyLatch.countDown();
                log.error("LDAP Proxy failed to start", f.cause());
                stop();
            }
        });
    }

    public void start() throws InterruptedException {
        startAsync();
        if (!awaitReady(30, TimeUnit.SECONDS)) {
            throw new IllegalStateException("LDAP Proxy did not become ready within timeout");
        }
        if (startupFailure != null) {
            throw new IllegalStateException("LDAP Proxy failed to start", startupFailure);
        }

        try {
            listenChannel.closeFuture().sync();
        } finally {
            stop();
        }
    }

    public boolean awaitReady(long timeout, TimeUnit unit) throws InterruptedException {
        boolean ready = readyLatch.await(timeout, unit);
        if (startupFailure != null) {
            throw new IllegalStateException("LDAP Proxy failed to start", startupFailure);
        }
        return ready;
    }

    public boolean isRunning() {
        Channel channel = listenChannel;
        return running && channel != null && channel.isActive();
    }

    public synchronized void stop() {
        log.info("shutting down LDAP Proxy...");
        running = false;
        if (listenChannel != null) {
            listenChannel.close().syncUninterruptibly();
            listenChannel = null;
        }
        upstreamChannels.forEach(channel -> channel.close().syncUninterruptibly());
        upstreamChannels.clear();
        if (upstreamWorkerGroup != null) {
            shutdownGroup(upstreamWorkerGroup);
            upstreamWorkerGroup = null;
        }
        if (workerGroup != null) {
            shutdownGroup(workerGroup);
            workerGroup = null;
        }
        if (bossGroup != null) {
            shutdownGroup(bossGroup);
            bossGroup = null;
        }
        log.info("LDAP Proxy stopped");
    }

    private void shutdownGroup(EventLoopGroup group) {
        io.netty.util.concurrent.Future<?> future = group.shutdownGracefully(0, 200, TimeUnit.MILLISECONDS);
        if (!isCurrentEventLoop(group)) {
            boolean terminated = future.awaitUninterruptibly(250, TimeUnit.MILLISECONDS);
            if (!terminated) {
                log.warn("event loop group shutdown did not finish within timeout");
            }
        }
    }

    private boolean isCurrentEventLoop(EventLoopGroup group) {
        for (EventExecutor executor : group) {
            if (executor.inEventLoop()) {
                return true;
            }
        }
        return false;
    }
}
