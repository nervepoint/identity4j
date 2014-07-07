package com.identity4j.util.http.request;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;


/**
 * This class is responsible for executing the HTTP request of method type
 * POST,GET,DELETE,PUT... and return HttpResponseData
 *
 * @author gaurav
 */
public class SimpleHttpRequest {

    protected HttpRequestBase httpRequestBase;

    public SimpleHttpRequest(HttpRequestBase httpRequestBase) {
        this.httpRequestBase = httpRequestBase;
    }

    /**
     * Utility method for executing HTTP Methods over the HttpClient passed and
     * return HttpResponse to the client
     *
     * @param httpClient http client that will be used t make the request
     * @return HttpResponseData contains returned json and HTTP Status Codes
     * @throws IOException
     */
    public com.identity4j.util.http.response.HttpResponse request(CloseableHttpClient httpClient) throws IOException {
    	CloseableHttpResponse response = null;
    	try{
	        response = httpClient.execute(httpRequestBase);
	
	        com.identity4j.util.http.response.HttpResponse httpResponseData = new com.identity4j.util.http.response.HttpResponse();
	
	        if(response.getEntity() != null){
		        String data = EntityUtils.toString(response.getEntity());
		        httpResponseData.setData(data);
	        }
	        
	        httpResponseData.getHttpStatusCodes().setProtocolVersion(response.getStatusLine().getProtocolVersion().toString());
	        httpResponseData.getHttpStatusCodes().setStatusCode(response.getStatusLine().getStatusCode());
	        httpResponseData.getHttpStatusCodes().setResonPhrase(response.getStatusLine().getReasonPhrase());
	        
	        return httpResponseData;
    	}finally{
    		if(response != null) response.close();
    	}
    }
}
