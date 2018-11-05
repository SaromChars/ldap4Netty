package priv.sarom.ldap4Netty.ldap.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.directory.api.ldap.model.message.Message;
import org.apache.directory.api.ldap.model.message.Request;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.ResultResponse;
import org.apache.directory.api.ldap.model.message.ResultResponseRequest;
import org.apache.directory.api.ldap.model.message.extended.NoticeOfDisconnect;

/**
 * 说明:
 *
 * @author: cxy, 2018/11/2
 */
@Sharable
public class LDAPExceptionHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Message message = (Message) msg;
        Request request = (Request) message;

        //匹配错误的response
        ResultResponse respmsg = null;
        if (request.hasResponse()) {    //initiate
            ResultResponseRequest resultreq = (ResultResponseRequest) request;
            respmsg = resultreq.getResultResponse();
            respmsg.getLdapResult().setResultCode(ResultCodeEnum.UNWILLING_TO_PERFORM);
            respmsg.getLdapResult().setDiagnosticMessage("UNWILLING_TO_PERFORM");

        }

        Message unavailable = (Message) NoticeOfDisconnect.UNAVAILABLE;
        ctx.writeAndFlush(unavailable);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
