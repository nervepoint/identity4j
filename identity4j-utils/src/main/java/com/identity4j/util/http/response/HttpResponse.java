package com.identity4j.util.http.response;

/**
 * This encapsulates HTTP response data and status codes.
 * <br />
 * HTTP status codes are represented by inner class {@link HttpStatusCodes}.
 * <br />
 * HTTP status information is represented by inner class {@link Status}. 
 * 
 * @author gaurav
 * 
 * 
 */
public class HttpResponse {
    protected Status status;
    private Object data;
    protected HttpStatusCodes httpStatusCodes = new HttpStatusCodes();

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public HttpStatusCodes getHttpStatusCodes() {
        return httpStatusCodes;
    }

    public void setHttpStatusCodes(HttpStatusCodes httpStatusCodes) {
        this.httpStatusCodes = httpStatusCodes;
    }
    
    public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result
				+ ((httpStatusCodes == null) ? 0 : httpStatusCodes.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
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
		HttpResponse other = (HttpResponse) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (httpStatusCodes == null) {
			if (other.httpStatusCodes != null)
				return false;
		} else if (!httpStatusCodes.equals(other.httpStatusCodes))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		return true;
	}

	@Override
    public String toString() {
        return "ServiceResponseObject{" +  "status=" + status + ", data=" + data + ", httpStatus=" + httpStatusCodes + '}';
    }

    /**
     * Encapsulates HTTP status information
     * 
     * @author gaurav
     *
     */
	public static class Status{
        private Integer code;
        private String error;

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((code == null) ? 0 : code.hashCode());
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
			Status other = (Status) obj;
			if (code == null) {
				if (other.code != null)
					return false;
			} else if (!code.equals(other.code))
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
            return "Status{" + "code=" + code + ", error=" + error + '}';
        }
        
    }
    
	/**
	 * Encapsulates HTTP status code information
	 * 
	 * @author gaurav
	 *
	 */
    public static class HttpStatusCodes {
            private String protocolVersion;
            private Integer statusCode;
            private String resonPhrase;

            public String getProtocolVersion() {
                return protocolVersion;
            }

            public void setProtocolVersion(String protocolVersion) {
                this.protocolVersion = protocolVersion;
            }

            public Integer getStatusCode() {
                return statusCode;
            }

            public void setStatusCode(Integer statusCode) {
                this.statusCode = statusCode;
            }

            public String getResonPhrase() {
                return resonPhrase;
            }

            public void setResonPhrase(String resonPhrase) {
                this.resonPhrase = resonPhrase;
            }

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime
						* result
						+ ((protocolVersion == null) ? 0 : protocolVersion
								.hashCode());
				result = prime * result
						+ ((resonPhrase == null) ? 0 : resonPhrase.hashCode());
				result = prime * result
						+ ((statusCode == null) ? 0 : statusCode.hashCode());
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
				HttpStatusCodes other = (HttpStatusCodes) obj;
				if (protocolVersion == null) {
					if (other.protocolVersion != null)
						return false;
				} else if (!protocolVersion.equals(other.protocolVersion))
					return false;
				if (resonPhrase == null) {
					if (other.resonPhrase != null)
						return false;
				} else if (!resonPhrase.equals(other.resonPhrase))
					return false;
				if (statusCode == null) {
					if (other.statusCode != null)
						return false;
				} else if (!statusCode.equals(other.statusCode))
					return false;
				return true;
			}

			@Override
            public String toString() {
                return "HttpStatus{" + "protocolVersion=" + protocolVersion + ", statusCode=" + statusCode + ", resonPhrase=" + resonPhrase + '}';
            }
            
            
        }
    
}
