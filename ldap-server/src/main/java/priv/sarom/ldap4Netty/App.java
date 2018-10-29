package priv.sarom.ldap4Netty;

import priv.sarom.ldap4Netty.demo.MultiServer;
import priv.sarom.ldap4Netty.demo.handler.EchoServerHandler;
import priv.sarom.ldap4Netty.demo.handler.TimeServerHandler;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws InterruptedException {
        MultiServer.builder()
                .port(37)
                .channelHandler(new TimeServerHandler())
                .build()
                .run();
    }
}
