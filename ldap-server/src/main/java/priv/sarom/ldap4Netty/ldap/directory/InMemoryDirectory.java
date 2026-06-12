package priv.sarom.ldap4Netty.ldap.directory;

import org.apache.directory.api.ldap.model.filter.EqualityNode;
import org.apache.directory.api.ldap.model.filter.ExprNode;
import org.apache.directory.api.ldap.model.filter.LeafNode;
import org.apache.directory.api.ldap.model.filter.PresenceNode;
import org.apache.directory.api.ldap.model.filter.SimpleNode;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InMemoryDirectory {
    private final List<StaticDirectoryEntry> entries;

    public InMemoryDirectory(List<StaticDirectoryEntry> entries) {
        if (entries == null) {
            throw new IllegalArgumentException("directory entries must not be null");
        }
        this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public List<StaticDirectoryEntry> search(Dn baseDn, SearchScope scope, ExprNode filter) {
        List<StaticDirectoryEntry> matches = new ArrayList<>();
        for (StaticDirectoryEntry entry : entries) {
            if (matchesScope(entry.getDn(), baseDn, scope) && matchesFilter(entry, filter)) {
                matches.add(entry);
            }
        }
        return matches;
    }

    private boolean matchesScope(Dn entryDn, Dn baseDn, SearchScope scope) {
        if (scope == SearchScope.OBJECT) {
            return entryDn.equals(baseDn);
        }
        if (scope == SearchScope.ONELEVEL) {
            return entryDn.getParent() != null && entryDn.getParent().equals(baseDn);
        }
        return entryDn.equals(baseDn) || entryDn.isDescendantOf(baseDn);
    }

    private boolean matchesFilter(StaticDirectoryEntry entry, ExprNode filter) {
        if (filter == null) {
            return true;
        }
        if (filter instanceof PresenceNode) {
            return entry.hasAttribute(((PresenceNode) filter).getAttribute());
        }
        if (filter instanceof EqualityNode) {
            SimpleNode<?> node = (SimpleNode<?>) filter;
            return entry.hasValue(((LeafNode) filter).getAttribute(), node.getValue().getString());
        }
        return false;
    }
}
