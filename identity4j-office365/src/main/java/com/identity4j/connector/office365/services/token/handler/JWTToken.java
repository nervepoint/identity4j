package com.identity4j.connector.office365.services.token.handler;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JWTToken {

	@JsonProperty("aud")
	private String aud;
	
	@JsonProperty("iss")
	private String iss;
	
	@JsonProperty("iat")
	private String iat;
	
	@JsonProperty("nbf")
	private String nbf;
	
	@JsonProperty("exp")
	private String exp;
	
	@JsonProperty("ver")
	private String version;
	
	@JsonProperty("tid")
	private String tid;
	
	@JsonProperty("oid")
	private String oid;
	
	@JsonProperty("upn")
	private String upn;
	
	@JsonProperty("sub")
	private String sub;
	
	@JsonProperty("given_name")
	private String givenName;
	
	@JsonProperty("family_name")
	private String familyName;
	
	@JsonProperty("name")
	private String name;
	
	@JsonProperty("amr")
	private List<String> amr = new ArrayList<String>();
	
	@JsonProperty("unique_name")
	private String uniqueName;

	public String getAud() {
		return aud;
	}

	public void setAud(String aud) {
		this.aud = aud;
	}

	public String getIss() {
		return iss;
	}

	public void setIss(String iss) {
		this.iss = iss;
	}

	public String getIat() {
		return iat;
	}

	public void setIat(String iat) {
		this.iat = iat;
	}

	public String getNbf() {
		return nbf;
	}

	public void setNbf(String nbf) {
		this.nbf = nbf;
	}

	public String getExp() {
		return exp;
	}

	public void setExp(String exp) {
		this.exp = exp;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	public String getUpn() {
		return upn;
	}

	public void setUpn(String upn) {
		this.upn = upn;
	}

	public String getSub() {
		return sub;
	}

	public void setSub(String sub) {
		this.sub = sub;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getAmr() {
		return amr;
	}

	public void setAmr(List<String> amr) {
		this.amr = amr;
	}

	public String getUniqueName() {
		return uniqueName;
	}

	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}
	
	
}
