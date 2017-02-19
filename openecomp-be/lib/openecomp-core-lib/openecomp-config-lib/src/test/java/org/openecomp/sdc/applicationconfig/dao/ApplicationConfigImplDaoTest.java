package org.openecomp.sdc.applicationconfig.dao;

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.core.utilities.applicationconfig.ApplicationConfig;
import org.openecomp.core.utilities.applicationconfig.ApplicationConfigFactory;
import org.openecomp.core.utilities.applicationconfig.dao.ApplicationConfigDao;
import org.openecomp.core.utilities.applicationconfig.dao.ApplicationConfigDaoFactory;
import org.openecomp.core.utilities.applicationconfig.dao.type.ApplicationConfigEntity;
import org.openecomp.core.utilities.applicationconfig.type.ConfigurationData;
import org.openecomp.core.utilities.file.FileUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class ApplicationConfigImplDaoTest {

  private static final String SCHEMA_GENERATOR_INITIALIZATION_ERROR =
      "SCHEMA_GENERATOR_INITIALIZATION_ERROR";
  private static final String SCHEMA_GENERATOR_INITIALIZATION_ERROR_MSG =
      "Error occurred while loading questionnaire schema templates";
  private static ApplicationConfigDao applicationConfigDao =
      ApplicationConfigDaoFactory.getInstance().createInterface();
  private static ApplicationConfig applicationConfig =
      ApplicationConfigFactory.getInstance().createInterface();

//  @BeforeClass
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

//  @Test
  public void testApplicationConfigTimestampValue() {
    ConfigurationData configurationData = applicationConfig
        .getConfigurationData("test - namespace", CompositionEntityType.vsp.name());

    Assert.assertNotNull(configurationData);
    Assert.assertNotEquals(configurationData.getTimeStamp(), 0);

  }

//  @Test(dependsOnMethods = "testApplicationConfigTimestampValue")
  public void testNotExistingApplicationConfigTimestampValue() {
    try {
      applicationConfig.getConfigurationData("test - namespace", "aaa");
    } catch (CoreException ce) {
      Assert.assertEquals(ce.getMessage(),
          "Configuration for namespace test - namespace and key aaa was not found");
    }

  }

//  @Test(dependsOnMethods = "testApplicationConfigTimestampValue")
  public void testInsertApplicationConfiguration() {
    String testTemplate = loadFileToString("questionnaire/testTemplate.txt");
    applicationConfig.insertValue("test_namespace", "test_key", testTemplate);

    Assert.assertEquals(testTemplate,
        applicationConfig.getConfigurationData("test_namespace", "test_key").getValue());
  }

}
