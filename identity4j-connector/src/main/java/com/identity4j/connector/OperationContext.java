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
	
	static OperationContext createDefault(OperationContext delegate, String newTag) {
		return new OperationContext() {
			
			@Override
			public String getTag() {
				return newTag;
			}
			
			@Override
			public UserGroupRelationshipCache getRelationshipCache() {
				return delegate.getRelationshipCache();
			}
		};
	}
	
	static OperationContext createDefault(String newTag) {
		return createDefault(createDefault(), newTag);
	}

	static OperationContext createDefault() {
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
		};
	}
}
