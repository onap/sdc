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

package org.openecomp.sdc.applicationconfig.dao;

public class ApplicationConfigImplDaoTest {

  /*

  private static final String SCHEMA_GENERATOR_INITIALIZATION_ERROR =
      "SCHEMA_GENERATOR_INITIALIZATION_ERROR";
  private static final String SCHEMA_GENERATOR_INITIALIZATION_ERROR_MSG =
      "Error occurred while loading questionnaire schema templates";
  private static ApplicationConfigDao applicationConfigDao =
      ApplicationConfigDaoFactory.getInstance().createInterface();
  private static ApplicationConfig applicationConfig =
      ApplicationConfigFactory.getInstance().createInterface();

  private final static Logger log = (Logger) LoggerFactory.getLogger
      (ApplicationConfigImplDaoTest.class.getName());

  private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass().getName());

  @BeforeClass
  public static void init() {
    try {

      ApplicationConfigEntity applicationConfigEntity1 =
          new ApplicationConfigEntity("test - namespace", "vsp", "vspTemplate");
      ApplicationConfigEntity applicationConfigEntity2 =
          new ApplicationConfigEntity("test - namespace", "nic", "nicTemplate");
      ApplicationConfigEntity applicationConfigEntity3 =
          new ApplicationConfigEntity("test - namespace", "component", "componentTemplate");

      applicationConfigDao.create(applicationConfigEntity1);
      applicationConfigDao.create(applicationConfigEntity2);
      applicationConfigDao.create(applicationConfigEntity3);

    } catch (Exception e) {
      log.debug("",e);
      throw new CoreException(new ErrorCode.ErrorCodeBuilder().
          withCategory(ErrorCategory.APPLICATION).
          withId(SCHEMA_GENERATOR_INITIALIZATION_ERROR).
          withMessage(SCHEMA_GENERATOR_INITIALIZATION_ERROR_MSG).
          build());
    }
  }

  private static String loadFileToString(String path) {
    return new String(FileUtils.toByteArray(FileUtils.loadFileToInputStream(path)));
  }

  @Test
  public void testApplicationConfigTimestampValue() {
    ConfigurationData configurationData = applicationConfig
        .getConfigurationData("test - namespace", "vsp");

    Assert.assertNotNull(configurationData);
    Assert.assertNotEquals(configurationData.getTimeStamp(), 0);

  }

  @Test(dependsOnMethods = "testApplicationConfigTimestampValue")
  public void testNotExistingApplicationConfigTimestampValue() {
    try {
      applicationConfig.getConfigurationData("test - namespace", "aaa");
    } catch (CoreException ce) {
      logger.debug("", ce);
      Assert.assertEquals(ce.getMessage(),
          "Configuration for namespace test - namespace and key aaa was not found");
    }

  }

  @Test(dependsOnMethods = "testApplicationConfigTimestampValue")
  public void testInsertApplicationConfiguration() {
    String testTemplate = loadFileToString("questionnaire/testTemplate.txt");
    applicationConfig.insertValue("test_namespace", "test_key", testTemplate);

    Assert.assertEquals(testTemplate,
        applicationConfig.getConfigurationData("test_namespace", "test_key").getValue());
  }
*/
}
