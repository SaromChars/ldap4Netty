package priv.sarom.ldap4Netty;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import priv.sarom.ldap4Netty.ldap.LDAPServer;
import priv.sarom.ldap4Netty.ldap.codec.LDAPDecoder;
import priv.sarom.ldap4Netty.ldap.codec.LDAPEncoder;
import priv.sarom.ldap4Netty.ldap.handler.LDAPAbandonHandler;
import priv.sarom.ldap4Netty.ldap.handler.LDAPBindHandler;
import priv.sarom.ldap4Netty.ldap.handler.LDAPExceptionHandler;
import priv.sarom.ldap4Netty.ldap.handler.LDAPModifyHandler;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException {

        Configurator.setRootLevel(Level.DEBUG);

        LDAPServer ldapServer = new LDAPServer(null);

        ldapServer.appendEncoder(LDAPEncoder.class)
                .appendDecoder(LDAPDecoder.class)
                .appendHandler(new LDAPBindHandler())
                .appendHandler(new LDAPAbandonHandler())
                .appendHandler(new LDAPModifyHandler())
                .appendHandler(new LDAPExceptionHandler())
                .start();

    }
}
