package priv.sarom.ldap4Netty.ldap.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.codec.api.LdapEncoder;
import org.apache.directory.api.ldap.codec.osgi.DefaultLdapCodecService;
import org.apache.directory.api.ldap.model.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

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

        Message responseMessage = (Message) o;

        LdapApiService ldapCodecService = new DefaultLdapCodecService();
        LdapEncoder ldapEncoder = new LdapEncoder(ldapCodecService);

        ByteBuffer byteBuffer = ldapEncoder.encodeMessage(responseMessage);
        byte[] array = byteBuffer.array();

        byteBuf.writeBytes(array);

        LOGGER.info(String.valueOf(System.nanoTime()));

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
