package priv.sarom.ldap4Netty;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import priv.sarom.ldap4Netty.ldap.LDAPServer;
import priv.sarom.ldap4Netty.ldap.codec.LDAPDecoder;
import priv.sarom.ldap4Netty.ldap.codec.LDAPEncoder;
import priv.sarom.ldap4Netty.ldap.handler.LDAPBindHandler;
import priv.sarom.ldap4Netty.ldap.handler.LDAPDiscardHandler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException {

        Configurator.setRootLevel(Level.DEBUG);

        LDAPServer ldapServer = new LDAPServer(null);

       /* ExecutorService executorService = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            return thread;
        });
        executorService.submit(() ->
                ldapServer.appendHandler(new LDAPDecoder())
                        .appendHandler(new LDAPBindHandler())
//                        .appendHandler(new LDAPEncoder())
                        .start()
        );*/


        ldapServer.appendDecoder(LDAPDecoder.class)
                .appendHandler(new LDAPBindHandler())
                .appendHandler(new LDAPDiscardHandler())
                .appendEncoder(LDAPEncoder.class)
                .start();

    }
}
