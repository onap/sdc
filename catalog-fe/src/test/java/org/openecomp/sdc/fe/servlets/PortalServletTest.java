package org.openecomp.sdc.fe.servlets;

import java.util.List;

import javax.annotation.Generated;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.openecomp.sdc.common.impl.MutableHttpServletRequest;

import io.netty.handler.codec.http2.Http2FrameReader.Configuration;


public class PortalServletTest {

	private PortalServlet createTestSubject() {
		return new PortalServlet();
	}

	
	@Test
	public void testDoGet() throws Exception {
		PortalServlet testSubject;
		HttpServletRequest request = null;
		HttpServletResponse response = null;

		// default test
		testSubject = createTestSubject();
		testSubject.doGet(request, response);
	}

	
	
}