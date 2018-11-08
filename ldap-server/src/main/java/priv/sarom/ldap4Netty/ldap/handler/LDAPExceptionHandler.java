package priv.sarom.ldap4Netty.ldap.handler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.directory.api.ldap.model.message.Message;
import org.apache.directory.api.ldap.model.message.Request;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.ResultResponse;
import org.apache.directory.api.ldap.model.message.ResultResponseRequest;
import org.apache.directory.api.ldap.model.message.extended.NoticeOfDisconnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 说明:
 *
 * @author: cxy, 2018/11/2
 */
@Sharable
public class LDAPExceptionHandler extends ChannelInboundHandlerAdapter {

    public static final Logger LOGGER = LoggerFactory.getLogger(LDAPExceptionHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Request request = (Request) msg;

        //匹配错误的response
        ResultResponse respmsg = null;
        if (request.hasResponse()) {    //initiate
            ResultResponseRequest resultreq = (ResultResponseRequest) request;
            respmsg = resultreq.getResultResponse();
            respmsg.getLdapResult().setResultCode(ResultCodeEnum.UNWILLING_TO_PERFORM);
            respmsg.getLdapResult().setDiagnosticMessage("UNWILLING_TO_PERFORM");

        }

        Message unavailable = NoticeOfDisconnect.UNAVAILABLE;
        ChannelFuture channelFuture = ctx.channel().writeAndFlush(unavailable);
        channelFuture.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error(cause.getMessage(), cause);
        ctx.close();
    }
}
