package priv.sarom.ldap4Netty.ssl;

import org.junit.Test;
import priv.sarom.ldap4Netty.ldap.LDAPServer;

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Enumeration;

/**
 * 说明:
 *
 * @author: cxy, 2018/11/12
 */
public class KeyStoreTest {

    private char[] pass = "123456".toCharArray();

    @Test
    public void checkCert() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {

        try (InputStream resourceAsStream = LDAPServer.class.getResourceAsStream("/server.jks")) {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(resourceAsStream, pass);

            System.out.println(ks.getKey("server",pass).getAlgorithm());

            Enumeration<String> aliases = ks.aliases();

            while (aliases.hasMoreElements()){
                System.out.println(aliases.nextElement());
            }
        }

    }
}
