package priv.sarom.ldap4Netty.ldap.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.apache.directory.api.ldap.model.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 说明:
 *
 * @author: cxy, 2018/11/9
 */
@ChannelHandler.Sharable
public class LDAPRequestHandler extends ChannelOutboundHandlerAdapter {

    public static final Logger LOGGER = LoggerFactory.getLogger(LDAPRequestHandler.class);

    private List<Message> messages;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        if (messages.size() > 0) {
            Message message = messages.get(0);
            messages.remove(0);
            ctx.writeAndFlush(message);
        } else {
            ctx.close();
        }


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error(cause.getMessage(), cause);
        ctx.close();
    }

    public LDAPRequestHandler(List<Message> messages) {
        this.messages = messages;
    }
}
