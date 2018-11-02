package priv.sarom.ldap4Netty;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import priv.sarom.ldap4Netty.ldap.LDAPServer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        /*MultiServer.builder()
                .port(37)
                .channelHandler(new TimeServerHandler())
                .build()
                .run();*/

        Configurator.setRootLevel(Level.DEBUG);

        LDAPServer ldapServer = new LDAPServer(null);

        ExecutorService executorService = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        });
        executorService.submit(() -> ldapServer.start());

        Thread.sleep(2000);
        ldapServer.stop();

    }
}
