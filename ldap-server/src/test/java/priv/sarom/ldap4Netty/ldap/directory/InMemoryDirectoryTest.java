package priv.sarom.ldap4Netty.ldap.directory;

import org.apache.directory.api.ldap.model.filter.EqualityNode;
import org.apache.directory.api.ldap.model.filter.PresenceNode;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class InMemoryDirectoryTest {

    @Test
    public void matchesBaseOneLevelAndSubtreeScopes() throws Exception {
        InMemoryDirectory directory = directory();

        assertEquals(Arrays.asList("ou=people,dc=example,dc=com"),
                names(directory.search(new Dn("ou=people,dc=example,dc=com"), SearchScope.OBJECT, new PresenceNode("objectClass"))));

        assertEquals(Arrays.asList(
                        "uid=alice,ou=people,dc=example,dc=com",
                        "uid=bob,ou=people,dc=example,dc=com"),
                names(directory.search(new Dn("ou=people,dc=example,dc=com"), SearchScope.ONELEVEL, new PresenceNode("objectClass"))));

        assertEquals(Arrays.asList(
                        "ou=people,dc=example,dc=com",
                        "uid=alice,ou=people,dc=example,dc=com",
                        "uid=bob,ou=people,dc=example,dc=com",
                        "cn=device,ou=devices,ou=people,dc=example,dc=com"),
                names(directory.search(new Dn("ou=people,dc=example,dc=com"), SearchScope.SUBTREE, new PresenceNode("objectClass"))));
    }

    @Test
    public void matchesEqualityAndPresenceFilters() throws Exception {
        InMemoryDirectory directory = directory();

        assertEquals(Arrays.asList("uid=alice,ou=people,dc=example,dc=com"),
                names(directory.search(new Dn("dc=example,dc=com"), SearchScope.SUBTREE, new EqualityNode<>("uid", "alice"))));

        assertEquals(Arrays.asList(
                        "uid=alice,ou=people,dc=example,dc=com",
                        "uid=bob,ou=people,dc=example,dc=com"),
                names(directory.search(new Dn("dc=example,dc=com"), SearchScope.SUBTREE, new PresenceNode("mail"))));

        assertEquals(0, directory.search(new Dn("dc=example,dc=com"), SearchScope.SUBTREE,
                new EqualityNode<>("uid", "missing")).size());
    }

    private InMemoryDirectory directory() {
        return new InMemoryDirectory(Arrays.asList(
                entry("ou=people,dc=example,dc=com", "organizationalUnit", "people", null),
                entry("uid=alice,ou=people,dc=example,dc=com", "inetOrgPerson", "alice", "alice@example.com"),
                entry("uid=bob,ou=people,dc=example,dc=com", "inetOrgPerson", "bob", "bob@example.com"),
                entry("cn=device,ou=devices,ou=people,dc=example,dc=com", "device", null, null)
        ));
    }

    private StaticDirectoryEntry entry(String dn, String objectClass, String uid, String mail) {
        StaticDirectoryEntry.Builder builder = StaticDirectoryEntry.builder()
                .dn(dn)
                .attribute("objectClass", objectClass);
        if (uid != null) {
            builder.attribute("uid", uid);
        }
        if (mail != null) {
            builder.attribute("mail", mail);
        }
        return builder.build();
    }

    private List<String> names(List<StaticDirectoryEntry> entries) {
        return entries.stream().map(entry -> entry.getDn().getName()).collect(java.util.stream.Collectors.toList());
    }
}
