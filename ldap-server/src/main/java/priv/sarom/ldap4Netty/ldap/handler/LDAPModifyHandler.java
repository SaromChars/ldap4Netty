package priv.sarom.ldap4Netty.ldap.handler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.message.LdapResult;
import org.apache.directory.api.ldap.model.message.MessageTypeEnum;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.apache.directory.api.ldap.model.message.Request;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.ResultResponse;
import org.apache.directory.api.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.sarom.ldap4Netty.ldap.entity.LDAPSession;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @descriptions:
 * @date: 2018/11/4
 * @author: SaromChars
 */
@Sharable
public class LDAPModifyHandler extends ChannelInboundHandlerAdapter {

    public static final Logger LOGGER = LoggerFactory.getLogger(LDAPModifyHandler.class);

    private Map<String, LDAPSession> ldapSessionMap;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        LOGGER.info("enter the LDAPModifyHandler.......");

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


        LOGGER.info("------------------------------------ModifyId:" + req.getMessageId());
        LOGGER.info("------------------------------------Modification name:" + req.getName());

        //business logic
        //add new sth.
        for (Modification modification : req.getModifications()) {
            Attribute attribute = modification.getAttribute();
            LOGGER.info("---------------------------------- id :" + attribute.getId());
            LOGGER.info("---------------------------------- upId :" + attribute.getUpId());
            LOGGER.info("---------------------------------- attributeType :" + attribute.getAttributeType());
            LOGGER.info(LocalDateTime.now().toString());

//            byte[] bytes = attribute.getBytes();
//            File file = new File("C:\\Users\\cxy\\Desktop\\" + req.getMessageId() + ".cer");
//            FileUtils.writeByteArrayToFile(file, bytes);
        }

        result.setResultCode(ResultCodeEnum.SUCCESS);

        ctx.channel().writeAndFlush(resultResponse);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error(cause.getMessage(), cause);
        ctx.close();
    }

    public LDAPModifyHandler(Map<String, LDAPSession> ldapSessionMap) throws Exception {
        if(ldapSessionMap == null){
            throw new Exception("the ldapSessionMap is not be null");
        }
        this.ldapSessionMap = ldapSessionMap;
    }
}
