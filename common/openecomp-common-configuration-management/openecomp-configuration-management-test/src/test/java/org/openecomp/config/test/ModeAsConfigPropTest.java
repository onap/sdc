package org.openecomp.config.test;

import org.openecomp.config.ConfigurationUtils;
import org.openecomp.config.api.Configuration;
import org.openecomp.config.api.ConfigurationManager;
import org.openecomp.config.util.ConfigTestConstant;
import org.openecomp.config.util.TestUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Scenario 8
 * Validate configuration with mode specified as a configuration property
 */

public class ModeAsConfigPropTest {

	String newValue = null;
	
	public final static String NAMESPACE = "ModeAsConfigProp";
	
	@Before
	public void setUp() throws IOException {
		String data = "{name:\"SCM\"}";
		TestUtil.writeFile(data);
	}

	@Test
	public void testMergeStrategyInConfig() throws IOException, InterruptedException {
		Configuration config = ConfigurationManager.lookup();

		Assert.assertEquals("14",config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH));

		Assert.assertEquals("1048",config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_MAXSIZE));

		List<String> expectedExtList = new ArrayList<String>();
		expectedExtList.add("pdf");
		expectedExtList.add("zip");
		expectedExtList.add("xml");
		expectedExtList.add("pdf");
		expectedExtList.add("tgz");
		expectedExtList.add("xls");
		List<String> extList = config.getAsStringValues(NAMESPACE, ConfigTestConstant.ARTIFACT_EXT);
		Assert.assertEquals(expectedExtList, extList);

		List<String> expectedEncList = new ArrayList<String>();
		expectedEncList.add("Base64");
		expectedEncList.add("MD5");
		List<String> encList = config.getAsStringValues(NAMESPACE, ConfigTestConstant.ARTIFACT_ENC);
		Assert.assertEquals(expectedEncList, encList);

		String newValue = config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_JSON_SCHEMA);
		Assert.assertEquals("{name:\"SCM\"}",config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_JSON_SCHEMA));

		Assert.assertEquals("a-zA-Z_0-9",config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_UPPER ));

		Assert.assertEquals("Deleted",config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_STATUS) );

		List<String> expectedLocList = new ArrayList<String>();
		expectedLocList.add("/opt/spool");
		expectedLocList.add(System.getProperty("user.home")+"/asdc");
		List<String> locList = config.getAsStringValues(NAMESPACE, ConfigTestConstant.ARTIFACT_LOC);
		Assert.assertEquals(expectedLocList, locList);

		Assert.assertEquals("@"+System.getenv("Path")+"/myschema.json",config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_XML_SCHEMA));

		List<String> artifactConsumer = config.getAsStringValues(NAMESPACE, ConfigTestConstant.ARTIFACT_CONSUMER );
		Assert.assertEquals(config.getAsStringValues(NAMESPACE, ConfigTestConstant.ARTIFACT_CONSUMER_APPC ), artifactConsumer);

		Assert.assertEquals(config.getAsBooleanValue(NAMESPACE, ConfigTestConstant.ARTIFACT_MANDATORY_NAME ), true);

		Assert.assertEquals(config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_MINLENGTH ), "6");

		Assert.assertEquals(config.getAsBooleanValue(NAMESPACE, ConfigTestConstant.ARTIFACT_ENCODED ), true);
	}

	@After
	public void tearDown() throws Exception {
		TestUtil.cleanUp();
	}

}
