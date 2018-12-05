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

public class App {
    public static void main(String[] args) throws Exception {

        Configurator.setRootLevel(Level.DEBUG);

        LDAPServer ldapServer = new LDAPServer(6666);
        HashMap<String, LDAPSession> sessionMap = new HashMap<>(100);

        ldapServer.appendEncoder(LDAPEncoder.class)
                .appendDecoder(LDAPDecoder.class)
                .appendHandler(new LDAPBindHandler(sessionMap, null))
                .appendHandler(new LDAPAbandonHandler())
                .appendHandler(new LDAPModifyHandler(sessionMap))
                .appendHandler(new LDAPExceptionHandler())

                //非ssl模式下sslContext和sslEngineMap非必需 ， nullable
                .init(null, null)
                .start();

    }
}
