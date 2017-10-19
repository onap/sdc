package org.openecomp.sdc.fe;

import javax.annotation.Generated;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.junit.Test;


public class GzipFilterTest {

	private GzipFilter createTestSubject() {
		return new GzipFilter();
	}

	

	
	@Test
	public void testInit() throws Exception {
		GzipFilter testSubject;
		FilterConfig filterConfig = null;

		// default test
		testSubject = createTestSubject();
		testSubject.init(filterConfig);
	}

	
	@Test
	public void testDestroy() throws Exception {
		GzipFilter testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.destroy();
	}
}