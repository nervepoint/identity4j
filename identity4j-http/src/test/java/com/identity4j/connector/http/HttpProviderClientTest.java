package com.identity4j.connector.http;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.identity4j.util.http.Http;
import com.identity4j.util.http.HttpData;
import com.identity4j.util.http.HttpPair;
import com.identity4j.util.http.HttpProviderClient;
import com.identity4j.util.http.HttpResponse;

public class HttpProviderClientTest {
	private Server server;
	private int port;

	int findPort() {
		int start = (int) (Math.random() * 40000) + 20000;
		for (int i = start; i < start + 1000; i++) {
			try {
				ServerSocket ss = new ServerSocket(i);
				try {
					ss.setReuseAddress(true);
					return i;
				} finally {
					ss.close();
				}
			} catch (IOException ioe) {

			}

		}
		throw new IllegalStateException("Could not find a port to listen on for HTTP.");
	}

	@Before
	public void startServer() throws Exception {
		server = new Server(port = findPort());
		server.setStopAtShutdown(true);
		WebAppContext webAppContext = new WebAppContext();
		webAppContext.setContextPath("/test");
		webAppContext.setResourceBase("src/test/webapp");
		webAppContext.setClassLoader(getClass().getClassLoader());
		webAppContext.addServlet(PostServlet.class, "/post");
		webAppContext.addServlet(PostJsonServlet.class, "/post-json");
		webAppContext.addServlet(GetServlet.class, "/get");
		server.setHandler(webAppContext);
		server.start();
	}

	@Test
	public void test404() throws IOException {
		HttpProviderClient c = Http.getProvider().getClient("http://localhost:" + port + "/test/NOFILE.html");
		HttpResponse r = c.get(null);
		assertEquals(404, r.status().getCode());
	}

	@Test
	public void testGet() throws IOException {
		HttpProviderClient c = Http.getProvider().getClient("http://localhost:" + port + "/test/index.html");
		HttpResponse r = c.get(null);
		assertEquals(200, r.status().getCode());
		assertEquals("text/html", Http.getMIMEType(r));
		compareStreams(r.contentStream(), new FileInputStream(new File("src/test/webapp/index.html")));
	}
	@Test
	public void testGetParameters() throws IOException {
		HttpProviderClient c = Http.getProvider().getClient("http://localhost:" + port + "/test/get?" + Http.encodeParameters(new HttpPair("field1", "value1"), new HttpPair("field2", "value2")));
		HttpResponse r = c.get(null);
		assertEquals(200, r.status().getCode());
		assertEquals("text/plain", Http.getMIMEType(r));
		assertEquals("Got field1=value1,field2=value2", r.contentString());
	}

	@Test
	public void testPost() throws IOException {
		HttpProviderClient c = Http.getProvider().getClient("http://localhost:" + port + "/test/post");
		HttpResponse r = c.post(null, Arrays.asList(new HttpPair("field1", "value1")));
		assertEquals(200, r.status().getCode());
		assertEquals("Got field1=value1", r.contentString());
	}

	@Test
	public void testPostJson() throws IOException {
		HttpProviderClient c = Http.getProvider().getClient("http://localhost:" + port + "/test/post-json");
		HttpResponse r = c.post(null, new HttpData() {
			@Override
			public void writeData(OutputStream out) throws IOException {
				out.write("{ field1: 'value1', field2: 'value2' }".getBytes());
			}
		}, new HttpPair("Content-Type", "application/json"));
		assertEquals(200, r.status().getCode());
		assertEquals("application/json", Http.getMIMEType(r));
		assertEquals("{ reply1: 'value1', reply2: 'value2' }", r.contentString());
		assertEquals(38, Http.getContentLength(r));
	}

	@After
	public void shutdownServer() throws Exception {
		server.stop();
	}

	void compareStreams(InputStream in, InputStream in2) throws IOException {
		try {
			int b = 0;
			int a1 = 0, a2 = 0;
			while (a1 != -1) {
				a1 = in.read();
				a2 = in2.read();
				assertEquals("Byte " + b, a1, a2);
				b++;
			}
		} finally {
			try {
				in.close();
			} finally {
				in2.close();
			}
		}
	}

	@SuppressWarnings("serial")
	public final static class GetServlet extends HttpServlet {

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			StringBuilder b = new StringBuilder();
			for(Enumeration<String> en = req.getParameterNames(); en.hasMoreElements(); ) {
				if(b.length() > 0)
					b.append(",");
				String n = en.nextElement();
				b.append(n + "=" +req.getParameter(n));
			}
			resp.setContentType("text/plain");
			resp.getWriter().print("Got " + b.toString());
		}

	}

	@SuppressWarnings("serial")
	public final static class PostServlet extends HttpServlet {

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			StringBuilder b = new StringBuilder();
			for(Enumeration<String> en = req.getParameterNames(); en.hasMoreElements(); ) {
				if(b.length() > 0)
					b.append(",");
				String n = en.nextElement();
				b.append(n + "=" +req.getParameter(n));
			}
			resp.setContentType("text/plain");
			resp.getWriter().print("Got " + b.toString());
		}

	}

	@SuppressWarnings("serial")
	public final static class PostJsonServlet extends HttpServlet {

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			if(req.getContentType().equals("application/json")) {
				resp.setContentType("application/json");
				resp.getWriter().print("{ reply1: 'value1', reply2: 'value2' }");
			}
			else
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Not JSON");
		}

	}
}
