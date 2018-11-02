package priv.sarom.ldap4Netty.ldap.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.directory.api.ldap.codec.osgi.DefaultLdapCodecService;
import org.apache.directory.api.ldap.model.message.BindRequest;
import org.apache.directory.api.ldap.model.message.LdapResult;
import org.apache.directory.api.ldap.model.message.Message;
import org.apache.directory.api.ldap.model.message.MessageTypeEnum;
import org.apache.directory.api.ldap.model.message.Request;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.ResultResponse;
import org.apache.directory.api.ldap.model.message.ResultResponseRequest;
import priv.sarom.ldap4Netty.ldap.entity.LDAPClient;

import javax.swing.event.ChangeListener;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 说明:
 *
 * @author: cxy, 2018/11/2
 */

@Sharable
public class LDAPBindHandler extends ChannelInboundHandlerAdapter {

    DefaultLdapCodecService ldapCodecService = new DefaultLdapCodecService();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        Message message= (Message)msg;
        Request request = (Request) message;

        if (request.getType() != MessageTypeEnum.BIND_REQUEST) {
            //call the next handler
            ctx.fireChannelRead(msg);
            return;
        }

        //bind data , create the ldap session
        BindRequest bindRequest = (BindRequest) request;
        LdapResult result = bindRequest.getResultResponse().getLdapResult();

        String ip = ctx.channel().remoteAddress().toString();
        String account = bindRequest.getName();
        String pwd = new String(bindRequest.getCredentials(), StandardCharsets.UTF_8);

        //match the client
        LDAPClient client = LDAPClient.builder()
                .account(account)
                .password(pwd)
                .ip(ip)
                .status((byte) 1)
                .build();

        //another business logical process
        result.setResultCode(ResultCodeEnum.SUCCESS);
        result.setDiagnosticMessage("OK");
        result.setMatchedDn(bindRequest.getDn());

        ctx.write(bindRequest.getResultResponse());

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
