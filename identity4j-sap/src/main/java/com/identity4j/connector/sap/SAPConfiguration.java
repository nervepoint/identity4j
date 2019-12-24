package com.identity4j.connector.sap;

import com.identity4j.connector.jdbc.JDBCConfiguration;
import com.identity4j.util.MultiMap;

/**
 * Configuration class provides access to properties configured
 */
public class SAPConfiguration extends JDBCConfiguration {
	public final static String SAP_SYSNR = "sap.instance";
	public final static String SAP_MULTI_TENANT = "sap.multiTenant";

	@Override
	public String getJDBCDriverName() {
		return "sap";
	}

	@Override
	public Integer getDefaultPort() {
		// https://archive.sap.com/discussions/thread/3764026
		return Integer.parseInt(String.format("3%02d%d", getInstance(), isMultiTenant() ? 13 : 15));
	}

	@Override
	public String getDriverClassName() {
		return "com.sap.db.jdbc.Driver";
	}

	@Override
	public String getJDBUrlProperties(boolean safe) {
		return null;
	}

	public SAPConfiguration(MultiMap configurationParameters) {
		super(configurationParameters);
	}

	@Override
	public String getUsernameHint() {
		return getConfigurationParameters().getStringOrNull(JDBC_USERNAME);
	}

	@Override
	public String getHostnameHint() {
		return getConfigurationParameters().getStringOrNull(JDBC_HOSTNAME);
	}
	
	public int getInstance() {
		return getConfigurationParameters().getIntegerOrDefault(SAP_SYSNR, 0);
	}
	
	public boolean isMultiTenant() {
		return getConfigurationParameters().getBooleanOrDefault(SAP_MULTI_TENANT, false);
	}


}
