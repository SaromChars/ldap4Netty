package priv.sarom.ldap4Netty.ldap.auth;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StaticAccountAuthenticatorTest {

    @Test
    public void acceptsConfiguredDnAndPassword() {
        StaticAccountAuthenticator authenticator =
                new StaticAccountAuthenticator(Collections.singletonMap("cn=testuser", "123456"));

        AuthenticationContext context = AuthenticationContext.builder()
                .bindDn("cn=testuser")
                .credentials("123456".getBytes(StandardCharsets.UTF_8))
                .remoteAddress("127.0.0.1")
                .build();

        assertTrue(authenticator.authenticate(context).isSuccess());
    }

    @Test
    public void rejectsUnknownDnOrWrongPassword() {
        StaticAccountAuthenticator authenticator =
                new StaticAccountAuthenticator(Collections.singletonMap("cn=testuser", "123456"));

        assertFalse(authenticator.authenticate(AuthenticationContext.builder()
                .bindDn("cn=missing")
                .credentials("123456".getBytes(StandardCharsets.UTF_8))
                .build()).isSuccess());
        assertFalse(authenticator.authenticate(AuthenticationContext.builder()
                .bindDn("cn=testuser")
                .credentials("bad".getBytes(StandardCharsets.UTF_8))
                .build()).isSuccess());
    }

    @Test
    public void authenticationContextCarriesConnectionData() {
        byte[] cert = new byte[]{1, 2, 3};

        AuthenticationContext context = AuthenticationContext.builder()
                .bindDn("cn=testuser")
                .credentials("123456".getBytes(StandardCharsets.UTF_8))
                .remoteAddress("127.0.0.1:389")
                .clientCertificate(cert)
                .build();

        assertTrue(context.getRemoteAddress().contains("127.0.0.1"));
        assertTrue(context.getClientCertificate().length == 3);
    }
}
