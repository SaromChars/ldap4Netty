package priv.sarom.ldap4Netty;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.util.ExecutorServices;
import org.hibernate.validator.cdi.HibernateValidator;
import org.hibernate.validator.cdi.internal.interceptor.MethodValidated;
import priv.sarom.ldap4Netty.demo.MultiServer;
import priv.sarom.ldap4Netty.demo.handler.EchoServerHandler;
import priv.sarom.ldap4Netty.demo.handler.TimeServerHandler;
import priv.sarom.ldap4Netty.ldap.LDAPServer;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
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

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        factory.getValidator().forExecutables();

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
