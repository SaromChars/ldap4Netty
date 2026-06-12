package priv.sarom.ldap4Netty.ldap.proxy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Maps client-side messageIds to upstream-side messageIds to avoid collisions
 * when multiple clients share upstream connections.
 *
 * In the 1:1 connection model, this acts as a pass-through (client and upstream
 * use the same messageId). When upgrading to N:M pooling, this component
 * allocates unique upstream messageIds and tracks the reverse mapping.
 */
public class MessageIdMapper {

    private final AtomicInteger upstreamIdGenerator = new AtomicInteger(1);

    // clientMsgId -> upstreamMsgId
    private final Map<Integer, Integer> clientToUpstream = new ConcurrentHashMap<>();
    // upstreamMsgId -> clientMsgId
    private final Map<Integer, Integer> upstreamToClient = new ConcurrentHashMap<>();

    private final boolean passThrough;

    public MessageIdMapper(boolean passThrough) {
        this.passThrough = passThrough;
    }

    public int toUpstreamId(int clientMsgId) {
        if (passThrough) {
            return clientMsgId;
        }
        return clientToUpstream.computeIfAbsent(clientMsgId, k -> {
            int upstreamId = upstreamIdGenerator.getAndIncrement();
            upstreamToClient.put(upstreamId, clientMsgId);
            return upstreamId;
        });
    }

    public int toClientId(int upstreamMsgId) {
        if (passThrough) {
            return upstreamMsgId;
        }
        Integer clientId = upstreamToClient.get(upstreamMsgId);
        return clientId != null ? clientId : upstreamMsgId;
    }

    public void removeMapping(int clientMsgId) {
        if (passThrough) {
            return;
        }
        Integer upstreamId = clientToUpstream.remove(clientMsgId);
        if (upstreamId != null) {
            upstreamToClient.remove(upstreamId);
        }
    }

    public void clear() {
        clientToUpstream.clear();
        upstreamToClient.clear();
    }
}
