package org.openecomp.sdc.be.components.distribution.engine;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses(

{ DistributionEngineInitTaskTest.class, DistributionEngineConfigTest.class, DistributionEngineHealthCheckTest.class,
		VfModuleArtifactPayloadTest.class })
public class TestSuite { // nothing
}
