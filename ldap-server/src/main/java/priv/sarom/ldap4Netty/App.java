package priv.sarom.ldap4Netty;


import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.LoggerConfig;
import priv.sarom.ldap4Netty.ldap.LDAPServer;
import priv.sarom.ldap4Netty.ldap.SSLHelper;
import priv.sarom.ldap4Netty.ldap.codec.LDAPDecoder;
import priv.sarom.ldap4Netty.ldap.codec.LDAPEncoder;
import priv.sarom.ldap4Netty.ldap.entity.LDAPSession;
import priv.sarom.ldap4Netty.ldap.handler.LDAPAbandonHandler;
import priv.sarom.ldap4Netty.ldap.handler.LDAPBindHandler;
import priv.sarom.ldap4Netty.ldap.handler.LDAPExceptionHandler;
import priv.sarom.ldap4Netty.ldap.handler.LDAPModifyHandler;
import priv.sarom.ldap4Netty.ldap.handler.LDAPSearchHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.File;
import java.util.HashMap;

public class App {
    public static void main(String[] args) throws Exception {

        LoggerConfig loggerConfig = new LoggerConfig();
        loggerConfig.setLevel(Level.INFO);

        LDAPServer ldapServer = new LDAPServer(6389);
        HashMap<String, LDAPSession> sessionMap = new HashMap<>(100);
        HashMap<String, SSLEngine> sslEngineMap = new HashMap<>(100);

        SSLContext sslContext = SSLHelper.createSSLContext(new File(App.class.getResource("/server.jks").getPath()), "123456".toCharArray());

        ldapServer.appendEncoder(LDAPEncoder.class)
                .appendDecoder(LDAPDecoder.class)
                //非验证证书时,sslEngineMap非必需
                .appendHandler(new LDAPBindHandler(sessionMap, sslEngineMap))
                .appendHandler(new LDAPAbandonHandler())
                .appendHandler(new LDAPModifyHandler(sessionMap))
                .appendHandler(new LDAPSearchHandler(sessionMap))
                .appendHandler(new LDAPExceptionHandler())

                //非ssl模式下sslContext非必需 ， nullable
                //非验证证书时,sslEngineMap非必需
                .init(sslContext, sslEngineMap)
                .start();

    }
}
