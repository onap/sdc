package org.openecomp.config.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openecomp.config.ConfigurationUtils;
import org.openecomp.config.util.ConfigTestConstant;

import org.openecomp.config.util.TestUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.openecomp.config.api.Configuration;
import org.openecomp.config.api.ConfigurationChangeListener;
import org.openecomp.config.api.ConfigurationManager;

/**
 * Scenario 7
 * Test to Validate notification on changes to the underlying source
 * Resource here is GeneratorsList.json ehich is created in test itself
 */

public class ResourceChangeNotificationTest  {

	String newValue = null;

	public final static String NAMESPACE = "Notification";
	
	@Before
	public void setUp() throws IOException {
		String data = "{name:\"SCM\"}";
		TestUtil.writeFile(data);
	}

	@Test
	public void testNotification() throws IOException, InterruptedException {		
		Configuration config = ConfigurationManager.lookup();		
		config.addConfigurationChangeListener(NAMESPACE,ConfigTestConstant.ARTIFACT_JSON_SCHEMA, new MyListener());
		updateJsonInFile();
		Thread.sleep(35000);
		String newValue = config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_JSON_SCHEMA);

		Assert.assertEquals("{name:\"updated SCM\"}",newValue);
		
		Assert.assertEquals( "14",config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH ));
		
		Assert.assertEquals( "a-zA-Z", config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_UPPER ));
		
		String artifactConsumer = config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_CONSUMER );
		Assert.assertEquals(artifactConsumer,config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_CONSUMER_APPC ));

		List<String> expectedExtList = new ArrayList<String>();
		expectedExtList.add("pdf"); expectedExtList.add("zip"); expectedExtList.add("xml");
		List<String> extList = config.getAsStringValues(NAMESPACE, ConfigTestConstant.ARTIFACT_EXT);
		Assert.assertEquals(expectedExtList, extList);
		
		List<String> expectedEncList = new ArrayList<String>();
		expectedEncList.add("Base64"); expectedEncList.add("MD5"); 
		List<String> encList = config.getAsStringValues(NAMESPACE, ConfigTestConstant.ARTIFACT_ENC);
		Assert.assertEquals(expectedEncList, encList);		
		
		List<String> expectedLocList = new ArrayList<String>();
		expectedLocList.add("/opt/spool"); expectedLocList.add(System.getProperty("user.home")+"/asdc");
		List<String> locList = config.getAsStringValues(NAMESPACE, ConfigTestConstant.ARTIFACT_LOC);
		Assert.assertEquals(expectedLocList, locList);

		Assert.assertEquals("@"+System.getenv("Path")+"/myschema.json",config.getAsString(NAMESPACE, ConfigTestConstant.ARTIFACT_XML_SCHEMA));
	}
	
	class MyListener implements ConfigurationChangeListener{
		@Override
		public void notify(String key, Object oldValue, Object newValue) {			
			System.out.println("received notification::oldValue=="+oldValue+" newValue=="+newValue);			
		}		
	}
	
	private void updateJsonInFile() throws IOException{	
		String data = "{name:\"updated SCM\"}";
		TestUtil.writeFile(data);
	}
	
	@After
	public void tearDown() throws Exception {
		TestUtil.cleanUp();
	}
}
