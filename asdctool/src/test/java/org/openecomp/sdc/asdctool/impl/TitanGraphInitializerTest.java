package org.openecomp.sdc.asdctool.impl;

import org.junit.Test;

public class TitanGraphInitializerTest {

	@Test(expected=IllegalArgumentException.class)
	public void testCreateGraph() throws Exception {
		String titanCfgFile = "";
		boolean result;

		// default test
		result = TitanGraphInitializer.createGraph("");
		
	}
}