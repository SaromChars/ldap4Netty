package priv.sarom.ldap4Netty.ldap.handler;

import io.netty.channel.embedded.EmbeddedChannel;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.SearchRequestImpl;
import org.apache.directory.api.ldap.model.message.SearchResultDone;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.junit.Test;
import priv.sarom.ldap4Netty.ldap.entity.LDAPSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class LDAPSessionRequiredHandlerTest {

    @Test
    public void searchBeforeBindReturnsAuthenticationRequired() throws Exception {
        Map<String, LDAPSession> sessions = new ConcurrentHashMap<>();
        EmbeddedChannel channel = new EmbeddedChannel(
                new LDAPSessionRequiredHandler(sessions, false),
                new LDAPSearchHandler(sessions));

        assertFalse(channel.writeInbound(searchRequest()));

        SearchResultDone response = channel.readOutbound();
        assertEquals(ResultCodeEnum.STRONG_AUTH_REQUIRED, response.getLdapResult().getResultCode());
    }

    private SearchRequestImpl searchRequest() throws Exception {
        SearchRequestImpl request = new SearchRequestImpl();
        request.setMessageId(2);
        request.setBase(new Dn("dc=test"));
        request.setScope(SearchScope.SUBTREE);
        return request;
    }
}
