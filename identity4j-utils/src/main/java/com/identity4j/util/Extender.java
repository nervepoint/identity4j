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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public abstract class Extender implements Serializable {

	private final static ThreadLocal<Extension> extension = new ThreadLocal<Extension>();

	public abstract void extend(String name, String parameter) throws Exception;

	protected Extension getExtension() {
		return extension.get();
	}

	public static Map<String, String> extend(String service) {
		return extend(new Extender() {

			@Override
			public void extend(String name, String parameter) throws Exception {
			}
		}, service);
	}

	public static Map<String, String> extend(Extender extender, String service) {
		try {
			int lastOrder = 0;
			Map<String, String> m = new HashMap<String, String>();
			List<Extension> extensions = new ArrayList<Extender.Extension>();
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			if (cl == null) {
				cl = extender.getClass().getClassLoader();
			}
			for (Enumeration<URL> urle = cl.getResources("META-INF/services/" + service); urle.hasMoreElements();) {
				URL serviceUrl = urle.nextElement();
				BufferedReader reader = new BufferedReader(new InputStreamReader(serviceUrl.openStream()));
				try {
					String line;
					while ((line = reader.readLine()) != null) {
						line = line.trim();
						if (!line.equals("") && !line.startsWith("#")) {
							int idx = line.indexOf("=");
							String name = line;
							String param = null;
							if (idx != -1) {
								name = line.substring(0, idx).trim();
								;
								param = line.substring(idx + 1).trim();
							}

							// Ordering
							int order = lastOrder + 100;
							if (name.startsWith("[")) {
								idx = name.indexOf("]");
								order = Integer.parseInt(name.substring(1, idx));
								name = name.substring(idx + 1);
							}
							lastOrder = order;
							m.put(name, param);
							extensions.add(new Extender.Extension(name, param, order, serviceUrl));
						}
					}
				} finally {
					reader.close();
				}
			}
			Collections.sort(extensions);
			for (Extension e : extensions) {
				extension.set(e);
				extender.extend(e.name, e.parameter);
			}
			return m;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static class Extension implements Comparable<Extension>, Serializable {
		private String name;
		private String parameter;
		private int order;
		private URL source;

		public Extension(String name, String parameter, int order, URL source) {
			this.name = name;
			this.parameter = parameter;
			this.order = order;
			this.source = source;
		}

		public String getName() {
			return name;
		}

		public String getParameter() {
			return parameter;
		}

		public int getOrder() {
			return order;
		}

		public URL getSource() {
			return source;
		}

		@Override
		public int compareTo(Extension other) {
			int o = new Integer(order).compareTo(new Integer(other.order));
			return o == 0 ? name.compareTo(other.name) : o;
		}
	}
}
