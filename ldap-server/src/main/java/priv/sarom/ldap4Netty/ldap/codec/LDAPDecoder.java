package priv.sarom.ldap4Netty.ldap.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.codec.api.LdapDecoder;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.api.MessageDecorator;
import org.apache.directory.api.ldap.codec.osgi.DefaultLdapCodecService;
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

    //the cache data about recived
    private static byte[] byteBuffer;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

        LOGGER.info("start time:" + String.valueOf(System.nanoTime()));

        LdapApiService ldapCodecService = new DefaultLdapCodecService();
        LdapDecoder ldapDecoder = new LdapDecoder();

        LdapMessageContainer container = new LdapMessageContainer<MessageDecorator<? extends Message>>(ldapCodecService);

        //the first reading, begin the data head
        int size = byteBuf.readableBytes();

        ByteArrayOutputStream bos = null;
        InputStream bis = null;
        try {
            bos = new ByteArrayOutputStream();
            while (byteBuf.isReadable()) {
                bos.write(new byte[]{byteBuf.readByte()});
            }

            LOGGER.info("read data: {} bytes", size);

            byte[] bytes = bos.toByteArray();
            if (byteBuffer != null) {
                int originLen = byteBuffer.length;
                byteBuffer = Arrays.copyOf(byteBuffer, originLen + bytes.length);//数组扩容
                System.arraycopy(bytes, 0, byteBuffer, originLen, bytes.length);
            } else {
                byteBuffer = Arrays.copyOf(bytes, bytes.length);
            }
            bis = new ByteArrayInputStream(byteBuffer);

            ldapDecoder.decode(bis, container);
            Message msg = container.getMessage().getDecorated();

            if (msg == null) {
                return;
            }
            list.add(msg);

            //decode success, release the cache
            byteBuffer = null;

            LOGGER.info("unpack success");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
            LOGGER.info("continue to read data more..");
            return;
        } finally {
            bis.close();
            bos.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
