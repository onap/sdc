package org.openecomp.sdc.be.unittests.utils;

import org.junit.BeforeClass;
import org.openecomp.sdc.common.test.BaseConfDependent;

public abstract class ModelConfDependentTest extends BaseConfDependent {
	@BeforeClass
	public static void setupBeforeClass() {
        componentName = "catalog-model";
        confPath = "src/test/resources/config";
        setUp();
    }
}
