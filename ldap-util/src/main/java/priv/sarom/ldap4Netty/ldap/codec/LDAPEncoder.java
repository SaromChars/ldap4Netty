package priv.sarom.ldap4Netty.ldap.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.ldap.model.message.Message;

/**
 * 说明:
 *
 * @author: cxy, 2018/11/2
 */
@Sharable
@Slf4j
public class LDAPEncoder extends MessageToByteEncoder {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {

        log.info("enter the LDAPEncoder.....");
        log.info("---------------------------thread:" + Thread.currentThread().getName());

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


        log.info(String.valueOf(System.nanoTime()));

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
