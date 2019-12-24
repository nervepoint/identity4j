package com.identity4j.http;

/*
 * #%L
 * Identity4J default HTTP implementation.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import com.identity4j.util.http.HttpData;
import com.identity4j.util.http.HttpException;
import com.identity4j.util.http.HttpPair;
import com.identity4j.util.http.HttpProviderClient;
import com.identity4j.util.http.HttpResponse;
import com.identity4j.util.http.HttpStatus;
import com.identity4j.util.http.HttpUtil;

public class HttpClientImpl implements HttpProviderClient {
	private final static Log LOG = LogFactory.getLog(HttpClientImpl.class);

	private final class Resp implements HttpResponse {
		private final HttpRequestBase method;
		boolean done;
		HttpStatus status = HttpStatus.DEFAULT;
		CloseableHttpClient httpClient;
		CloseableHttpResponse response;
		List<HttpPair> headers = new ArrayList<HttpPair>();

		private Resp(CloseableHttpClient httpClient, HttpRequestBase method) {
			this.method = method;
			this.httpClient = httpClient;
		}

		void checkDone() throws HttpException {
			if (!done) {
				try {
					try {
						response = httpClient.execute(method);
						status = new HttpStatus(response.getStatusLine().getStatusCode(),
								response.getStatusLine().getReasonPhrase(),
								response.getStatusLine().getProtocolVersion().getProtocol());
						for (Header h : response.getAllHeaders()) {
							headers.add(new HttpPair(h.getName(), h.getValue()));
						}
					} catch(HttpException he) {
						throw he;
					} catch(RuntimeException re) {
						throw re;
					} catch (Exception e) {
						throw new HttpException(status, e);
					}
				} finally {
					done = true;
				}
			}
		}

		@Override
		public HttpStatus status() throws HttpException {
			checkDone();
			return status;
		}

		@Override
		public byte[] content() throws HttpException {
			checkDone();
			try {
				return EntityUtils.toByteArray(response.getEntity());
			} catch (IOException e) {
				throw new HttpException(e);
			}
		}

		@Override
		public void release() {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					throw new HttpException(e);
				}
			}
		}

		@Override
		public String contentString() {
			checkDone();
			try {
				if (response != null)
					return EntityUtils.toString(response.getEntity());
			} catch (IOException e) {
				throw new HttpException(e);
			}
			return null;
		}

		@Override
		public List<HttpPair> headers() {
			checkDone();
			return headers;
		}

		@Override
		public InputStream contentStream() throws IOException {
			checkDone();
			if(response != null) {
	            HttpEntity entity = response.getEntity();
	            if(entity != null)
	                return entity.getContent();
			}
            return null;
		}

	}

	private String url;
	//private HttpClientBuilder cl;
	private CloseableHttpClient httpClient;
	private int connectionRequestTimeout = -1;
	private int connectTimeout = -1;
	private int soTimeout = -1;
	private CertificateRequirements certRequirments = CertificateRequirements.DEFAULT;
	private String username;
	private char[] password;
	private String realm;

	public HttpClientImpl(String urlStr, String username, char[] password, String realm) {
		this.username = username;
		this.password = password;
		this.realm = realm;
		this.url = urlStr;
	}

	public String getUrl() {
		return url;
	}

	public String getUsername() {
		return username;
	}

	public char[] getPassword() {
		return password;
	}

	public String getRealm() {
		return realm;
	}

	public int getSoTimeout() {
		return soTimeout;
	}

	public void setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
	}

	public CertificateRequirements getCertRequirments() {
		return certRequirments;
	}

	public void setCertRequirments(CertificateRequirements certRequirments) {
		this.certRequirments = certRequirments;
	}

	public int getConnectionRequestTimeout() {
		return connectionRequestTimeout;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public HttpResponse get(String uri, HttpPair... header) throws HttpException {
		checkClient();
		final HttpGet method = new HttpGet(HttpUtil.concatenateUriParts(url, uri));
		addHeaders(method, header);
		LOG.info(String.format("HTTP GET %s", method.getURI().toString()));
		return new Resp(httpClient, method);
	}

	@Override
	public HttpResponse post(String uri, Collection<HttpPair> parameters, HttpPair... header) throws HttpException {
		checkClient();
		final HttpPost method = new HttpPost(HttpUtil.concatenateUriParts(url, uri));
		addHeaders(method, header);
		LOG.info(String.format("HTTP POST %s", method.getURI().toString()));
		try {
			method.setEntity(new UrlEncodedFormEntity(httpPairToNameValuePair(parameters)));
		} catch (UnsupportedEncodingException e) {
			throw new HttpException(e);
		}
		return new Resp(httpClient, method);
	}

	@Override
	public HttpResponse post(String uri, String data, HttpPair... header) {
		checkClient();
		final HttpPost method = new HttpPost(HttpUtil.concatenateUriParts(url, uri));
		LOG.info(String.format("HTTP POST %s", method.getURI().toString()));
		return doContentRequest(data, method, header);
	}

	@Override
	public HttpResponse post(String uri, final HttpData data, HttpPair... header) {
		checkClient();
		final HttpPost method = new HttpPost(HttpUtil.concatenateUriParts(url, uri));
		addHeaders(method, header);
		method.removeHeaders("Content-Length");
		LOG.info(String.format("HTTP POST %s", method.getURI().toString()));
		EntityTemplate entity = new EntityTemplate(new ContentProducer() {
			@Override
			public void writeTo(OutputStream outstream) throws IOException {
				data.writeData(outstream);
			}
		});
		method.setEntity(entity);
		entity.setContentType(extractContentType(header).toString());
		return new Resp(httpClient, method);
	}

	@Override
	public HttpResponse put(String uri, final HttpData data, HttpPair... headers) {
		checkClient();

		final HttpPut method = new HttpPut(HttpUtil.concatenateUriParts(url, uri));
		addHeaders(method, headers);
		LOG.info(String.format("HTTP PUT %s", method.getURI().toString()));
		EntityTemplate entity = new EntityTemplate(new ContentProducer() {
			@Override
			public void writeTo(OutputStream outstream) throws IOException {
				data.writeData(outstream);
			}
		});
		entity.setContentType(extractContentType(headers).toString());
		method.setEntity(entity);
		return new Resp(httpClient, method);
	}

	@Override
	public HttpResponse patch(String uri, String data, HttpPair... headers) {
		checkClient();
		final HttpPatch method = new HttpPatch(HttpUtil.concatenateUriParts(url, uri));
		LOG.info(String.format("HTTP PATCH %s", method.getURI().toString()));
		return doContentRequest(data, method, headers);
	}

	@Override
	public HttpResponse put(String uri, String data, HttpPair... headers) {
		checkClient();
		final HttpPut method = new HttpPut(HttpUtil.concatenateUriParts(url, uri));
		LOG.info(String.format("HTTP PATCH %s", method.getURI().toString()));
		return doContentRequest(data, method, headers);
	}

	@Override
	public HttpResponse delete(String uri, HttpPair... headers) {
		checkClient();
		final HttpDelete method = new HttpDelete(HttpUtil.concatenateUriParts(url, uri));
		addHeaders(method, headers);
		LOG.info(String.format("HTTP DELETE %s", method.getURI().toString()));
		return new Resp(httpClient, method);
	}

	@Override
	public void setSocketTimeout(int ms) {
		if (httpClient != null)
			throw new IllegalStateException("Cannot set socket timeout after client has been created.");
		soTimeout = ms;

	}

	@Override
	public void setConnectTimeout(int ms) {
		if (httpClient != null)
			throw new IllegalStateException("Cannot set connection timeout after client has been created.");
		connectTimeout = ms;

	}

	@Override
	public void setConnectionRequestTimeout(int ms) {
		if (httpClient != null)
			throw new IllegalStateException("Cannot set connection request timeout after client has been created.");
		connectionRequestTimeout = ms;
	}

	@Override
	public void setCertificateRequirements(CertificateRequirements certRequirments) {
		if (httpClient != null)
			throw new IllegalStateException("Cannot set certificate requirements after client has been created.");
		this.certRequirments = certRequirments;
	}

	private HttpResponse doContentRequest(String data, final HttpEntityEnclosingRequestBase method,
			HttpPair... header) {
		addHeaders(method, header);
		ContentType ct = extractContentType(header);
		if (data != null) {
			method.setEntity(new StringEntity(data, ct));
		}
		
		/* TODO I am not convinced by this but it gets things working for now. Apache Http
		 * will throw an exception if this header is presented as it adds it itself. I 
		 * thought this just happened when content is actually provided via an entity
		 * but this does not appear to be the case.
		 */
		if(method.getEntity() == null || method.getEntity().getContentLength() != -1)
			method.removeHeaders("Content-Length");
		
		return new Resp(httpClient, method);
	}

	private ContentType extractContentType(HttpPair... headers) {
		String contentTypeStr = getHeaderValue("Content-Type", headers);
		String[] parts = contentTypeStr.split(";");
		String contentType = null;
		String charset = null;
		for (String p : parts) {
			if (contentType == null) {
				contentType = p.trim();
			} else {
				int idx = p.indexOf('=');
				String n = p;
				String v = null;
				if (idx != -1) {
					n = p.substring(0, idx);
					v = p.substring(idx + 1);
				}
				if (n.equalsIgnoreCase("charset")) {
					charset = v;
				}
			}
		}
		ContentType ct = charset == null ? ContentType.create(contentType) : ContentType.create(contentType, charset);
		return ct;
	}

	private String getHeaderValue(String header, HttpPair... headers) {
		for (HttpPair p : headers) {
			if (p.getName().equalsIgnoreCase(header)) {
				return p.getValue();
			}
		}
		throw new IllegalArgumentException("No " + header + " header supplied.");
	}

	private List<NameValuePair> httpPairToNameValuePair(Collection<HttpPair> parameters) {
		List<NameValuePair> n = new LinkedList<NameValuePair>();
		for (HttpPair h : parameters)
			n.add(new BasicNameValuePair(h.getName(), h.getValue()));
		return n;
	}

	protected void addHeaders(HttpRequestBase method, HttpPair... header) {
		for (HttpPair pair : header) {
			method.addHeader(new BasicHeader(pair.getName(), pair.getValue()));
		}
	}

	private void checkClient() {
		if (httpClient == null) {
			try {
				httpClient = createHttpClient();
			} catch (IOException e) {
				throw new HttpException(e);
			}
		}
	}

	protected CloseableHttpClient createHttpClient() throws IOException {
		HttpClientBuilder cl = HttpClients.custom();

		try {
			URL url = new URL(this.url);
			if (username != null && username.length() > 0) {
				CredentialsProvider credsProvider = new BasicCredentialsProvider();
				credsProvider.setCredentials(
						new AuthScope(url.getHost(), url.getPort() == -1 ? url.getDefaultPort() : url.getPort(),
								realm.length() == 0 ? AuthScope.ANY_REALM : realm),
						new UsernamePasswordCredentials(username, password == null ? null : new String(password)));
				cl.setDefaultCredentialsProvider(credsProvider);

			}
		} catch (MalformedURLException mrle) {
			throw new IllegalArgumentException(mrle);
		}
		
		Builder builder = RequestConfig.custom();
		if (soTimeout != -1)
			builder.setSocketTimeout(soTimeout);
		if (connectTimeout != -1)
			builder.setConnectTimeout(connectTimeout);
		if (connectionRequestTimeout != -1)
			builder.setConnectionRequestTimeout(connectionRequestTimeout);

		switch (certRequirments) {
		case STRICT:
			Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory> create()
					.register("http", new PlainConnectionSocketFactory())
					.register("https", new SSLConnectionSocketFactory(SSLContexts.createSystemDefault())).build();
			cl.setConnectionManager(new BasicHttpClientConnectionManager(reg));
			break;
		case LOOSE:
			reg = RegistryBuilder.<ConnectionSocketFactory> create()
					.register("http", new PlainConnectionSocketFactory()).register("https",
							new SSLConnectionSocketFactory(SSLContexts.createSystemDefault(), new HostnameVerifier() {

								@Override
								public boolean verify(String hostname, SSLSession session) {
									return true;
								}
							}))
					.build();
			cl.setConnectionManager(new BasicHttpClientConnectionManager(reg));
			break;
		case NONE:
			reg = RegistryBuilder.<ConnectionSocketFactory> create()
					.register("http", new PlainConnectionSocketFactory()).register("https",
							new SSLConnectionSocketFactory(SSLContexts.createSystemDefault(), new HostnameVerifier() {

								@Override
								public boolean verify(String hostname, SSLSession session) {
									return true;
								}
							}))
					.build();
			cl.setConnectionManager(new BasicHttpClientConnectionManager(reg));
			break;
		case DEFAULT:
			break;
		}

		cl.setDefaultRequestConfig(builder.build());
		return cl.build();
	}

}