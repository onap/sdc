package org.openecomp.sdc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses(

{ ErrorConfigurationTest.class, org.openecomp.sdc.be.TestSuite.class })
public class TestSuite { // nothing
}
