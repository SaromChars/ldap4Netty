package priv.sarom.ldap4Netty.ldap.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.ldap.model.message.BindRequest;
import org.apache.directory.api.ldap.model.message.LdapResult;
import org.apache.directory.api.ldap.model.message.MessageTypeEnum;
import org.apache.directory.api.ldap.model.message.Request;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.ResultResponse;
import priv.sarom.ldap4Netty.ldap.auth.AcceptAllAuthenticator;
import priv.sarom.ldap4Netty.ldap.auth.AuthenticationContext;
import priv.sarom.ldap4Netty.ldap.auth.AuthenticationResult;
import priv.sarom.ldap4Netty.ldap.auth.Authenticator;
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
@Slf4j
public class LDAPBindHandler extends ChannelInboundHandlerAdapter {

    private final Map<String, SSLEngine> sslEngineMap;
    private final Map<String, LDAPSession> ldapSessionMap;
    private final Authenticator authenticator;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        String channelId = ctx.channel().id().asLongText();
        log.debug("processing LDAP message, channel: {}", channelId);
        Request request = (Request) msg;
        if (request.getType() != MessageTypeEnum.BIND_REQUEST) {

            //need to control the session validation

            //call the next handler
            ctx.fireChannelRead(msg);
            return;
        }

        log.debug("processing bind request, channel: {}", channelId);

        //bind data , create the ldap session
        BindRequest bindRequest = (BindRequest) request;

        //get the information about client from this connnection
        String ip = resolveRemoteAddress(ctx);
        String account = bindRequest.getName();
        byte[] credentials = bindRequest.getCredentials();
        String pwd = new String(credentials, StandardCharsets.UTF_8);

        //get the client cert form sslEngineMap
        byte[] clientCertData = null;
        if (sslEngineMap != null) {
            SSLEngine sslEngine = sslEngineMap.get(channelId);
            if (sslEngine != null) {
                X509Certificate[] peerCertificateChain = sslEngine.getSession().getPeerCertificateChain();
                clientCertData = peerCertificateChain[0].getEncoded();
            }
        }

        ResultResponse resultResponse = bindRequest.getResultResponse();
        resultResponse.setMessageId(request.getMessageId());
        LdapResult result = resultResponse.getLdapResult();
        result.setMatchedDn(bindRequest.getDn());

        AuthenticationResult authResult = authenticator.authenticate(AuthenticationContext.builder()
                .bindDn(account)
                .credentials(credentials)
                .remoteAddress(ip)
                .clientCertificate(clientCertData)
                .build());

        if (!authResult.isSuccess()) {
            result.setResultCode(ResultCodeEnum.INVALID_CREDENTIALS);
            result.setDiagnosticMessage(authResult.getDiagnosticMessage());
            ctx.channel().writeAndFlush(resultResponse);
            return;
        }

        LDAPAccount client = LDAPAccount.builder()
                .account(account)
                .password(pwd)
                .ip(ip)
                .status((byte) 1)
                .cert(clientCertData)
                .build();

        LDAPSession ldapSession = new LDAPSession();
        ldapSession.setAccount(client);
        ldapSession.setRemoteIP(client.getIp());
        ldapSession.setAnonymous(false);

        ldapSessionMap.put(channelId, ldapSession);

        result.setResultCode(ResultCodeEnum.SUCCESS);
        ctx.channel().writeAndFlush(resultResponse);

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().asLongText();
        LDAPSession removed = ldapSessionMap.remove(channelId);
        if (removed != null) {
            log.info("session removed for channel: {}", channelId);
        }
        if (sslEngineMap != null) {
            sslEngineMap.remove(channelId);
        }
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("LDAPBindHandler error, channel: {}", ctx.channel().id().asLongText(), cause);
        ctx.close();
    }

    public LDAPBindHandler(Map<String, LDAPSession> ldapSessionMap, Map<String, SSLEngine> sslEngineMap) throws Exception {
        this(ldapSessionMap, sslEngineMap, new AcceptAllAuthenticator());
    }

    public LDAPBindHandler(Map<String, LDAPSession> ldapSessionMap, Map<String, SSLEngine> sslEngineMap,
                           Authenticator authenticator) throws Exception {
        if (ldapSessionMap == null) {
            throw new Exception("the ldapSessionMap is not be null");
        }
        this.ldapSessionMap = ldapSessionMap;
        this.sslEngineMap = sslEngineMap;
        this.authenticator = authenticator;
    }

    private String resolveRemoteAddress(ChannelHandlerContext ctx) {
        if (ctx.channel().remoteAddress() instanceof InetSocketAddress) {
            InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
            return address.getAddress().getHostAddress() + ":" + address.getPort();
        }
        return String.valueOf(ctx.channel().remoteAddress());
    }
}
