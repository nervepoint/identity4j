package com.identity4j.util.http.request;

import java.io.IOException;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import com.identity4j.util.http.response.HttpResponse;

/**
 * This class is responsible for handling request with JSON body for methods of type POST, PATCH
 * i.e. where a request can have data.
 * 
 * @author gaurav
 */
public class BodyHTTPRequest extends SimpleHttpRequest {


    public BodyHTTPRequest(HttpRequestBase httpRequestBase) {
        super(httpRequestBase);
    }

    /**
     * This method takes Data Body to be sent over HTTP request as String object serviceRequestData
     * 
     * @param httpClient
     * @param serviceRequestData
     * @return HttpResponseData contains returned json and HTTP Status Codes
     * @throws IOException 
     */
    public HttpResponse request(CloseableHttpClient httpClient, String serviceRequestData) throws IOException {
        if (httpRequestBase instanceof HttpEntityEnclosingRequestBase) {
            ((HttpEntityEnclosingRequestBase) httpRequestBase).setEntity(new StringEntity(serviceRequestData));
        }
        return super.request(httpClient);
    }
}
