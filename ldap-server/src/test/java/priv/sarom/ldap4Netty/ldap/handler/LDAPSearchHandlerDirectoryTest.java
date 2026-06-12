package priv.sarom.ldap4Netty.ldap.handler;

import io.netty.channel.embedded.EmbeddedChannel;
import org.apache.directory.api.ldap.model.filter.EqualityNode;
import org.apache.directory.api.ldap.model.message.SearchRequestImpl;
import org.apache.directory.api.ldap.model.message.SearchResultDone;
import org.apache.directory.api.ldap.model.message.SearchResultEntry;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.junit.Test;
import priv.sarom.ldap4Netty.ldap.directory.InMemoryDirectory;
import priv.sarom.ldap4Netty.ldap.directory.StaticDirectoryEntry;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LDAPSearchHandlerDirectoryTest {

    @Test
    public void writesMatchingEntriesThenExactlyOneDoneResponse() throws Exception {
        InMemoryDirectory directory = new InMemoryDirectory(Collections.singletonList(
                StaticDirectoryEntry.builder()
                        .dn("uid=alice,ou=people,dc=example,dc=com")
                        .attribute("objectClass", "inetOrgPerson")
                        .attribute("uid", "alice")
                        .attribute("mail", "alice@example.com")
                        .build()));
        EmbeddedChannel channel = new EmbeddedChannel(new LDAPSearchHandler(new ConcurrentHashMap<>(), directory));

        SearchRequestImpl request = new SearchRequestImpl();
        request.setMessageId(7);
        request.setBase(new Dn("dc=example,dc=com"));
        request.setScope(SearchScope.SUBTREE);
        request.setFilter(new EqualityNode<>("uid", "alice"));

        channel.writeInbound(request);

        Object first = channel.readOutbound();
        Object second = channel.readOutbound();

        assertTrue(first instanceof SearchResultEntry);
        assertEquals(7, ((SearchResultEntry) first).getMessageId());
        assertEquals("uid=alice,ou=people,dc=example,dc=com", ((SearchResultEntry) first).getObjectName().getName());
        assertEquals("alice@example.com", ((SearchResultEntry) first).getEntry().get("mail").getString());
        assertTrue(second instanceof SearchResultDone);
        assertEquals(7, ((SearchResultDone) second).getMessageId());
        assertNull(channel.readOutbound());
    }
}
