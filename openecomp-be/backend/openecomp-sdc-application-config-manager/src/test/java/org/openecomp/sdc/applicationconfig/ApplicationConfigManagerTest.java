/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.applicationconfig;

import org.openecomp.sdc.applicationconfig.impl.ApplicationConfigManagerImpl;

/**
 * Created by Talio on 8/9/2016.
 */
public class ApplicationConfigManagerTest {

  public static final String TEST_NAMESPACE_1 = "test-app-namespace";
  public static final String TEST_NAMESPACE_2 = "test-namespace";
  public static final String TEST_KEY = "test-app-key";
  public static final String TEST_VALUE = "test-app-value";
  ApplicationConfigManager applicationConfigManager = new ApplicationConfigManagerImpl();

  /*
  @Test
  public void testInsertIntoTable() {
    try {
      applicationConfigManager.insertIntoTable(TEST_NAMESPACE_1, TEST_KEY, TEST_VALUE);
    } catch (CoreException exception) {
      Assert.assertEquals(exception.getMessage(),
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

  */
}
