package org.openecomp.sdc.cucumber.runners;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.openecomp.sdc.conf.TestAPIConfDependentTest;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "classpath:cucumber/tenantIsolation.feature", glue = "org.openecomp.sdc.cucumber.steps")

public class RunTenantIsolationCucumberCI extends TestAPIConfDependentTest {

	@BeforeClass
	public static void beforeClass() {
	}

	@AfterClass
	public static void afterClassJUnit() {
	}

	@org.testng.annotations.BeforeClass
	public static void beforeClassTestNg() {
	}

	@org.testng.annotations.AfterClass
	public static void afterClassTestNG() {
	}
}