package com.identity4j.util.expect;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.identity4j.util.expect.Expect;
import com.identity4j.util.expect.ExpectTimeoutException;

public class ExpectTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testExpectString() throws ExpectTimeoutException, IOException {
		Expect e = new Expect(getClass().getResourceAsStream("testExpectString.txt"), System.out);
		assertTrue(e.expect("string4"));
		assertTrue(e.expect("string7"));
	}

	@Test
	public void testExpectStringNoMatch() throws ExpectTimeoutException, IOException {
		Expect e = new Expect(getClass().getResourceAsStream("testExpectString.txt"), System.out);
		assertFalse(e.expect("string99"));
	}
}
