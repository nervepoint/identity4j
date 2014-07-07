package com.identity4j.connector;

import java.util.Iterator;

public interface BrowseableConnector extends Connector {

	/**
	 * Get all browseable nodes under the supplied parent node. If the parent is
	 * <code>null</code>, the root nodes should be returned.
	 * 
	 * @param parent parent
	 * @return child nodes
	 */
	Iterator<BrowseNode> getBrowseableNodes(BrowseNode parent);
}
