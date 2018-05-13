package org.openecomp.sdc.cucumber.runners;

import org.junit.runner.RunWith;
import org.openecomp.sdc.be.components.BeConfDependentTest;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "classpath:cucumber/tenantIsolation.feature", glue = "org.openecomp.sdc.be.components.distribution.engine")

public class RunTenantIsolationTest  extends BeConfDependentTest{



}