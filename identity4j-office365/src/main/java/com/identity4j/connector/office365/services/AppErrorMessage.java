package com.identity4j.connector.office365.services;

/*
 * #%L
 * Identity4J OFFICE 365
 * %%
 * Copyright (C) 2013 - 2017 LogonBox
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

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
		private String message;
		@JsonIgnore
		private String[] values;
		private InnerError innerError;
		
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		public String[] getValues() {
			return values;
		}
		public void setValues(String[] values) {
			this.values = values;
		}
		public InnerError getInnerError() {
			return innerError;
		}
		public void setInnerError(InnerError innerError) {
			this.innerError = innerError;
		}
	}
	
	public static class InnerError {
		private String data;
		private String requestId;
		private String clientRequestId;
		public String getData() {
			return data;
		}
		public void setData(String data) {
			this.data = data;
		}
		public String getRequestId() {
			return requestId;
		}
		public void setRequestId(String requestId) {
			this.requestId = requestId;
		}
		public String getClientRequestId() {
			return clientRequestId;
		}
		public void setClientRequestId(String clientRequestId) {
			this.clientRequestId = clientRequestId;
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
