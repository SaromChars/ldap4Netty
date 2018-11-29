package priv.sarom.ldap4Netty.ldap.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.directory.api.ldap.model.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * 说明:
 *
 * @author: cxy, 2018/11/2
 */
public class LDAPDecoder extends ByteToMessageDecoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(LDAPDecoder.class);


    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

        LOGGER.info("start time:" + String.valueOf(System.nanoTime()));

        //the first reading, begin the data head
        int size = byteBuf.readableBytes();

        InputStream bis = null;
        try {
            byte[] bytes = byteBuf.hasArray()
                    ? byteBuf.array() :
                    Unpooled.buffer(size).writeBytes(byteBuf).array();
            bis = new ByteArrayInputStream(bytes);

            LOGGER.info("read data: {} bytes", size);


            Message msg = MyLDAPDecoder.decode2Message(bis);

            if (msg == null) {
                return;
            }
            list.add(msg);

            LOGGER.info("unpack success");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info("continue to read data more..");
            return;
        } finally {
            bis.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
