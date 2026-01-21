package com.identity4j.connector;

/**
 * Supplies some contextual objects and callbacks used during major operations
 * on a connector, such as {@link Connector#allIdentities()} and
 * {@link Connector#allRoles()} which can share caching of users and groups and
 * efficient management user group relationships via this object.
 *
 */
public interface OperationContext {

	String getTag();

	UserGroupRelationshipCache getRelationshipCache();
	
	boolean isGroups();

	static OperationContext createDefault(OperationContext delegate, String newTag) {
		return createDefault(delegate, newTag, true);
	}
	
	static OperationContext createDefault(OperationContext delegate, String newTag, boolean withGroups) {
		return new OperationContext() {
			
			@Override
			public String getTag() {
				return newTag;
			}
			
			@Override
			public UserGroupRelationshipCache getRelationshipCache() {
				return delegate.getRelationshipCache();
			}

			@Override
			public boolean isGroups() {
				return withGroups;
			}
		};
	}
	
	static OperationContext createDefault(String newTag) {
		return createDefault(createDefault(), newTag);
	}

	static OperationContext createDefault() {
		return createDefault(true);
	}

	static OperationContext createDefault(boolean withGroups) {
		return new OperationContext() {
			private UserGroupRelationshipCache cache = new UserGroupRelationshipCache();

			@Override
			public String getTag() {
				return null;
			}

			@Override
			public UserGroupRelationshipCache getRelationshipCache() {
				return cache;
			}

			@Override
			public boolean isGroups() {
				return withGroups;
			}
		};
	}
}
