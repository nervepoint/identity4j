package com.identity4j.connector.office365.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class encapsulates error messages sent by Active Directory Graph API.
 * The JSON error object is mapped to this class.
 * 
 * @author gaurav
 *
 */
public class AppErrorMessage {

	public static class Error {
		private String code;
		private Message message;
		@JsonIgnore
		private String[] values;
		
		
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		public Message getMessage() {
			return message;
		}
		public void setMessage(Message message) {
			this.message = message;
		}
		public String[] getValues() {
			return values;
		}
		public void setValues(String[] values) {
			this.values = values;
		}
	}
	public static class Message {
		private String lang;
		private String value;
		public String getLang() {
			return lang;
		}
		public void setLang(String lang) {
			this.lang = lang;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		
	}
	
	@JsonProperty("error")
	private Error error;

	public Error getError() {
		return error;
	}

	public void setError(Error error) {
		this.error = error;
	}
	
}
