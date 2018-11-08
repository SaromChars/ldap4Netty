package priv.sarom.ldap4Netty.ldap.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.cert.X509Certificate;

/**
 * @descriptions: defines the session created by remote connection information
 * @date: 2018/11/2
 * @author: SaromChars
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LDAPSession  {

    private X509Certificate remoteCert;
    private String remoteIP;

    private LDAPAccount client;

    private Boolean anonymous;
}
