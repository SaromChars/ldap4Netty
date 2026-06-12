package priv.sarom.ldap4Netty.ldap.directory;

import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StaticDirectoryEntry {
    private final Dn dn;
    private final Map<String, List<String>> attributes;

    private StaticDirectoryEntry(Dn dn, Map<String, List<String>> attributes) {
        this.dn = dn;
        this.attributes = attributes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Dn getDn() {
        return dn;
    }

    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    boolean hasAttribute(String attribute) {
        return attributes.containsKey(normalizeAttribute(attribute));
    }

    boolean hasValue(String attribute, String value) {
        List<String> values = attributes.get(normalizeAttribute(attribute));
        return values != null && values.stream().anyMatch(existing -> existing.equalsIgnoreCase(value));
    }

    public Entry toEntry() throws LdapException {
        Entry entry = new DefaultEntry(dn);
        for (Map.Entry<String, List<String>> attribute : attributes.entrySet()) {
            entry.add(attribute.getKey(), attribute.getValue().toArray(new String[0]));
        }
        return entry;
    }

    private static String normalizeAttribute(String attribute) {
        if (attribute == null || attribute.trim().isEmpty()) {
            throw new IllegalArgumentException("LDAP attribute name must not be blank");
        }
        return attribute.trim().toLowerCase(Locale.ROOT);
    }

    public static class Builder {
        private String dn;
        private final Map<String, List<String>> attributes = new LinkedHashMap<>();

        public Builder dn(String dn) {
            this.dn = dn;
            return this;
        }

        public Builder attribute(String name, String... values) {
            List<String> attributeValues = new ArrayList<>();
            if (values != null) {
                Collections.addAll(attributeValues, values);
            }
            return attributes(name, attributeValues);
        }

        public Builder attributes(String name, List<String> values) {
            String normalizedName = normalizeAttribute(name);
            if (values == null || values.isEmpty()) {
                throw new IllegalArgumentException("LDAP attribute '" + normalizedName + "' must have at least one value");
            }

            List<String> copiedValues = new ArrayList<>(values.size());
            for (String value : values) {
                if (value == null) {
                    throw new IllegalArgumentException("LDAP attribute '" + normalizedName + "' must not contain null values");
                }
                copiedValues.add(value);
            }
            attributes.put(normalizedName, Collections.unmodifiableList(copiedValues));
            return this;
        }

        public StaticDirectoryEntry build() {
            Dn parsedDn;
            try {
                parsedDn = new Dn(dn);
            } catch (LdapInvalidDnException e) {
                throw new IllegalArgumentException("Invalid LDAP entry DN: " + dn, e);
            }
            if (attributes.isEmpty()) {
                throw new IllegalArgumentException("LDAP entry '" + dn + "' must have at least one attribute");
            }
            return new StaticDirectoryEntry(parsedDn, Collections.unmodifiableMap(new LinkedHashMap<>(attributes)));
        }
    }
}
