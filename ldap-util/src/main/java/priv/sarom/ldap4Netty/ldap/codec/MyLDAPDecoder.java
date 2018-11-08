package priv.sarom.ldap4Netty.ldap.codec;

import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.codec.api.LdapDecoder;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.api.MessageDecorator;
import org.apache.directory.api.ldap.codec.osgi.DefaultLdapCodecService;
import org.apache.directory.api.ldap.model.message.Message;

import java.io.InputStream;

/**
 * @descriptions:
 * @date: 2018/11/9
 * @author: SaromChars
 */
public class MyLDAPDecoder {

    public static Message decode2Message(InputStream is) throws DecoderException {

        LdapApiService ldapCodecService = new DefaultLdapCodecService();
        LdapDecoder ldapDecoder = new LdapDecoder();
        LdapMessageContainer container = new LdapMessageContainer<MessageDecorator<? extends Message>>(ldapCodecService);

        ldapDecoder.decode(is, container);
        Message msg = container.getMessage().getDecorated();

        return msg;
    }
}
