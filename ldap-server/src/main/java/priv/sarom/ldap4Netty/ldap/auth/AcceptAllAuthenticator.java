package priv.sarom.ldap4Netty.ldap.auth;

public class AcceptAllAuthenticator implements Authenticator {

    @Override
    public AuthenticationResult authenticate(AuthenticationContext context) {
        return AuthenticationResult.accepted();
    }
}
