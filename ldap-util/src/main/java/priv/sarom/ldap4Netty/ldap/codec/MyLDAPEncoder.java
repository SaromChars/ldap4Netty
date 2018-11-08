package priv.sarom.ldap4Netty.ldap.codec;

import org.apache.directory.api.asn1.EncoderException;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.codec.api.LdapEncoder;
import org.apache.directory.api.ldap.codec.osgi.DefaultLdapCodecService;
import org.apache.directory.api.ldap.model.message.Message;

import java.nio.ByteBuffer;

/**
 * @descriptions:
 * @date: 2018/11/9
 * @author: SaromChars
 */
public class MyLDAPEncoder {

    public static byte[] encode2Byte(Message message) throws EncoderException {

        LdapApiService ldapCodecService = new DefaultLdapCodecService();
        LdapEncoder ldapEncoder = new LdapEncoder(ldapCodecService);

        ByteBuffer byteBuffer = ldapEncoder.encodeMessage(message);
        byte[] array = byteBuffer.array();

        return array;
    }
}
