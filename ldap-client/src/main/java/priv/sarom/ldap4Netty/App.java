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
import priv.sarom.ldap4Netty.ldap.codec.LDAPDecoder;
import priv.sarom.ldap4Netty.ldap.codec.LDAPEncoder;
import priv.sarom.ldap4Netty.ldap.handler.LDAPRequestHandler;
import priv.sarom.ldap4Netty.ldap.handler.LDAPResponseHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws InterruptedException, LdapInvalidDnException {

        Configurator.setRootLevel(Level.INFO);

        List<Message> messages = new ArrayList<>();

        String name = "cn=account";
        Dn rdns = new Dn(new Rdn(name));

        //build a BindRequest
        BindRequest bindRequest = new BindRequestImpl();
        bindRequest.setMessageId(1);
        bindRequest.setVersion3(true);
        bindRequest.setDn(rdns);
        bindRequest.setName(name);
        bindRequest.setCredentials("pwd");

        messages.add(bindRequest);

        //bind a ModifyRequest
        ModifyRequest modifyRequest = new ModifyRequestImpl();
        modifyRequest.setMessageId(3);
        modifyRequest.setName(rdns);

        DefaultAttribute attribute = new DefaultAttribute("u;binary", "testdemo".getBytes());
        modifyRequest.add(attribute);

        messages.add(modifyRequest);



        LDAPClient client = new LDAPClient("localhost", 389);

        client.appendEncoder(LDAPEncoder.class)
                .appendDecoder(LDAPDecoder.class)
                .appendHandler(new LDAPResponseHandler())
                .appendHandler(new LDAPRequestHandler(messages))
                .start();


    }
}
