package org.openecomp.sdc.webseal.simulator;


import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public final class MutableHttpServletRequest extends HttpServletRequestWrapper {
	// holds custom header and value mapping
	private final Map<String, String> customHeaders;

	public MutableHttpServletRequest(HttpServletRequest request){
		super(request);
		this.customHeaders = new HashMap<String, String>();
	}
	
	public void putHeader(String name, String value){
		this.customHeaders.put(name, value);
	}

	public String getHeader(String name) {
		// check the custom headers first
		String headerValue = customHeaders.get(name);
		
		if (headerValue != null){
			return headerValue;
		}
		// else return from into the original wrapped object
		return ((HttpServletRequest) getRequest()).getHeader(name);
	}

	public Enumeration<String> getHeaderNames() {
		// create a set of the custom header names
		Set<String> set = new HashSet<String>(customHeaders.keySet());
		
		// now add the headers from the wrapped request object
		@SuppressWarnings("unchecked")
		Enumeration<String> e = ((HttpServletRequest) getRequest()).getHeaderNames();
		while (e.hasMoreElements()) {
			// add the names of the request headers into the list
			String n = e.nextElement();
			set.add(n);
		}

		// create an enumeration from the set and return
		return Collections.enumeration(set);
	}
}

