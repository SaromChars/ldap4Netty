package priv.sarom.ldap4Netty.proxy;

import org.junit.Test;
import priv.sarom.ldap4Netty.ldap.proxy.LDAPProxyServer;
import priv.sarom.ldap4Netty.ldap.proxy.ProxyConfig;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LDAPProxyRuntimeTest {

    @Test
    public void proxyConfigRejectsMissingUpstreamHostBeforeBinding() throws Exception {
        ProxyConfig config = ProxyConfig.builder()
                .listenPort(freePort())
                .upstreamPort(freePort())
                .build();

        try {
            config.validate();
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("upstreamHost"));
            return;
        }

        throw new AssertionError("missing upstreamHost should be rejected");
    }

    @Test
    public void proxyConfigRejectsInvalidPortsAndTimeouts() {
        assertInvalid(ProxyConfig.builder()
                .listenPort(0)
                .upstreamHost("localhost")
                .upstreamPort(389)
                .build(), "listenPort");
        assertInvalid(ProxyConfig.builder()
                .listenPort(389)
                .upstreamHost("localhost")
                .upstreamPort(70000)
                .build(), "upstreamPort");
        assertInvalid(ProxyConfig.builder()
                .listenPort(389)
                .upstreamHost("localhost")
                .upstreamPort(389)
                .connectTimeoutMillis(0)
                .build(), "connectTimeoutMillis");
    }

    @Test
    public void proxyStartsWithoutBlockingAndStopsIdempotently() throws Exception {
        ProxyConfig config = ProxyConfig.builder()
                .listenPort(freePort())
                .upstreamHost("localhost")
                .upstreamPort(freePort())
                .build();

        LDAPProxyServer proxyServer = new LDAPProxyServer(config);
        try {
            proxyServer.startAsync();

            assertTrue("proxy should become ready", proxyServer.awaitReady(2, TimeUnit.SECONDS));
            assertTrue("proxy should report running", proxyServer.isRunning());

            proxyServer.stop();
            proxyServer.stop();

            assertFalse("proxy should report stopped", proxyServer.isRunning());
        } finally {
            proxyServer.stop();
        }
    }

    @Test(timeout = 3000)
    public void proxyStartFailureDoesNotHangShutdown() throws Exception {
        try (ServerSocket occupied = new ServerSocket(0)) {
            ProxyConfig config = ProxyConfig.builder()
                    .listenPort(occupied.getLocalPort())
                    .upstreamHost("localhost")
                    .upstreamPort(freePort())
                    .build();

            LDAPProxyServer proxyServer = new LDAPProxyServer(config);
            try {
                proxyServer.startAsync();
                proxyServer.awaitReady(2, TimeUnit.SECONDS);
            } catch (IllegalStateException expected) {
                assertTrue(expected.getMessage().contains("failed to start"));
            } finally {
                proxyServer.stop();
            }

            assertFalse(proxyServer.isRunning());
        }
    }

    private int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private void assertInvalid(ProxyConfig config, String expectedMessagePart) {
        try {
            config.validate();
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains(expectedMessagePart));
            return;
        }

        throw new AssertionError("configuration should be rejected: " + expectedMessagePart);
    }
}
