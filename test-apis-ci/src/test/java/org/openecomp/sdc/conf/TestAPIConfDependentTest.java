package org.openecomp.sdc.conf;

import org.junit.BeforeClass;
import org.openecomp.sdc.common.test.BaseConfDependent;

public class TestAPIConfDependentTest extends BaseConfDependent{
	@BeforeClass
	public static void setupBeforeClass() {
		componentName = "test-apis-ci";
		confPath = "src/test/resources/config";
		setUp();
	}
}
