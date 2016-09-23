package com.identity4j.connector.jndi.directory;

public class DirectoryOU {

	String dn;
	String name;
	
	DirectoryOU(String dn, String name) {
		this.dn = dn;
		this.name = name;
	}
	
	public String getDn() {
		return dn;
	}
	public void setDn(String dn) {
		this.dn = dn;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
}
