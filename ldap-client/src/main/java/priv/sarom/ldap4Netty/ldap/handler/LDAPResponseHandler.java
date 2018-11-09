package priv.sarom.ldap4Netty.ldap.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.ResultResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 说明:
 *
 * @author: cxy, 2018/11/9
 */
@ChannelHandler.Sharable
public class LDAPResponseHandler extends ChannelInboundHandlerAdapter {

    public static final Logger LOGGER = LoggerFactory.getLogger(LDAPResponseHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        LOGGER.info("enter the LDAPResponseHandler....");

        ResultResponse response = (ResultResponse) msg;
        int messageId = response.getMessageId();
        LOGGER.info("the recived message's id : {}", messageId);
        LOGGER.info("the response is {}",response.getLdapResult().getResultCode().getMessage());
        LOGGER.info(response.getLdapResult().getDiagnosticMessage());

        //makesure the response success
        if (response.getLdapResult().getResultCode().getResultCode() == ResultCodeEnum.SUCCESS.getResultCode()){
            //continue to send
            ctx.channel().writeAndFlush(1);
        }else {
            //close the connecntion
            ctx.close();
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error(cause.getMessage(), cause);
        ctx.close();
    }
}
