package priv.sarom.ldap4Netty.ldap.proxy.interceptor;

import io.netty.channel.Channel;
import org.apache.directory.api.ldap.model.message.Message;

/**
 * Intercepts LDAP messages flowing through the proxy.
 * Implementations can inspect, modify, or block messages. Request
 * interceptors run in ascending {@link #order()} value. Response interceptors
 * run in the reverse order. Returning {@code null} drops the message and stops
 * the remaining interceptors for that direction.
 */
public interface ProxyInterceptor {

    /**
     * Called before a client request is forwarded to the upstream.
     *
     * @param clientChannel the client's channel
     * @param request       the LDAP request message
     * @return the (possibly modified) message to forward, or null to drop the request
     */
    default Message onRequest(Channel clientChannel, Message request) {
        return request;
    }

    /**
     * Called before an upstream response is relayed back to the client.
     *
     * @param clientChannel the client's channel
     * @param response      the LDAP response message
     * @return the (possibly modified) message to relay, or null to drop the response
     */
    default Message onResponse(Channel clientChannel, Message response) {
        return response;
    }

    /**
     * Called when the client connection is established.
     */
    default void onConnect(Channel clientChannel) {
    }

    /**
     * Called when the client connection is closed.
     */
    default void onDisconnect(Channel clientChannel) {
    }

    /**
     * The order in which this interceptor runs (lower = earlier).
     */
    default int order() {
        return 100;
    }
}
