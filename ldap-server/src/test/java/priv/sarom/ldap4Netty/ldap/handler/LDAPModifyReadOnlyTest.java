package priv.sarom.ldap4Netty.ldap.handler;

import io.netty.channel.embedded.EmbeddedChannel;
import org.apache.directory.api.ldap.model.message.ModifyRequestImpl;
import org.apache.directory.api.ldap.model.message.ModifyResponse;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.name.Dn;
import org.junit.Test;
import priv.sarom.ldap4Netty.ldap.entity.LDAPSession;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LDAPModifyReadOnlyTest {

    @Test
    public void readOnlyModifyReturnsUnwillingToPerform() throws Exception {
        ConcurrentHashMap<String, LDAPSession> sessions = new ConcurrentHashMap<>();
        EmbeddedChannel channel = new EmbeddedChannel(new LDAPModifyHandler(sessions, true));
        sessions.put(channel.id().asLongText(), new LDAPSession());

        ModifyRequestImpl request = new ModifyRequestImpl();
        request.setMessageId(9);
        request.setName(new Dn("uid=alice,dc=example,dc=com"));

        channel.writeInbound(request);

        Object response = channel.readOutbound();
        assertTrue(response instanceof ModifyResponse);
        assertEquals(9, ((ModifyResponse) response).getMessageId());
        assertEquals(ResultCodeEnum.UNWILLING_TO_PERFORM, ((ModifyResponse) response).getLdapResult().getResultCode());
    }
}
