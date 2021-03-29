package priv.sarom.ldap4Netty;

import org.apache.directory.api.ldap.model.entry.DefaultAttribute;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.message.BindRequest;
import org.apache.directory.api.ldap.model.message.BindRequestImpl;
import org.apache.directory.api.ldap.model.message.Message;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.apache.directory.api.ldap.model.message.ModifyRequestImpl;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import priv.sarom.ldap4Netty.ldap.LDAPClient;
import priv.sarom.ldap4Netty.ldap.SSLHelper;
import priv.sarom.ldap4Netty.ldap.codec.LDAPDecoder;
import priv.sarom.ldap4Netty.ldap.codec.LDAPEncoder;
import priv.sarom.ldap4Netty.ldap.exception.LDAPException;
import priv.sarom.ldap4Netty.ldap.handler.LDAPOperationHandler;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

public class App {
    public static void main(String[] args) throws InterruptedException, LdapInvalidDnException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyManagementException, KeyStoreException, LDAPException {

        String name = "cn=accout";
        Dn rdns = new Dn(new Rdn(name));

        //build a BindRequest
        BindRequest bindRequest = new BindRequestImpl();
        bindRequest.setMessageId(1);
        bindRequest.setVersion3(true);
        bindRequest.setDn(rdns);
        bindRequest.setName(name);
        bindRequest.setCredentials("123456");

        //bind a ModifyRequest
        ModifyRequest modifyRequest = new ModifyRequestImpl();
        modifyRequest.setMessageId(3);
        modifyRequest.setName(rdns);

        DefaultAttribute attribute = new DefaultAttribute("userCertificate;binary", new byte[1024*8]);
        modifyRequest.add(attribute);

        int count = 50;
        while (count-- > 0) {
            List<Message> messages = new ArrayList<>();
            messages.add(bindRequest);
            messages.add(modifyRequest);
            messages.add(modifyRequest);
            messages.add(modifyRequest);
            messages.add(modifyRequest);
            messages.add(modifyRequest);
            messages.add(modifyRequest);
            messages.add(modifyRequest);
            messages.add(modifyRequest);
            messages.add(modifyRequest);
            messages.add(modifyRequest);

            LDAPClient client = new LDAPClient("localhost", 6389);
            SSLContext sslContext = SSLHelper.createSSLContext(new File(App.class.getResource("/client.jks").getPath()), "123456".toCharArray());
            client.appendEncoder(LDAPEncoder.class)
                    .appendDecoder(LDAPDecoder.class)
                    .appendHandler(new LDAPOperationHandler(messages))
                    .init(sslContext, null)
                    .start();
        }

        Thread.currentThread().join(30_000);


    }
}
