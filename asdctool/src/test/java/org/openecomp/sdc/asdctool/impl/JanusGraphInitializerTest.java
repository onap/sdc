package org.openecomp.sdc.asdctool.impl;

import org.junit.Test;

public class JanusGraphInitializerTest {

	@Test(expected=IllegalArgumentException.class)
	public void testCreateGraph() throws Exception {
		String janusGraphCfgFile = "";
		boolean result;

		// default test
		result = JanusGraphInitializer.createGraph("");
		
	}
}