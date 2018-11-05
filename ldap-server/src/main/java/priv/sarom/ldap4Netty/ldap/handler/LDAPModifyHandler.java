package priv.sarom.ldap4Netty.ldap.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.directory.api.ldap.model.message.LdapResult;
import org.apache.directory.api.ldap.model.message.MessageTypeEnum;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.apache.directory.api.ldap.model.message.Request;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;

/**
 * @descriptions:
 * @date: 2018/11/4
 * @author: SaromChars
 */
@Sharable
public class LDAPModifyHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Request request = (Request) msg;

        if (request.getType() != MessageTypeEnum.MODIFY_REQUEST) {
            //call the next handler
            ctx.fireChannelRead(msg);
            return;
        }
        ModifyRequest req = (ModifyRequest) request;
        LdapResult result = req.getResultResponse().getLdapResult();
        result.setMatchedDn(req.getName());

        //business logic
        //add new sth.

        result.setResultCode(ResultCodeEnum.SUCCESS);
        result.setDiagnosticMessage("OK");

        ctx.writeAndFlush(req.getResultResponse());

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
