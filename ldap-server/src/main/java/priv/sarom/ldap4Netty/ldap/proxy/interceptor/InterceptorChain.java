package priv.sarom.ldap4Netty.ldap.proxy.interceptor;

import io.netty.channel.Channel;
import org.apache.directory.api.ldap.model.message.Message;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Manages an ordered list of ProxyInterceptors and runs messages through them.
 */
public class InterceptorChain {

    private final List<ProxyInterceptor> interceptors = new ArrayList<>();

    public InterceptorChain addInterceptor(ProxyInterceptor interceptor) {
        interceptors.add(interceptor);
        interceptors.sort(Comparator.comparingInt(ProxyInterceptor::order));
        return this;
    }

    /**
     * Runs request through all interceptors. Returns null if any interceptor drops it.
     */
    public Message applyRequest(Channel clientChannel, Message request) {
        Message current = request;
        for (ProxyInterceptor interceptor : interceptors) {
            current = interceptor.onRequest(clientChannel, current);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    /**
     * Runs response through all interceptors (in reverse order). Returns null if any drops it.
     */
    public Message applyResponse(Channel clientChannel, Message response) {
        Message current = response;
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            current = interceptors.get(i).onResponse(clientChannel, current);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    public void fireConnect(Channel clientChannel) {
        for (ProxyInterceptor interceptor : interceptors) {
            interceptor.onConnect(clientChannel);
        }
    }

    public void fireDisconnect(Channel clientChannel) {
        for (ProxyInterceptor interceptor : interceptors) {
            interceptor.onDisconnect(clientChannel);
        }
    }

    public boolean isEmpty() {
        return interceptors.isEmpty();
    }
}
