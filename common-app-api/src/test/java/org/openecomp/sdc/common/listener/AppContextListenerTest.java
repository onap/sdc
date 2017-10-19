package org.openecomp.sdc.common.listener;

import java.util.Map;

import javax.annotation.Generated;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.junit.Test;


public class AppContextListenerTest {

	private AppContextListener createTestSubject() {
		return new AppContextListener();
	}

	


	
	@Test
	public void testContextDestroyed() throws Exception {
		AppContextListener testSubject;
		ServletContextEvent context = null;

		// default test
		testSubject = createTestSubject();
		testSubject.contextDestroyed(context);
	}



	
//	@Test
	public void testGetManifestInfo() throws Exception {
		ServletContext application = null;
		Map<String, String> result;

		// default test
		result = AppContextListener.getManifestInfo(application);
	}
}