package priv.sarom.ldap4Netty.ldap.auth;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StaticAccountAuthenticator implements Authenticator {

    private final Map<String, String> accounts;

    public StaticAccountAuthenticator(Map<String, String> accounts) {
        this.accounts = Collections.unmodifiableMap(new HashMap<>(accounts));
    }

    @Override
    public AuthenticationResult authenticate(AuthenticationContext context) {
        String expectedPassword = accounts.get(context.getBindDn());
        if (expectedPassword == null) {
            return AuthenticationResult.rejected("unknown bind DN");
        }

        String actualPassword = new String(context.getCredentials(), StandardCharsets.UTF_8);
        if (!expectedPassword.equals(actualPassword)) {
            return AuthenticationResult.rejected("invalid credentials");
        }

        return AuthenticationResult.accepted();
    }
}
