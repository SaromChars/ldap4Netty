package priv.sarom.ldap4Netty.ldap.directory;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class StaticDirectoryEntryTest {

    @Test
    public void acceptsValidEntry() {
        StaticDirectoryEntry entry = StaticDirectoryEntry.builder()
                .dn("uid=alice,ou=people,dc=example,dc=com")
                .attribute("objectClass", "inetOrgPerson")
                .attribute("cn", "Alice")
                .attribute("mail", "alice@example.com", "alice@work.example.com")
                .build();

        assertEquals("uid=alice,ou=people,dc=example,dc=com", entry.getDn().getName());
        assertEquals(Arrays.asList("alice@example.com", "alice@work.example.com"), entry.getAttributes().get("mail"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidDn() {
        StaticDirectoryEntry.builder()
                .dn("not a dn")
                .attribute("cn", "Alice")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidAttributeName() {
        StaticDirectoryEntry.builder()
                .dn("uid=alice,dc=example,dc=com")
                .attribute(" ", "Alice")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsAttributeWithoutValues() {
        StaticDirectoryEntry.builder()
                .dn("uid=alice,dc=example,dc=com")
                .attributes("cn", Collections.emptyList())
                .build();
    }
}
