package priv.sarom.ldap4Netty.ldap.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticationContext {

    private String bindDn;
    private byte[] credentials;
    private String remoteAddress;
    private byte[] clientCertificate;
}
