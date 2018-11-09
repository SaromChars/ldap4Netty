package priv.sarom.ldap4Netty;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import priv.sarom.ldap4Netty.ldap.LDAPServer;
import priv.sarom.ldap4Netty.ldap.codec.LDAPDecoder;
import priv.sarom.ldap4Netty.ldap.codec.LDAPEncoder;
import priv.sarom.ldap4Netty.ldap.entity.LDAPSession;
import priv.sarom.ldap4Netty.ldap.handler.LDAPAbandonHandler;
import priv.sarom.ldap4Netty.ldap.handler.LDAPBindHandler;
import priv.sarom.ldap4Netty.ldap.handler.LDAPExceptionHandler;
import priv.sarom.ldap4Netty.ldap.handler.LDAPModifyHandler;

import java.util.HashMap;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws Exception {

        Configurator.setRootLevel(Level.INFO);

        LDAPServer ldapServer = new LDAPServer(null);

        HashMap<String, LDAPSession> sessionMap = new HashMap<>(100);

        ldapServer.appendEncoder(LDAPEncoder.class)
                .appendDecoder(LDAPDecoder.class)
                .appendHandler(new LDAPBindHandler(sessionMap))
                .appendHandler(new LDAPAbandonHandler())
                .appendHandler(new LDAPModifyHandler(sessionMap))
                .appendHandler(new LDAPExceptionHandler())
                .start();

    }
}
