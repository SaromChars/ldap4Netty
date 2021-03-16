package priv.sarom.ldap4Netty.ldap.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.directory.api.ldap.model.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 说明:
 *
 * @author: cxy, 2018/11/2
 */
@Sharable
public class LDAPEncoder extends MessageToByteEncoder{

    private static final Logger LOGGER = LoggerFactory.getLogger(LDAPEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {

        LOGGER.info("enter the LDAPEncoder.....");

        Message responseMessage = (Message) o;
        byte[] array = MyLDAPEncoder.encode2Byte(responseMessage);
        byteBuf.writeBytes(array);

        /**
         * test multi pack
         */
       /* List<Message> responseMessage = (List<Message>) o;
        Message message = responseMessage.get(0);
        byte[] array = MyLDAPEncoder.encode2Byte(message);
        byteBuf.writeBytes(array);

        message = responseMessage.get(1);
        array = MyLDAPEncoder.encode2Byte(message);
        byteBuf.writeBytes(array);*/


        LOGGER.info(String.valueOf(System.nanoTime()));

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
