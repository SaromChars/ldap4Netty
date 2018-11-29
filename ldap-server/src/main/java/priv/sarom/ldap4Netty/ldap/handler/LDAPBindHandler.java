package priv.sarom.ldap4Netty.ldap.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.directory.api.ldap.model.message.BindRequest;
import org.apache.directory.api.ldap.model.message.LdapResult;
import org.apache.directory.api.ldap.model.message.MessageTypeEnum;
import org.apache.directory.api.ldap.model.message.Request;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.sarom.ldap4Netty.ldap.entity.LDAPAccount;
import priv.sarom.ldap4Netty.ldap.entity.LDAPSession;

import javax.net.ssl.SSLEngine;
import javax.security.cert.X509Certificate;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 说明:
 *
 * @author: cxy, 2018/11/2
 */

@Sharable
public class LDAPBindHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LDAPBindHandler.class);
    private final Map<String, SSLEngine> sslEngineMap;

    private Map<String, LDAPSession> ldapSessionMap;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        LOGGER.info("enter the LDAPBindHandler.....");

        String channelId = ctx.channel().id().asLongText();
        LOGGER.info(channelId);

        Request request = (Request) msg;

        if (request.getType() != MessageTypeEnum.BIND_REQUEST) {

            //need to control the session validation

            throw new Exception("......");
            //call the next handler
 /*           ctx.fireChannelRead(msg);
            return;*/
        }

        //bind data , create the ldap session
        BindRequest bindRequest = (BindRequest) request;
        LdapResult result = bindRequest.getResultResponse().getLdapResult();

        //get the information about client from this connnection
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        String account = bindRequest.getName();
        String pwd = new String(bindRequest.getCredentials(), StandardCharsets.UTF_8);

        //get the client cert form sslEngineMap
        SSLEngine sslEngine = sslEngineMap.get(channelId);
        byte[] clientCertData = null;
        if (sslEngine != null) {
            X509Certificate[] peerCertificateChain = sslEngine.getSession().getPeerCertificateChain();
            clientCertData = peerCertificateChain[0].getEncoded();
        }

        LDAPAccount client = LDAPAccount.builder()
                .account(account)
                .password(pwd)
                .ip(ip)
                .status((byte) 1)
                .cert(clientCertData)
                .build();

        //match the client, accessable,
        //else return failure


        //put into the session
        LDAPSession ldapSession = new LDAPSession();
        ldapSession.setAccount(client);
        ldapSession.setRemoteIP(client.getIp());

        ldapSessionMap.put(channelId, ldapSession);

        //another business logical process
        result.setResultCode(ResultCodeEnum.SUCCESS);
        result.setDiagnosticMessage("OK");
        result.setMatchedDn(bindRequest.getDn());

        ctx.channel().writeAndFlush(bindRequest.getResultResponse());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.info("catch the error...................");
//        cause.printStackTrace();
        ctx.close();
    }

    public LDAPBindHandler(Map<String, LDAPSession> ldapSessionMap, Map<String, SSLEngine> sslEngineMap) throws Exception {
        if (ldapSessionMap == null) {
            throw new Exception("the ldapSessionMap is not be null");
        }
        this.ldapSessionMap = ldapSessionMap;
        this.sslEngineMap = sslEngineMap;
    }
}
