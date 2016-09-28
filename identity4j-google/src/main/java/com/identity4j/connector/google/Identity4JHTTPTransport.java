package com.identity4j.connector.google;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.identity4j.util.http.Http;
import com.identity4j.util.http.HttpData;
import com.identity4j.util.http.HttpPair;
import com.identity4j.util.http.HttpProviderClient;
import com.identity4j.util.http.HttpResponse;

public class Identity4JHTTPTransport extends HttpTransport {

	@Override
	protected LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
		if (method.equals(HttpMethods.DELETE)) {
			return new DeleteReq(url);
		} else if (method.equals(HttpMethods.GET)) {
			return new GetReq(url);
		} else if (method.equals(HttpMethods.POST)) {
			return new PostReq(url);
		} else if (method.equals(HttpMethods.PUT)) {
			return new PutReq(url);
		}
		throw new UnsupportedOperationException();
	}

	abstract class Req extends LowLevelHttpRequest {

		private List<HttpPair> headers = new LinkedList<HttpPair>();
		private String url;
		private int connectTimeout = -1;
		private int readTimeout = -1;

		Req(String url) {
			this.url = url;
		}

		@Override
		public void addHeader(String name, String value) throws IOException {
			headers.add(new HttpPair(name, value));
		}

		public void setTimeout(int connectTimeout, int readTimeout) throws IOException {
			this.connectTimeout = connectTimeout;
			this.readTimeout = readTimeout;
		}

		@Override
		public LowLevelHttpResponse execute() throws IOException {
			List<HttpPair> headers = new LinkedList<HttpPair>(this.headers);
			if(getContentType() != null)
				headers.add(new HttpPair("Content-Type", getContentType()));
			if(getContentEncoding() != null)
				headers.add(new HttpPair("Content-Encoding", getContentEncoding()));
			if(getContentLength() > -1)
				headers.add(new HttpPair("Content-Length", String.valueOf(getContentLength())));
			
			HttpProviderClient client = Http.getProvider().getClient(url);
			if (connectTimeout != -1)
				client.setConnectTimeout(connectTimeout);
			if (readTimeout != -1)
				client.setSocketTimeout(readTimeout);
			final HttpResponse resp = doMethod(client, headers);
			return new LowLevelHttpResponse() {

				@Override
				public String getStatusLine() throws IOException {
					return resp.status().getLine();
				}

				@Override
				public int getStatusCode() throws IOException {
					return resp.status().getCode();
				}

				@Override
				public String getReasonPhrase() throws IOException {
					return resp.status().getError();
				}

				@Override
				public String getHeaderValue(int index) throws IOException {
					return resp.headers().get(index).getValue();
				}

				@Override
				public String getHeaderName(int index) throws IOException {
					return resp.headers().get(index).getName();
				}

				@Override
				public int getHeaderCount() throws IOException {
					return resp.headers().size();
				}

				@Override
				public String getContentType() throws IOException {
					return Http.getContentType(resp);
				}

				@Override
				public long getContentLength() throws IOException {
					return Http.getContentLength(resp);
				}

				@Override
				public String getContentEncoding() throws IOException {
					return Http.getContentEncoding(resp);
				}

				@Override
				public InputStream getContent() throws IOException {
					return resp.contentStream();
				}
			};
		}

		abstract HttpResponse doMethod(HttpProviderClient client, List<HttpPair> headers);
	}

	class DeleteReq extends Req {

		DeleteReq(String url) {
			super(url);
		}

		@Override
		HttpResponse doMethod(HttpProviderClient client, List<HttpPair> headers) {
			return client.delete(null, headers.toArray(new HttpPair[0]));
		}
	}

	class GetReq extends Req {

		GetReq(String url) {
			super(url);
		}

		@Override
		HttpResponse doMethod(HttpProviderClient client, List<HttpPair> headers) {
			return client.get(null, headers.toArray(new HttpPair[0]));
		}
	}
	
	abstract class ContentReq extends Req {
		ContentReq(String url) {
			super(url);
		}

		@Override
		final HttpResponse doMethod(HttpProviderClient client, List<HttpPair> headers) {
			if(getStreamingContent() != null) {
				return doMethodStream(client, headers, new HttpData() {
					
					@Override
					public void writeData(OutputStream out) throws IOException {
						getStreamingContent().writeTo(out);						
					}
				});	
			}
			else {
				return doMethodNoStream(client, headers);
			}
		}
		
		abstract HttpResponse doMethodStream(HttpProviderClient client, List<HttpPair> headers, HttpData data);
		
		abstract HttpResponse doMethodNoStream(HttpProviderClient client, List<HttpPair> headers);
	}

	class PutReq extends ContentReq {

		PutReq(String url) {
			super(url);
		}

		@Override
		HttpResponse doMethodStream(HttpProviderClient client, List<HttpPair> headers, HttpData data) {
			return client.put(null, (String)null, headers.toArray(new HttpPair[0]));
		}

		@Override
		HttpResponse doMethodNoStream(HttpProviderClient client, List<HttpPair> headers) {
			return client.put(null, (String)null, headers.toArray(new HttpPair[0]));
		}
	}

	class PostReq extends ContentReq {

		PostReq(String url) {
			super(url);
		}

		@Override
		HttpResponse doMethodNoStream(HttpProviderClient client, List<HttpPair> headers) {
			return client.post(null, (String)null, headers.toArray(new HttpPair[0]));
		}

		@Override
		HttpResponse doMethodStream(HttpProviderClient client, List<HttpPair> headers, HttpData data) {
			return client.post(null, data, headers.toArray(new HttpPair[0]));
		}
	}
}
