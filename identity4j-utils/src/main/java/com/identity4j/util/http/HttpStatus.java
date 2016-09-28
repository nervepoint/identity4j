package com.identity4j.util.http;

/**
 * Encapsulates HTTP status information
 * 
 * @author gaurav
 *
 */
public class HttpStatus {
	
	public final static HttpStatus DEFAULT = new HttpStatus(0, null, null);
	
    private final int code;
    private final String error;
    private String protocol;

    public HttpStatus(int code, String error, String protocol) {
		super();
		this.code = code;
		this.error = error;
	}

	public String getLine() {
		return protocol + " " + code + " " + error;
	}
	
	public String getProtocol() {
		return protocol;
	}

	public int getCode() {
        return code;
    }

    public String getError() {
        return error;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + code;
		result = prime * result + ((error == null) ? 0 : error.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HttpStatus other = (HttpStatus) obj;
		if (code != other.code)
			return false;
		if (error == null) {
			if (other.error != null)
				return false;
		} else if (!error.equals(other.error))
			return false;
		return true;
	}

	@Override
    public String toString() {
        return "HTTP Error: " + code + (error == null ? "" : " (" + error + ")");
    }
    
}