package priv.sarom.ldap4Netty.ldap.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.directory.api.ldap.model.message.BindRequest;
import org.apache.directory.api.ldap.model.message.Message;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.ResultResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 说明:
 *
 * @author: cxy, 2018/11/9
 */
public class LDAPOperationHandler extends ChannelInboundHandlerAdapter {

    public static final Logger LOGGER = LoggerFactory.getLogger(LDAPOperationHandler.class);

    private List<Message> messages;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        if (messages == null || messages.size() == 0) {
            LOGGER.error("Operation not null");
            ctx.close();
        }

        Message message = messages.get(0);
        // LDAP bind operation is necessary
        if (!(message instanceof BindRequest)) {
            ctx.close();
            return;
        }

        ctx.writeAndFlush(message);
        System.out.println("send init");
        messages.remove(0);

        //粘包测试
/*        while (messages.size() > 0) {
            ctx.write(messages.get(0));
            messages.remove(0);
            ctx.flush();
        }*/
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        LOGGER.info("enter the LDAPResponseHandler....");

        ResultResponse response = (ResultResponse) msg;
        int messageId = response.getMessageId();
        LOGGER.info("the recived message's id : {}", messageId);
        LOGGER.info("the response is {}", response.getLdapResult().getResultCode().getMessage());
        LOGGER.info(response.getLdapResult().getDiagnosticMessage());

        //makesure the response success
        if (response.getLdapResult().getResultCode().getResultCode() != ResultCodeEnum.SUCCESS.getResultCode()) {
            System.out.println("send error:" + response.getLdapResult().getDiagnosticMessage());
            ctx.close();
            return;
        }

        //continue to send modify operation
        while (true) {
            if (messages.size() == 0) {
                System.out.println("send final");
                return;
            }

            ctx.channel().writeAndFlush(messages.get(0));
            messages.remove(0);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error(cause.getMessage(), cause);
        ctx.close();
    }

    public LDAPOperationHandler(List<Message> messages) {
        this.messages = messages;
    }
}
