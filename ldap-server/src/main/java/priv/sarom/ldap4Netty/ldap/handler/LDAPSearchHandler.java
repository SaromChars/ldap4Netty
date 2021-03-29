package priv.sarom.ldap4Netty.ldap.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.ldap.model.entry.DefaultAttribute;
import org.apache.directory.api.ldap.model.message.LdapResult;
import org.apache.directory.api.ldap.model.message.MessageTypeEnum;
import org.apache.directory.api.ldap.model.message.Request;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.SearchResultDoneImpl;
import org.apache.directory.api.ldap.model.message.SearchResultEntryImpl;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import priv.sarom.ldap4Netty.ldap.entity.LDAPSession;

import java.util.Map;

/**
 * @descriptions:
 * @date: 2018/11/4
 * @author: SaromChars
 */
@Sharable
@Slf4j
public class LDAPSearchHandler extends ChannelInboundHandlerAdapter {

    private Map<String, LDAPSession> ldapSessionMap;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        log.info("enter the LDAPSearchHandler.......");
        log.info("---------------------------thread:" + Thread.currentThread().getName());

        Request request = (Request) msg;
        SearchRequest req = (SearchRequest) request;

        if (request.getType() != MessageTypeEnum.SEARCH_REQUEST) {
            //call the next handler
            ctx.fireChannelRead(msg);
            return;
        }

        log.info("------------------------------------MessageId:" + req.getMessageId());
        log.info("------------------------------------Base:" + req.getBase());
        log.info("------------------------------------Filter:" + req.getFilter().toString());
        log.info("------------------------------------Scope:" + req.getScope().name());

        SearchResultEntryImpl resultEntry = new SearchResultEntryImpl();
        //返回用户相关数据
        //可根据filter以及当前scope实现匹配
        resultEntry.setObjectName(new Dn(new Rdn("cn=test")));
        resultEntry.getEntry()
                /*.add(new DefaultAttribute("supportedControl",
                        ProxiedAuthz.OID,
                        ManageDsaIT.OID,
                        Subentries.OID,
                        PagedResults.OID))
                .add(new DefaultAttribute("supportedLDAPVersion", "3"))
                .add(new DefaultAttribute("supportedExtension",
                        StartTlsRequest.OID,
                        PasswordModifyRequest.EXTENSION_OID,
                        WhoAmIRequest.EXTENSION_OID,
                        CancelRequest.EXTENSION_OID))
                .add(new DefaultAttribute("supportedFeatures",
                        SchemaConstants.FEATURE_MODIFY_INCREMENT,
                        SchemaConstants.FEATURE_ALL_OPERATIONAL_ATTRIBUTES
                        ))
                .add(new DefaultAttribute("entryDN"))
                .add(new DefaultAttribute("monitorContext", "cn=monitor"))
                .add(new DefaultAttribute("configContext", "cn=config"))
                .add(new DefaultAttribute("structuralObjectClass", "OpenLDAProotDSE"))
                .add(new DefaultAttribute("objectClass", "top", "OpenLDAProotDSE"))*/
                //component
                .add(new DefaultAttribute("namingContexts", "dc=test"));
        resultEntry.setMessageId(req.getMessageId());
        writeWithProtected(ctx, resultEntry);

        SearchResultDoneImpl resultResponse = (SearchResultDoneImpl) req.getResultResponse();
        LdapResult result = resultResponse.getLdapResult();

        resultResponse.setMessageId(req.getMessageId());
        result.setResultCode(ResultCodeEnum.SUCCESS);

        writeWithProtected(ctx, resultResponse);

    }

    private void writeWithProtected(ChannelHandlerContext ctx, Object obj) {
        if (ctx.channel().isActive() && ctx.channel().isWritable()) {
            ctx.channel().writeAndFlush(obj);
        } else {
            log.error("the buffer is full");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage(), cause);
        ctx.close();
    }

    public LDAPSearchHandler(Map<String, LDAPSession> ldapSessionMap) throws Exception {
        if (ldapSessionMap == null) {
            throw new Exception("the ldapSessionMap is not be null");
        }
        this.ldapSessionMap = ldapSessionMap;
    }
}
