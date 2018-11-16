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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import priv.sarom.ldap4Netty.ldap.LDAPClient;
import priv.sarom.ldap4Netty.ldap.SSLHelper;
import priv.sarom.ldap4Netty.ldap.codec.LDAPDecoder;
import priv.sarom.ldap4Netty.ldap.codec.LDAPEncoder;
import priv.sarom.ldap4Netty.ldap.exception.LDAPException;
import priv.sarom.ldap4Netty.ldap.handler.LDAPRequestHandler;
import priv.sarom.ldap4Netty.ldap.handler.LDAPResponseHandler;

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

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws InterruptedException, LdapInvalidDnException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyManagementException, KeyStoreException, LDAPException {

        Configurator.setRootLevel(Level.INFO);

        List<Message> messages = new ArrayList<>();

        String name = "cn=accout";
        Dn rdns = new Dn(new Rdn(name));

        //build a BindRequest
        BindRequest bindRequest = new BindRequestImpl();
        bindRequest.setMessageId(1);
        bindRequest.setVersion3(true);
        bindRequest.setDn(rdns);
        bindRequest.setName(name);
        bindRequest.setCredentials("123456");

        messages.add(bindRequest);

        //bind a ModifyRequest
        ModifyRequest modifyRequest = new ModifyRequestImpl();
        modifyRequest.setMessageId(3);
        modifyRequest.setName(rdns);

        DefaultAttribute attribute = new DefaultAttribute("userCertificate;binary", "testdemo".getBytes());
        modifyRequest.add(attribute);

        messages.add(modifyRequest);


        LDAPClient client = new LDAPClient("192.168.20.150", 6389);

        SSLContext sslContext = SSLHelper.createSSLContext(new File(App.class.getResource("/client.jks").getFile()), "123456".toCharArray(), new File(App.class.getResource("/client.jks").getFile()), "123456".toCharArray());

        client.appendEncoder(LDAPEncoder.class)
                .appendDecoder(LDAPDecoder.class)
                .appendHandler(new LDAPResponseHandler())
                .appendHandler(new LDAPRequestHandler(messages))
                .init(sslContext, null)
                .start();


    }
}
