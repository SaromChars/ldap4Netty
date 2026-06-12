package priv.sarom.ldap4Netty.ldap.proxy;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProxyConfig {

    @Builder.Default
    private int listenPort = 389;

    private String upstreamHost;

    @Builder.Default
    private int upstreamPort = 389;

    @Builder.Default
    private boolean upstreamSsl = false;

    @Builder.Default
    private int maxConnections = 50;

    @Builder.Default
    private long connectTimeoutMillis = 5000;

    @Builder.Default
    private boolean frontendSsl = false;

    private String keystorePath;

    private char[] keystorePassword;

    public ProxyConfig validate() {
        validatePort("listenPort", listenPort);
        validatePort("upstreamPort", upstreamPort);
        if (upstreamHost == null || upstreamHost.trim().isEmpty()) {
            throw new IllegalArgumentException("upstreamHost must not be blank");
        }
        if (connectTimeoutMillis <= 0 || connectTimeoutMillis > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("connectTimeoutMillis must be between 1 and " + Integer.MAX_VALUE);
        }
        if (maxConnections <= 0) {
            throw new IllegalArgumentException("maxConnections must be positive");
        }
        return this;
    }

    private void validatePort(String name, int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException(name + " must be between 1 and 65535");
        }
    }
}
