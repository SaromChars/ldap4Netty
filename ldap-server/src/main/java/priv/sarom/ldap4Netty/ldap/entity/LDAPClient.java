package priv.sarom.ldap4Netty.ldap.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @descriptions: defines the LDAP client
 * @date: 2018/11/2
 * @author: SaromChars
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LDAPClient {

    private Integer id;

    private String name;

    private String account;

    private String password;

    private Byte status;

    private String memo;

    private Boolean ipVerify;
    private String ip;


    private Boolean certVerify;
    private String certSHA1Hex;
    private byte[] cert;

    @Deprecated
    private Boolean privilege;
}
