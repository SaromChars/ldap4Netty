package priv.sarom.ldap4Netty.ldap.codec;

import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.codec.api.LdapDecoder;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.osgi.DefaultLdapCodecService;
import org.apache.directory.api.ldap.model.message.Message;

import java.io.InputStream;

/**
 * @descriptions:
 * @date: 2018/11/9
 * @author: SaromChars
 */
@Slf4j
public class MyLDAPDecoder {

    private static LdapApiService ldapCodecService = new DefaultLdapCodecService();

    public static Message decode2Message(InputStream bis) throws DecoderException {
        LdapDecoder ldapDecoder = new LdapDecoder();
        LdapMessageContainer container = new LdapMessageContainer(ldapCodecService);
        ldapDecoder.decode(bis, container);
        return container.getMessage();
    }
}
