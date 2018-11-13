package priv.sarom.ldap4Netty;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import priv.sarom.ldap4Netty.ldap.LDAPServer;
import priv.sarom.ldap4Netty.ldap.SSLHelper;
import priv.sarom.ldap4Netty.ldap.codec.LDAPDecoder;
import priv.sarom.ldap4Netty.ldap.codec.LDAPEncoder;
import priv.sarom.ldap4Netty.ldap.entity.LDAPSession;
import priv.sarom.ldap4Netty.ldap.handler.LDAPAbandonHandler;
import priv.sarom.ldap4Netty.ldap.handler.LDAPBindHandler;
import priv.sarom.ldap4Netty.ldap.handler.LDAPExceptionHandler;
import priv.sarom.ldap4Netty.ldap.handler.LDAPModifyHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.File;
import java.util.HashMap;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws Exception {

        Configurator.setRootLevel(Level.DEBUG);

        LDAPServer ldapServer = new LDAPServer(null);

        HashMap<String, LDAPSession> sessionMap = new HashMap<>(100);
        HashMap<String, SSLEngine> sslEngineMap = new HashMap<>(100);

//        SSLContext sslContext = SSLHelper.createSSLContext(new File(App.class.getResource("/server.jks").getFile()), "123456".toCharArray(),new File(App.class.getResource("/server.jks").getFile()), "123456".toCharArray());

        SSLContext sslContext = SSLHelper.createSSLContext(new File("D:\\server\\apache-tomcat-8.0.35\\conf\\https\\keystore.jks"), "123456".toCharArray(),new File("D:\\server\\apache-tomcat-8.0.35\\conf\\https\\keystore.jks"), "123456".toCharArray());

        ldapServer.appendEncoder(LDAPEncoder.class)
                .appendDecoder(LDAPDecoder.class)
                .appendHandler(new LDAPBindHandler(sessionMap, sslEngineMap))
                .appendHandler(new LDAPAbandonHandler())
                .appendHandler(new LDAPModifyHandler(sessionMap))
                .appendHandler(new LDAPExceptionHandler())
                .init(sslContext, sslEngineMap)
                .start();

    }
}
