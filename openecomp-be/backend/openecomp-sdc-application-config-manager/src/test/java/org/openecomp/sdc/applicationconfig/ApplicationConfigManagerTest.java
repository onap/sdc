package org.openecomp.sdc.applicationconfig;

import org.openecomp.sdc.applicationconfig.impl.ApplicationConfigManagerImpl;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.core.utilities.applicationconfig.dao.type.ApplicationConfigEntity;
import org.openecomp.core.utilities.applicationconfig.type.ConfigurationData;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collection;

public class ApplicationConfigManagerTest {

  public static final String TEST_NAMESPACE_1 = "test-app-namespace";
  public static final String TEST_NAMESPACE_2 = "test-namespace";
  public static final String TEST_KEY = "test-app-key";
  public static final String TEST_VALUE = "test-app-value";
  ApplicationConfigManager applicationConfigManager = new ApplicationConfigManagerImpl();

  @Test
  public void testInsertIntoTable() {
    try {
      applicationConfigManager.insertIntoTable(TEST_NAMESPACE_1, TEST_KEY, TEST_VALUE);
    } catch (CoreException e) {
      Assert.assertEquals(e.getMessage(),
          "Error occurred while loading questionnaire schema templates");
    }
  }


  @Test(dependsOnMethods = "testInsertIntoTable")
  public void testGetValueFromTable() {
    ConfigurationData value = applicationConfigManager.getFromTable(TEST_NAMESPACE_1, TEST_KEY);

    Assert.assertEquals(value.getValue(), TEST_VALUE);
  }


  @Test(dependsOnMethods = "testInsertIntoTable")
  public void testGetValueFromTableNegative() {
    try {
      ConfigurationData value =
          applicationConfigManager.getFromTable("not-existing-namespace", "not-existing-key");
    } catch (CoreException ce) {
      Assert.assertEquals(ce.getMessage(),
          "Configuration for namespace not-existing-namespace and key not-existing-key was not found");
    }

  }

  @Test
  public void testGetList() {
    applicationConfigManager.insertIntoTable(TEST_NAMESPACE_2, "key1", "val1");
    applicationConfigManager.insertIntoTable(TEST_NAMESPACE_2, "key2", "val2");
    applicationConfigManager.insertIntoTable(TEST_NAMESPACE_2, "key3", "val3");

    Collection<ApplicationConfigEntity> ACElist =
        applicationConfigManager.getListOfConfigurationByNamespace(TEST_NAMESPACE_2);

    Assert.assertNotNull(ACElist);
    Assert.assertEquals(ACElist.size(), 3);
  }
}
