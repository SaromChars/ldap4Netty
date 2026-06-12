package priv.sarom.ldap4Netty.ldap.handler;

import io.netty.channel.embedded.EmbeddedChannel;
import org.apache.directory.api.ldap.model.message.BindRequestImpl;
import org.apache.directory.api.ldap.model.message.BindResponse;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.junit.Test;
import priv.sarom.ldap4Netty.ldap.auth.AuthenticationResult;
import priv.sarom.ldap4Netty.ldap.auth.StaticAccountAuthenticator;
import priv.sarom.ldap4Netty.ldap.entity.LDAPSession;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LDAPBindAuthenticationTest {

    @Test
    public void acceptedBindCreatesSessionAndReturnsSuccess() throws Exception {
        Map<String, LDAPSession> sessions = new ConcurrentHashMap<>();
        EmbeddedChannel channel = new EmbeddedChannel(new LDAPBindHandler(sessions, null,
                new StaticAccountAuthenticator(Collections.singletonMap("cn=testuser", "123456"))));

        channel.writeInbound(bindRequest("cn=testuser", "123456"));

        BindResponse response = channel.readOutbound();
        assertEquals(ResultCodeEnum.SUCCESS, response.getLdapResult().getResultCode());
        assertTrue(sessions.containsKey(channel.id().asLongText()));
        assertFalse(Boolean.TRUE.equals(sessions.get(channel.id().asLongText()).getAnonymous()));
    }

    @Test
    public void rejectedBindDoesNotCreateSessionAndReturnsInvalidCredentials() throws Exception {
        Map<String, LDAPSession> sessions = new ConcurrentHashMap<>();
        EmbeddedChannel channel = new EmbeddedChannel(new LDAPBindHandler(sessions, null,
                context -> AuthenticationResult.rejected("bad credentials")));

        channel.writeInbound(bindRequest("cn=testuser", "bad"));

        BindResponse response = channel.readOutbound();
        assertEquals(ResultCodeEnum.INVALID_CREDENTIALS, response.getLdapResult().getResultCode());
        assertFalse(sessions.containsKey(channel.id().asLongText()));
    }

    private BindRequestImpl bindRequest(String dn, String password) {
        BindRequestImpl request = new BindRequestImpl();
        request.setMessageId(1);
        request.setVersion3(true);
        request.setName(dn);
        request.setCredentials(password);
        return request;
    }
}
