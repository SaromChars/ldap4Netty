package priv.sarom.ldap4Netty.ldap.proxy.interceptor;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.ldap.model.message.BindRequest;
import org.apache.directory.api.ldap.model.message.Message;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.apache.directory.api.ldap.model.message.Request;
import org.apache.directory.api.ldap.model.message.ResultResponse;
import org.apache.directory.api.ldap.model.message.SearchRequest;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Logs all LDAP operations flowing through the proxy.
 * Records: client IP, operation type, target DN, result code and elapsed time.
 */
@Slf4j
public class AccessLogInterceptor implements ProxyInterceptor {

    private final Map<String, Long> requestTimestamps = new ConcurrentHashMap<>();

    @Override
    public Message onRequest(Channel clientChannel, Message request) {
        String clientIp = resolveIp(clientChannel);
        String key = clientChannel.id().asShortText() + ":" + request.getMessageId();
        requestTimestamps.put(key, System.currentTimeMillis());

        if (request instanceof Request) {
            Request req = (Request) request;
            String detail = extractDetail(request);
            log.info("[ACCESS] {} -> {} msgId={} {}",
                    clientIp, req.getType(), request.getMessageId(), detail);
        }

        return request;
    }

    @Override
    public Message onResponse(Channel clientChannel, Message response) {
        String key = clientChannel.id().asShortText() + ":" + response.getMessageId();
        Long startTime = requestTimestamps.remove(key);
        long elapsed = startTime != null ? System.currentTimeMillis() - startTime : -1;

        if (response instanceof ResultResponse) {
            ResultResponse rr = (ResultResponse) response;
            log.info("[ACCESS] <- msgId={} result={} elapsed={}ms",
                    response.getMessageId(),
                    rr.getLdapResult().getResultCode(),
                    elapsed);
        }

        return response;
    }

    @Override
    public void onConnect(Channel clientChannel) {
        log.info("[ACCESS] connection opened: {}", resolveIp(clientChannel));
    }

    @Override
    public void onDisconnect(Channel clientChannel) {
        log.info("[ACCESS] connection closed: {}", resolveIp(clientChannel));
    }

    @Override
    public int order() {
        return 0;
    }

    private String resolveIp(Channel channel) {
        InetSocketAddress addr = (InetSocketAddress) channel.remoteAddress();
        return addr != null ? addr.getAddress().getHostAddress() + ":" + addr.getPort() : "unknown";
    }

    private String extractDetail(Message msg) {
        if (msg instanceof BindRequest) {
            BindRequest br = (BindRequest) msg;
            return "dn=" + br.getName();
        } else if (msg instanceof SearchRequest) {
            SearchRequest sr = (SearchRequest) msg;
            return "base=" + sr.getBase() + " scope=" + sr.getScope() + " filter=" + sr.getFilter();
        } else if (msg instanceof ModifyRequest) {
            ModifyRequest mr = (ModifyRequest) msg;
            return "dn=" + mr.getName();
        }
        return "";
    }
}
