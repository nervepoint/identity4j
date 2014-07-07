/* HEADER */
package com.identity4j.connector.directory;

import java.util.Collection;

import javax.naming.Name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.simple.AbstractParameterizedContextMapper;

public abstract class AbstractFilteredContextMapper<T> extends AbstractParameterizedContextMapper<T> {
	private static final Log LOG = LogFactory.getLog(AbstractFilteredContextMapper.class);
	private final Collection<Name> includedOus;
	private final Collection<Name> excludedOus;

	public AbstractFilteredContextMapper(Collection<Name> includedOus, Collection<Name> excludedOus) {
		this.includedOus = includedOus;
		this.excludedOus = excludedOus;
	}

	@Override
	protected final T doMapFromContext(DirContextOperations ctx) {
		if (isDnValid(ctx.getDn(), includedOus, excludedOus)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Included result " + ctx.getNameInNamespace());
			}
			return onMapFromContext(ctx);
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Excluded result " + ctx.getNameInNamespace());
		}
		return null;
	}

	private boolean isDnValid(Name name, Collection<Name> includedOus, Collection<Name> excludedOus) {
		boolean included = isInOuList(name, includedOus);
		boolean notExcluded = !isInOuList(name, excludedOus);
		return included && notExcluded;
	}

	private boolean isInOuList(Name name, Collection<Name> names) {
		for (Name dnToCheck : names) {
			if (name.toString().toLowerCase().endsWith(dnToCheck.toString().toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	protected abstract T onMapFromContext(DirContextOperations ctx);
}