package com.identity4j.connector.sap;

import java.util.Properties;

import com.identity4j.connector.jdbc.JDBCConfiguration;
import com.identity4j.util.MultiMap;
import com.sap.conn.jco.ext.DestinationDataProvider;

/**
 * Configuration class provides access to properties configured
 */
public class SAPConfiguration2 extends JDBCConfiguration {
	public final static String JCO_ASHOST = "jco.ashost";
	public final static String JCO_SYSNR = "jco.sysnr";
	public final static String JCO_CLIENT = "jco.client";
	public final static String JCO_USER = "jco.user";
	public final static String JCO_PASSWD = "jco.passwd";
	public final static String JCO_LANG = "jco.lang";
	public final static String JCO_MULTI_TENANT = "jco.multiTenant";

	@Override
	public String getJDBCDriverName() {
		return "hdbsql";
	}

	@Override
	public Integer getDefaultPort() {
		// https://archive.sap.com/discussions/thread/3764026
		return Integer.parseInt(String.format("3%02d%d", getInstance(), isMultiTenant() ? 13 : 15));
	}

	@Override
	public String getDriverClassName() {
		return "com.sap.jdbc.Driver";
	}

	@Override
	public String getJDBUrlProperties(boolean safe) {
		StringBuilder buf = new StringBuilder();
		buf.append("user=");
		buf.append(configurationParameters.getString(JDBC_USERNAME));
		buf.append("&");
		buf.append("password=");
		buf.append(configurationParameters.getString(JDBC_PASSWORD));
		return buf.toString();
	}

	public SAPConfiguration2(MultiMap configurationParameters) {
		super(configurationParameters);
	}

	@Override
	public String getUsernameHint() {
		return getConfigurationParameters().getStringOrNull(JCO_USER);
	}

	@Override
	public String getHostnameHint() {
		return getConfigurationParameters().getStringOrNull(JCO_ASHOST);
	}
	
	public int getInstance() {
		return getConfigurationParameters().getIntegerOrDefault(JCO_SYSNR, 0);
	}
	
	public boolean isMultiTenant() {
		return getConfigurationParameters().getBooleanOrDefault(JCO_MULTI_TENANT, false);
	}

	public Properties getDestinationProperties() {
		Properties p = new Properties();
		p.put(DestinationDataProvider.JCO_ASHOST, getHostnameHint());
		p.put(DestinationDataProvider.JCO_SYSNR, String.valueOf(getInstance()));
		p.put(DestinationDataProvider.JCO_CLIENT, getConfigurationParameters().getString(JCO_CLIENT));
		p.put(DestinationDataProvider.JCO_USER, getUsernameHint());
		p.put(DestinationDataProvider.JCO_PASSWD, getConfigurationParameters().getString(JCO_PASSWD));
		p.put(DestinationDataProvider.JCO_LANG, getConfigurationParameters().getString(JCO_LANG));
		return p;
	}

}
