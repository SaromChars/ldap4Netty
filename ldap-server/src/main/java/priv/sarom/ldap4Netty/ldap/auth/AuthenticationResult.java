package priv.sarom.ldap4Netty.ldap.auth;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthenticationResult {

    private final boolean success;
    private final String diagnosticMessage;

    public static AuthenticationResult accepted() {
        return new AuthenticationResult(true, null);
    }

    public static AuthenticationResult rejected(String diagnosticMessage) {
        return new AuthenticationResult(false, diagnosticMessage);
    }
}
