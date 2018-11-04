package priv.sarom.ldap4Netty.ldap.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.directory.api.ldap.model.message.Message;
import org.apache.directory.api.ldap.model.message.MessageTypeEnum;
import org.apache.directory.api.ldap.model.message.Request;

/**
 * @descriptions:
 * @date: 2018/11/4
 * @author: SaromChars
 */
public class LDAPAbandonHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Message message = (Message) msg;
        Request request = (Request) message;

        if (request.getType() != MessageTypeEnum.ABANDON_REQUEST) {
            //call the next handler
            ctx.fireChannelRead(msg);
        }

        //discard the request
        return;
    }
}
