package org.openecomp.sdc.be.utils;

import org.junit.BeforeClass;
import org.openecomp.sdc.common.test.BaseConfDependent;

public class DAOConfDependentTest extends BaseConfDependent {
	@BeforeClass
    public static void setupBeforeClass() {
        componentName = "catalog-dao";
        confPath = "src/test/resources/config";
        setUp();
    }
}
