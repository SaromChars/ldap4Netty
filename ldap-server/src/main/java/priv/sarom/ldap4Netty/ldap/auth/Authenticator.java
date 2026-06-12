package priv.sarom.ldap4Netty.ldap.auth;

public interface Authenticator {

    AuthenticationResult authenticate(AuthenticationContext context);
}
