package com.identity4j.connector;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.Role;

public class UserGroupRelationshipCache {
	
	public enum KeyType {
		NAME, GID, DN
	}
	
	public static class GroupKey {
		private KeyType type;
		private String key;

		public GroupKey(KeyType type, String key) {
			super();
			this.type = type;
			this.key = key;
		}

		public KeyType getType() {
			return type;
		}

		public String getKey() {
			return key;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			GroupKey other = (GroupKey) obj;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			if (type != other.type)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "GroupKey [type=" + type + ", key=" + key + "]";
		}
	}

	private static class CachedRole {
		private Role role;
		private final GroupKey roleKey;
		private Set<String> users = new LinkedHashSet<>();
		private Set<GroupKey> groups = new LinkedHashSet<>();

		CachedRole(GroupKey roleKey) {
			this(roleKey, null);
		}

		CachedRole(GroupKey roleKey, Role role) {
			super();
			this.role = role;
			this.roleKey = roleKey;
		}

		Role getRole(Function<GroupKey, Role> supplier) {
			if (role == null) {
				if (supplier == null)
					throw new IllegalStateException("Group supplier not set, and no pre-created group was supplied.");
				role = supplier.apply(roleKey);
			}
			return role;
		}
	}

	static class CachedIdentity {
		private Identity identity;
		private final String identityKey;
		private Set<GroupKey> groups = new LinkedHashSet<>();

		CachedIdentity(String identityKey) {
			this(identityKey, null);
		}

		CachedIdentity(String identityKey, Identity identity) {
			super();
			this.identity = identity;
			this.identityKey = identityKey;
		}

		private Identity getIdentity(Function<String, Identity> identitySupplier) {
			if (identity == null) {
				if (identitySupplier == null)
					throw new IllegalStateException("User supplier not set, and no pre-created user was supplied.");
				identity = identitySupplier.apply(identityKey);
			}
			return identity;
		}
	}

	private final Map<GroupKey, CachedRole> roles = new HashMap<>();
	private final Map<String, CachedIdentity> identities = new HashMap<>();

	public UserGroupRelationshipCache() {
	}

	public Collection<Identity> getIdentiesForRole(GroupKey roleKey, Function<String, Identity> supplier) {
		return getCachedRole(roleKey).users.stream().map(k -> getIdentity(k, supplier)).collect(Collectors.toList());
	}

	public Collection<Role> getRolesForRole(GroupKey roleKey, Function<GroupKey, Role> supplier) {
		return getCachedRole(roleKey).groups.stream().map(k -> getRole(k, supplier)).collect(Collectors.toList());
	}

	public Collection<Role> getRolesForUser(String identityKey, Function<GroupKey, Role> supplier) {
		CachedIdentity i = getCachedIdentity(identityKey);
		return i.groups.stream().map(k -> getRole(k, supplier))
				.collect(Collectors.toList());
	}

	public void addIdentity(String identityKey) {
		if (!identities.containsKey(identityKey))
			identities.put(identityKey, new CachedIdentity(identityKey, null));
	}

	public void addIdentity(String identityKey, Identity user) {
		if (!identities.containsKey(identityKey))
			identities.put(identityKey, new CachedIdentity(identityKey, user));
	}

	public void addRole(GroupKey roleKey) {
		if (!roles.containsKey(roleKey))
			roles.put(roleKey, new CachedRole(roleKey));
	}

	public void addRole(GroupKey roleKey, Role group) {
		if (!roles.containsKey(roleKey))
			roles.put(roleKey, new CachedRole(roleKey, group));
	}

	public Identity getIdentity(String identityKey, Function<String, Identity> supplier) {
		return getCachedIdentity(identityKey).getIdentity(supplier);
	}

	public Role getRole(GroupKey roleKey, Function<GroupKey, Role> supplier) {
		return getCachedRole(roleKey).getRole(supplier);
	}

	public void addRoleToIdentity(String identityKey, GroupKey roleKey) {
		CachedRole r = getCachedRole(roleKey);
		CachedIdentity i = getCachedIdentity(identityKey);
		r.users.add(identityKey);
		i.groups.add(roleKey);
	}

	public void addRoleToRole(GroupKey roleKey, GroupKey parentKey) {
		CachedRole parent = getCachedRole(parentKey);
		parent.groups.add(roleKey);
	}

	private CachedIdentity getCachedIdentity(String identityKey) {
		if (identities.containsKey(identityKey))
			return identities.get(identityKey);
		else {
			CachedIdentity Identity = new CachedIdentity(identityKey);
			identities.put(identityKey, Identity);
			return Identity;
		}
	}

	private CachedRole getCachedRole(GroupKey roleKey) {
		if (roles.containsKey(roleKey))
			return roles.get(roleKey);
		else {
			CachedRole Role = new CachedRole(roleKey);
			roles.put(roleKey, Role);
			return Role;
		}
	}
}
