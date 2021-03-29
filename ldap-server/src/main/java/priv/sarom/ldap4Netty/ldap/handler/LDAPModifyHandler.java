package priv.sarom.ldap4Netty.ldap.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.message.LdapResult;
import org.apache.directory.api.ldap.model.message.MessageTypeEnum;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.apache.directory.api.ldap.model.message.Request;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.ResultResponse;
import priv.sarom.ldap4Netty.ldap.entity.LDAPSession;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @descriptions:
 * @date: 2018/11/4
 * @author: SaromChars
 */
@Sharable
@Slf4j
public class LDAPModifyHandler extends ChannelInboundHandlerAdapter {

    private Map<String, LDAPSession> ldapSessionMap;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        log.info("enter the LDAPModifyHandler.......");
        log.info("---------------------------thread:" + Thread.currentThread().getName());

        Request request = (Request) msg;

        if (request.getType() != MessageTypeEnum.MODIFY_REQUEST) {
            //call the next handler
            ctx.fireChannelRead(msg);
            return;
        }

        ModifyRequest req = (ModifyRequest) request;
        ResultResponse resultResponse = req.getResultResponse();
        resultResponse.setMessageId(req.getMessageId());
        LdapResult result = resultResponse.getLdapResult();
        result.setMatchedDn(req.getName());

        //error modify name     not bind name
        String channelId = ctx.channel().id().asLongText();
        if (!ldapSessionMap.containsKey(channelId)) {
            // then clien not bind ,
            result.setResultCode(ResultCodeEnum.INVALID_CREDENTIALS);
            result.setDiagnosticMessage(ResultCodeEnum.INVALID_CREDENTIALS.getMessage());

            ctx.channel().writeAndFlush(resultResponse);
            return;
        }


        log.info("------------------------------------ModifyId:" + req.getMessageId());
        log.info("------------------------------------Modification name:" + req.getName());

        //business logic
        //add new sth.
        for (Modification modification : req.getModifications()) {
            Attribute attribute = modification.getAttribute();
            log.info("---------------------------------- id :" + attribute.getId());
            log.info("---------------------------------- upId :" + attribute.getUpId());
            log.info("---------------------------------- attributeType :" + attribute.getAttributeType());
            log.info(LocalDateTime.now().toString());

        }

        result.setResultCode(ResultCodeEnum.SUCCESS);

        if (ctx.channel().isActive() && ctx.channel().isWritable()) {
            ctx.channel().writeAndFlush(resultResponse);
        } else {
            log.error("the buffer is full");
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage(), cause);
        ctx.close();
    }

    public LDAPModifyHandler(Map<String, LDAPSession> ldapSessionMap) throws Exception {
        if (ldapSessionMap == null) {
            throw new Exception("the ldapSessionMap is not be null");
        }
        this.ldapSessionMap = ldapSessionMap;
    }
}
