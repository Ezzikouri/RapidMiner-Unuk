/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2012 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.tools;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import javax.xml.ws.BindingProvider;

import sun.net.www.protocol.http.AuthCacheImpl;
import sun.net.www.protocol.http.AuthCacheValue;

/**
 * Some utility methods for web services and url connections.
 * 
 * @author Simon Fischer, Marco Boeck
 *
 */
public class WebServiceTools {

	private static final int TIMEOUT = 20000;
	
	private static final int TIMEOUT_URL_CONNECTION = 20000;

	public static void setTimeout(BindingProvider port) {
		setTimeout(port, TIMEOUT);
	}
	
	/** Sets the timeout for this web service client. Every port created
	 *  by a JAX-WS can be cast to BindingProvider. */
	public static void setTimeout(BindingProvider port, int timeout) {
		if (port == null) {
			throw new IllegalArgumentException("port must not be null!");
		}
		
		Map<String, Object> ctxt = port.getRequestContext();
		ctxt.put("com.sun.xml.ws.developer.JAXWSProperties.CONNECT_TIMEOUT", timeout);
		ctxt.put("com.sun.xml.ws.connect.timeout", timeout);
		ctxt.put("com.sun.xml.ws.internal.connect.timeout", timeout);
		ctxt.put("com.sun.xml.ws.request.timeout", timeout); 
		ctxt.put("com.sun.xml.internal.ws.request.timeout", timeout);
		// We don't want to use proprietary Sun code
//		ctxt.put(BindingProviderProperties.REQUEST_TIMEOUT, timeout);
//		ctxt.put(BindingProviderProperties.CONNECT_TIMEOUT, timeout);
	}
	
	/** Pre-authenticates the Web service if password is not null. */
	public static void setCredentials(BindingProvider bp, String username, char[] password) {
		if (password != null) {	
			bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, username);
			bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, new String(password));
		}
	}
	
	/**
	 * Sets some default settings for {@link URLConnection}s, e.g. timeouts.
	 * @param connection
	 */
	public static void setURLConnectionDefaults(URLConnection connection) {
		if (connection == null) {
			throw new IllegalArgumentException("url must not be null!");
		}
		
		connection.setConnectTimeout(TIMEOUT_URL_CONNECTION);
		connection.setReadTimeout(TIMEOUT_URL_CONNECTION);
	}
	
	/**
	 * Opens an {@link InputStream} from the given {@link URL} and calls
	 * {@link #setURLConnectionDefaults(URLConnection)} on the {@link URLConnection}.
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static InputStream openStreamFromURL(URL url) throws IOException {
		if (url == null) {
			throw new IllegalArgumentException("url must not be null!");
		}
		
		URLConnection connection = url.openConnection();
        setURLConnectionDefaults(connection);
        return connection.getInputStream();
	}

	/** Clears all (Java-)cached credentials for Web services. */
	public static void clearAuthCache() {
		AuthCacheValue.setAuthCache(new AuthCacheImpl());		
	}
}
