package priv.sarom.ldap4Netty.ldap.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.directory.api.ldap.model.message.LdapResult;
import org.apache.directory.api.ldap.model.message.Request;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.ResultResponse;
import org.apache.directory.api.ldap.model.message.ResultResponseRequest;
import priv.sarom.ldap4Netty.ldap.entity.LDAPSession;

import java.util.Map;

public class LDAPSessionRequiredHandler extends ChannelInboundHandlerAdapter {

    private final Map<String, LDAPSession> ldapSessionMap;
    private final boolean allowAnonymous;

    public LDAPSessionRequiredHandler(Map<String, LDAPSession> ldapSessionMap, boolean allowAnonymous) {
        this.ldapSessionMap = ldapSessionMap;
        this.allowAnonymous = allowAnonymous;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof Request) || allowAnonymous) {
            ctx.fireChannelRead(msg);
            return;
        }

        Request request = (Request) msg;
        if (!request.hasResponse()) {
            ctx.fireChannelRead(msg);
            return;
        }

        LDAPSession session = ldapSessionMap.get(ctx.channel().id().asLongText());
        if (session != null && !Boolean.TRUE.equals(session.getAnonymous())) {
            ctx.fireChannelRead(msg);
            return;
        }

        ResultResponse response = ((ResultResponseRequest) request).getResultResponse();
        response.setMessageId(request.getMessageId());
        LdapResult result = response.getLdapResult();
        result.setResultCode(ResultCodeEnum.STRONG_AUTH_REQUIRED);
        result.setDiagnosticMessage(ResultCodeEnum.STRONG_AUTH_REQUIRED.getMessage());
        ctx.channel().writeAndFlush(response);
    }
}
