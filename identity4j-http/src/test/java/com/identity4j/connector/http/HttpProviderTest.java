package com.identity4j.connector.http;

import org.junit.Assert;
import org.junit.Test;

import com.identity4j.util.http.Http;
import com.identity4j.util.http.HttpProvider;

public class HttpProviderTest {

	@Test
	public void testDefaultProvider() throws Exception {
		HttpProvider prov = Http.getProvider();
		Assert.assertNotNull(prov);
	}
}
