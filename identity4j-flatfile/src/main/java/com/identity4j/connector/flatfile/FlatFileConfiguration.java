/* HEADER */
package com.identity4j.connector.flatfile;

import com.identity4j.connector.Connector;
import com.identity4j.util.MultiMap;

public final class FlatFileConfiguration extends AbstractFlatFileConfiguration {

    public FlatFileConfiguration(MultiMap configurationParameters) {
        super(configurationParameters);
    }

	@Override
	public Class<? extends Connector<?>> getConnectorClass() {
		return FlatFileConnector.class;
	}
}