package priv.sarom.ldap4Netty;

import priv.sarom.ldap4Netty.ldap.proxy.LDAPProxyServer;
import priv.sarom.ldap4Netty.ldap.proxy.ProxyConfig;
import priv.sarom.ldap4Netty.ldap.proxy.interceptor.AccessLogInterceptor;

/**
 * Launch the LDAP Proxy Server.
 *
 * Usage: ProxyApp [listenPort] [upstreamHost] [upstreamPort]
 * Default: listen on 6389, forward to localhost:389
 */
public class ProxyApp {

    public static void main(String[] args) throws Exception {
        int listenPort = args.length > 0 ? Integer.parseInt(args[0]) : 6389;
        String upstreamHost = args.length > 1 ? args[1] : "localhost";
        int upstreamPort = args.length > 2 ? Integer.parseInt(args[2]) : 389;

        ProxyConfig config = ProxyConfig.builder()
                .listenPort(listenPort)
                .upstreamHost(upstreamHost)
                .upstreamPort(upstreamPort)
                .build();

        LDAPProxyServer proxyServer = new LDAPProxyServer(config);
        proxyServer.addInterceptor(new AccessLogInterceptor());
        proxyServer.start();
    }
}
