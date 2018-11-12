package priv.sarom.ldap4Netty.ldap.initializer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.sarom.ldap4Netty.ldap.exception.LDAPException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.util.List;
import java.util.Map;

/**
 * 说明:
 *
 * @author: cxy, 2018/11/12
 */
public class SSLInitializer extends ChannelInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger(SSLInitializer.class);
    private final Map<String, SSLEngine> sslEngineMap;

    private Class decoderClass = null;
    private Class encoderClass = null;
    private List<ChannelHandler> handlers = null;

    private SSLContext sslContext;
    private boolean isClient;
    private boolean verifyClient;

    @Override
    protected void initChannel(Channel channel) throws Exception {

        String channelId = channel.id().asLongText();
        LOGGER.info(channelId);

        SSLEngine sslEngine = sslContext.createSSLEngine();
        sslEngine.setUseClientMode(isClient);
        sslEngine.setNeedClientAuth(verifyClient);

        if (sslEngineMap != null) {
            sslEngineMap.put(channelId, sslEngine);
        }

        ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast(new SslHandler(sslEngine));

        if (encoderClass != null) {
            pipeline.addLast((MessageToByteEncoder) encoderClass.newInstance());
        }

        if (decoderClass != null) {
            pipeline.addLast((ByteToMessageDecoder) decoderClass.newInstance());
        }

        handlers.stream().forEach(channelHandler -> pipeline.addLast(channelHandler));
    }

    public SSLInitializer(Class decoderClass, Class encoderClass, List<ChannelHandler> handlers, SSLContext sslContext, Map<String, SSLEngine> sslEngineMap) throws LDAPException {

        if (decoderClass == null) {
            throw new LDAPException("decoderClass should not be null..");
        }


        if (encoderClass == null) {
            throw new LDAPException("decoderClass should not be null..");
        }

        if (handlers == null || handlers.size() == 0) {
            throw new LDAPException("the handlers should have one element at least...");
        }

        if (sslContext == null) {
            throw new LDAPException("the sslContext should not be null..");
        }

        this.decoderClass = decoderClass;
        this.encoderClass = encoderClass;
        this.handlers = handlers;
        this.sslContext = sslContext;
        this.sslEngineMap = sslEngineMap;
    }

    public boolean isClient() {
        return isClient;
    }

    public void setClient(boolean client) {
        isClient = client;
    }

    public boolean isVerifyClient() {
        return verifyClient;
    }

    public void setVerifyClient(boolean verifyClient) {
        this.verifyClient = verifyClient;
    }
}
