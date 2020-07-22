/* HEADER */
package com.identity4j.util;

/*
 * #%L
 * Identity4J Utils
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


import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilTest {

	private static final List<String> STRING2_LIST = Arrays.asList("value^One", "valueTwo\n", "\rvalueThree");
	private static final List<String> STRING1_LIST = Arrays.asList("value!One", "valueTwo\r", "\tvalueThree");
	private static final String STRING2_FORMATTED_FOR_STORAGE = "value\\^One^valueTwo\\n^\\rvalueThree";
	private static final String STRING1_FORMATTED_FOR_STORAGE = "value\\!One!valueTwo\\r!\\tvalueThree";

	@Test
	public void testNullOrTrimmedBlank() {
		Assert.assertTrue(StringUtil.isArrayNullOrEmpty(null));
		Assert.assertTrue(StringUtil.isNullOrEmpty(""));
		Assert.assertFalse(StringUtil.isNullOrEmpty("Not Null Or Blank!"));
	}

	@Test
	public void testNonNull() {
		Assert.assertEquals(StringUtil.nonNull(null), "");
		Assert.assertEquals(StringUtil.nonNull(""), "");
		Assert.assertEquals(StringUtil.nonNull(" "), " ");
	}

	@Test
	public void testTrim() {
		Assert.assertEquals(StringUtil.trim(null), "");
		Assert.assertEquals(StringUtil.trim(""), "");
		Assert.assertEquals(StringUtil.trim(" "), "");
	}

	@Test
	public void testGetBefore() {
		Assert.assertEquals(StringUtil.getBefore(null, null), "");
		Assert.assertEquals(StringUtil.getBefore("", ""), "");
		Assert.assertEquals(StringUtil.getBefore("something", "@"), "something");
		Assert.assertEquals(StringUtil.getBefore("something@another.com", "@"), "something");
		Assert.assertEquals(StringUtil.getBefore("something@another@com", "@"), "something");
	}

	@Test
	public void testGetAfter() {
		Assert.assertEquals(StringUtil.getAfter(null, null), "");
		Assert.assertEquals(StringUtil.getAfter("", ""), "");
		Assert.assertEquals(StringUtil.getAfter("something", "@"), "something");
		Assert.assertEquals(StringUtil.getAfter("something@another.com", "@"), "another.com");
		Assert.assertEquals(StringUtil.getAfter("something@another@com", "@"), "another@com");
	}

	@Test
	public void toArray() {
		Assert.assertArrayEquals(StringUtil.toDefaultArray(null), new String[] {});
		Assert.assertArrayEquals(StringUtil.toDefaultArray(""), new String[] {});
		Assert.assertArrayEquals(StringUtil.toDefaultArray("!"), new String[] { "", "" });
		Assert.assertArrayEquals(StringUtil.toDefaultArray("valueOne!valueTwo!valueThree"), new String[] { "valueOne", "valueTwo",
			"valueThree" });
		Assert.assertArrayEquals(StringUtil.toArray("valueOne^valueTwo^valueThree", '^'), new String[] { "valueOne", "valueTwo",
			"valueThree" });
	}

	@Test
	public void toList() {
		Assert.assertEquals(StringUtil.toDefaultList(null), Collections.emptyList());
		Assert.assertEquals(StringUtil.toDefaultList(""), Collections.emptyList());
		Assert.assertEquals(StringUtil.toDefaultList("!"), Arrays.asList("", ""));
		Assert.assertEquals(StringUtil.toDefaultList("valueOne!valueTwo!valueThree"),
			Arrays.asList("valueOne", "valueTwo", "valueThree"));
		Assert.assertEquals(StringUtil.toList("valueOne^valueTwo^valueThree", '^'),
			Arrays.asList("valueOne", "valueTwo", "valueThree"));
	}

	@Test
	public void toListWithEscapes() {
		Assert.assertEquals(StringUtil.toDefaultList(STRING1_FORMATTED_FOR_STORAGE), STRING1_LIST);
		Assert.assertEquals(StringUtil.toList(STRING2_FORMATTED_FOR_STORAGE, '^'), STRING2_LIST);
	}

	@Test
	public void toStringWithEscapes() {
		Assert.assertEquals(StringUtil.toDefaultString((String[]) STRING1_LIST.toArray()), STRING1_FORMATTED_FOR_STORAGE);
		Assert.assertEquals(StringUtil.toString("^", (String[]) STRING2_LIST.toArray()), STRING2_FORMATTED_FOR_STORAGE);
	}

	@Test
	public void testUpperCamelCase() {
		Assert.assertEquals(StringUtil.upperCamelCase("valueOne valueTwo valueThree"), "ValueOneValueTwoValueThree");
		Assert.assertEquals(StringUtil.upperCamelCase("valueOne.valueTwo.valueThree"), "ValueOne.ValueTwo.ValueThree");
		Assert.assertEquals(StringUtil.upperCamelCase("Value One Value Two Value Three"), "ValueOneValueTwoValueThree");
	}
}