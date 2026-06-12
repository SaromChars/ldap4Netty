package priv.sarom.ldap4Netty.ldap.initializer;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import priv.sarom.ldap4Netty.ldap.exception.LDAPException;

import java.util.List;

/**
 * 说明:
 *
 * @author: cxy, 2018/11/12
 */
public class OrdinaryInitializer extends ChannelInitializer<SocketChannel> {

    private Class<? extends ByteToMessageDecoder> decoderClass = null;
    private Class<? extends MessageToByteEncoder> encoderClass = null;
    private List<ChannelHandler> handlers = null;

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        if (encoderClass != null) {
            pipeline.addLast(encoderClass.newInstance());
        }

        if (decoderClass != null) {
            pipeline.addLast(decoderClass.newInstance());
        }

        for (ChannelHandler handler : handlers) {
            pipeline.addLast(handler);
        }
    }

    public OrdinaryInitializer(Class<? extends ByteToMessageDecoder> decoderClass,
                              Class<? extends MessageToByteEncoder> encoderClass,
                              List<ChannelHandler> handlers) throws LDAPException {

        if (decoderClass == null) {
            throw new LDAPException("decoderClass should not be null..");
        }


        if (encoderClass == null) {
            throw new LDAPException("decoderClass should not be null..");
        }

        if (handlers == null || handlers.size() == 0) {
            throw new LDAPException("the handlers should have one element at least...");
        }

        this.decoderClass = decoderClass;
        this.encoderClass = encoderClass;
        this.handlers = handlers;
    }

}
