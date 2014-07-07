/* HEADER */
package com.identity4j.connector.directory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.simple.AbstractParameterizedContextMapper;
import org.springframework.ldap.support.LdapUtils;

import com.identity4j.connector.principal.Principal;
import com.identity4j.util.StringUtil;

/**
 * Implementation of the
 * {@link org.springframework.ldap.core.simple.ParameterizedContextMapper}
 * interface. Uses Java 5 covariant return types to override the return type of
 * the {@link #mapFromContext(DirContextAdapter)} method to be the type
 * parameter T.
 * 
 * @param <T> the principal type
 */
public abstract class PrincipalContextMapper<T extends Principal> extends AbstractParameterizedContextMapper<T> {
	final static Log LOG = LogFactory.getLog(PrincipalContextMapper.class);

	@Override
	protected final T doMapFromContext(DirContextOperations result) {
		T principal = mapFromContext(result);
		setAttributes(principal, result);
		return principal;
	}

	protected boolean isAttributeMapped(Attribute attribute) {
		return true;
	}

	/**
	 * @param result
	 * @return
	 */
	protected abstract T mapFromContext(DirContextOperations result);

	private Map<String, String[]> setAttributes(T principal, DirContextOperations result) {
		Map<String, String[]> attributes = new HashMap<String, String[]>();
		for (NamingEnumeration<? extends Attribute> attributeEmun = result.getAttributes().getAll(); attributeEmun
			.hasMoreElements();) {
			Attribute attribute = attributeEmun.nextElement();
			if (isAttributeMapped(attribute)) {
				principal.setAttribute(attribute.getID(), getElements(attribute));
			}
		}
		return attributes;
	}

	private static String[] getElements(Attribute attribute) {
		Collection<String> values = new ArrayList<String>();
		for (int index = 0; index < attribute.size(); index++) {
			try {
				// TODO how to decide how non-string attributes are converted
				final Object object = attribute.get(index);
				if (object instanceof byte[]) {
					values.add(StringUtil.convertByteToString((byte[]) object));
				} else if (object instanceof String || object instanceof Number || object instanceof Boolean) {
					values.add(object.toString());
				} else {
					LOG.warn("Unknown attribute class, assuming String.");
					values.add(object.toString());
				}
			} catch (javax.naming.NamingException nme) {
				throw LdapUtils.convertLdapException(nme);
			}
		}
		return values.toArray(new String[values.size()]);
	}
}