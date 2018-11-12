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

    private Class decoderClass = null;
    private Class encoderClass = null;
    private List<ChannelHandler> handlers = null;

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        if (encoderClass != null) {
            pipeline.addLast((MessageToByteEncoder) encoderClass.newInstance());
        }

        if (decoderClass != null) {
            pipeline.addLast((ByteToMessageDecoder) decoderClass.newInstance());
        }

        handlers.stream().forEach(channelHandler -> pipeline.addLast(channelHandler));
    }

    public OrdinaryInitializer(Class decoderClass, Class encoderClass, List<ChannelHandler> handlers) throws LDAPException {

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
