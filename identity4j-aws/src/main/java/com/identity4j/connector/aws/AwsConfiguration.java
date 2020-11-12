package com.identity4j.connector.aws;

import com.identity4j.connector.AbstractConnectorConfiguration;
import com.identity4j.connector.Connector;
import com.identity4j.util.MultiMap;

public class AwsConfiguration extends AbstractConnectorConfiguration {
	
	public static final String AWS_REGION = "awsRegion";
	public static final String AWS_ACCESS_KEY_ID = "accessKeyId";
	public static final String AWS_SECRET_ACCESS_KEY = "secretAccessKey";
	
	

	public AwsConfiguration(MultiMap configurationParameters) {
		super(configurationParameters);
	}

	@Override
	public String getUsernameHint() {
		return null;
	}

	@Override
	public String getHostnameHint() {
		return null;
	}
	
	public String getAwsRegion() {
		return configurationParameters.getString(AWS_REGION);
	}
	
	public String getAwsAccessKeyId() {
		return configurationParameters.getString(AWS_ACCESS_KEY_ID);
	}
	
	public String getAwsSecretAccessKey() {
		return configurationParameters.getString(AWS_SECRET_ACCESS_KEY);
	}
	
	@Override
	public Class<? extends Connector<?>> getConnectorClass() {
		return AwsConnector.class;
	}

}
