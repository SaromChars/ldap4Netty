package priv.sarom.ldap4Netty.proxy;

import io.netty.channel.Channel;
import org.apache.directory.api.ldap.model.message.Message;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import priv.sarom.ldap4Netty.ldap.LDAPServer;
import priv.sarom.ldap4Netty.ldap.codec.LDAPDecoder;
import priv.sarom.ldap4Netty.ldap.codec.LDAPEncoder;
import priv.sarom.ldap4Netty.ldap.entity.LDAPSession;
import priv.sarom.ldap4Netty.ldap.handler.LDAPAbandonHandler;
import priv.sarom.ldap4Netty.ldap.handler.LDAPBindHandler;
import priv.sarom.ldap4Netty.ldap.handler.LDAPExceptionHandler;
import priv.sarom.ldap4Netty.ldap.handler.LDAPSearchHandler;
import priv.sarom.ldap4Netty.ldap.proxy.LDAPProxyServer;
import priv.sarom.ldap4Netty.ldap.proxy.ProxyConfig;
import priv.sarom.ldap4Netty.ldap.proxy.interceptor.AccessLogInterceptor;
import priv.sarom.ldap4Netty.ldap.proxy.interceptor.ProxyInterceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class LDAPProxyIntegrationTest {

    private static final int UPSTREAM_PORT = 16389;
    private static final int PROXY_PORT = 16390;

    private static Thread upstreamThread;
    private static LDAPServer upstreamServer;
    private static LDAPProxyServer proxyServer;
    private static final AtomicInteger interceptedRequests = new AtomicInteger(0);

    @BeforeClass
    public static void setUp() throws Exception {
        ConcurrentHashMap<String, LDAPSession> sessionMap = new ConcurrentHashMap<>();

        upstreamServer = new LDAPServer(UPSTREAM_PORT);
        upstreamServer.appendEncoder(LDAPEncoder.class)
                .appendDecoder(LDAPDecoder.class)
                .appendHandler(new LDAPBindHandler(sessionMap, null))
                .appendHandler(new LDAPAbandonHandler())
                .appendHandler(new LDAPSearchHandler(sessionMap))
                .appendHandler(new LDAPExceptionHandler())
                .init(null, null);

        upstreamThread = new Thread(() -> {
            try {
                upstreamServer.start();
            } catch (Exception ignored) {
            }
        });
        upstreamThread.setDaemon(true);
        upstreamThread.start();
        Thread.sleep(1000);

        ProxyConfig config = ProxyConfig.builder()
                .listenPort(PROXY_PORT)
                .upstreamHost("localhost")
                .upstreamPort(UPSTREAM_PORT)
                .build();

        proxyServer = new LDAPProxyServer(config);
        proxyServer.addInterceptor(new ProxyInterceptor() {
            @Override
            public Message onRequest(Channel clientChannel, Message request) {
                interceptedRequests.incrementAndGet();
                return request;
            }

            @Override
            public int order() {
                return 0;
            }
        });
        proxyServer.addInterceptor(new AccessLogInterceptor());

        proxyServer.startAsync();
        assertTrue("proxy should be ready before tests start",
                proxyServer.awaitReady(2, TimeUnit.SECONDS));
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (proxyServer != null) {
            proxyServer.stop();
        }
        if (upstreamServer != null) {
            upstreamServer.stop();
        }
    }

    @Test
    public void testProxyForwardsBindRequest() throws Exception {
        org.apache.directory.ldap.client.api.LdapConnection connection =
                new org.apache.directory.ldap.client.api.LdapNetworkConnection("localhost", PROXY_PORT);
        try {
            connection.bind("cn=testuser", "123456");
            assertTrue("bind through proxy should succeed", connection.isAuthenticated());
        } finally {
            connection.unBind();
            connection.close();
        }
    }

    @Test
    public void testProxyForwardsSearchRequest() throws Exception {
        org.apache.directory.ldap.client.api.LdapConnection connection =
                new org.apache.directory.ldap.client.api.LdapNetworkConnection("localhost", PROXY_PORT);
        try {
            connection.bind("cn=testuser", "123456");

            org.apache.directory.api.ldap.model.cursor.EntryCursor cursor =
                    connection.search("dc=test", "(objectClass=*)",
                            org.apache.directory.api.ldap.model.message.SearchScope.SUBTREE);

            List<String> results = new ArrayList<>();
            while (cursor.next()) {
                results.add(cursor.get().getDn().toString());
            }
            cursor.close();

            assertFalse("search through proxy should return results", results.isEmpty());
        } finally {
            connection.unBind();
            connection.close();
        }
    }

    @Test
    public void testInterceptorReceivesRequests() throws Exception {
        int countBefore = interceptedRequests.get();

        org.apache.directory.ldap.client.api.LdapConnection connection =
                new org.apache.directory.ldap.client.api.LdapNetworkConnection("localhost", PROXY_PORT);
        try {
            connection.bind("cn=testuser", "123456");
        } finally {
            connection.unBind();
            connection.close();
        }

        assertTrue("interceptor should have captured requests",
                interceptedRequests.get() > countBefore);
    }
}
