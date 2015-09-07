/* HEADER */
package com.identity4j.connector.principal;

/**
 * Implementation of a {@link Role}.
 */
public class RoleImpl extends AbstractPrincipal implements Role {
	
	private static final long serialVersionUID = 238667374263002867L;

	/**
     * @param guid
     * @param roleName
     */
    public RoleImpl(String guid, String roleName) {
        super(guid, roleName);
    }

    @Override
    public final boolean equals(Object obj) {
        if (!(obj instanceof Role)) {
            return false;
        }
        Role role = (Role) obj;
        return getGuid().equals(role.getGuid());
    }

}