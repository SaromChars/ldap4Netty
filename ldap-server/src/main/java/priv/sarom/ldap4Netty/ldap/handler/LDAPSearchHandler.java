package priv.sarom.ldap4Netty.ldap.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.sarom.ldap4Netty.ldap.entity.LDAPSession;

import java.util.Map;

/**
 * @descriptions:
 * @date: 2018/11/4
 * @author: SaromChars
 */
@Sharable
public class LDAPSearchHandler extends ChannelInboundHandlerAdapter {

    public static final Logger LOGGER = LoggerFactory.getLogger(LDAPSearchHandler.class);

    private Map<String, LDAPSession> ldapSessionMap;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        LOGGER.info("enter the LDAPSearchHandler.......");

        Request request = (Request) msg;
        SearchRequest req = (SearchRequest) request;

        if (request.getType() != MessageTypeEnum.SEARCH_REQUEST) {
            //call the next handler
            ctx.fireChannelRead(msg);
            return;
        }

        LOGGER.info("------------------------------------MessageId:" + req.getMessageId());
        LOGGER.info("------------------------------------Base:" + req.getBase());
        LOGGER.info("------------------------------------Filter:" + req.getFilter().toString());
        LOGGER.info("------------------------------------Scope:" + req.getScope().name());

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
        ctx.channel().writeAndFlush(resultEntry);

        SearchResultDoneImpl resultResponse = (SearchResultDoneImpl) req.getResultResponse();
        LdapResult result = resultResponse.getLdapResult();

        resultResponse.setMessageId(req.getMessageId());
        result.setResultCode(ResultCodeEnum.SUCCESS);

        ctx.channel().writeAndFlush(resultResponse);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error(cause.getMessage(), cause);
        ctx.close();
    }

    public LDAPSearchHandler(Map<String, LDAPSession> ldapSessionMap) throws Exception {
        if (ldapSessionMap == null) {
            throw new Exception("the ldapSessionMap is not be null");
        }
        this.ldapSessionMap = ldapSessionMap;
    }
}
