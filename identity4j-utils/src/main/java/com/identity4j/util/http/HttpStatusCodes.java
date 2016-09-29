package com.identity4j.util.http;

/**
 * Encapsulates HTTP status code information
 * 
 * @author gaurav
 *
 */
public class HttpStatusCodes {
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