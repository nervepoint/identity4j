package com.identity4j.connector.jndi.activedirectory;

public enum ActiveDirectorySchemaVersion {
	
	UNKNOWN(Integer.MIN_VALUE),
	PRE_WINDOWS_2000(0),
	WINDOWS_2000(13),
	WINDOWS_2003(30),
	WINDOWS_2003_R2(31),
	WINDOWS_2008(44),
	WINDOWS_2008_R2(47),
	WINDOWS_2012(56),
    WINDOWS_2012_R2(69),  
    WINDWOS_2016(87),
    WINDOWS_2019(88),
	POST_WINDOWS_2019(Integer.MAX_VALUE);
	
	private final int version;
	
	private ActiveDirectorySchemaVersion(int version) {
		this.version = version;
	}
	
	public int getVersion() {
		return version;
	}
}
