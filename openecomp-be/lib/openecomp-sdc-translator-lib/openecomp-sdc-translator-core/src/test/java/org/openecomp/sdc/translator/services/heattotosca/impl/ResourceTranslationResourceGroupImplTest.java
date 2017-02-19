package org.openecomp.sdc.translator.services.heattotosca.impl;

import org.openecomp.sdc.common.errors.CoreException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

/**
 * @author shiria
 * @since July 21, 2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceTranslationResourceGroupImplTest extends BaseResourceTranslationTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testTranslateResourceGroup() throws Exception {
    inputFilesPath = "/mock/heat/nested/resource_group/inputs";
    outputFilesPath = "/mock/heat/nested/resource_group/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateMultiResourceGroup() throws Exception {
    inputFilesPath = "/mock/heat/nested/multiple_resource_groups/inputs";
    outputFilesPath = "/mock/heat/nested/multiple_resource_groups/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateResourceGroupDynamicCount() throws Exception {
    inputFilesPath = "/mock/heat/nested/resource_group_with_dynamic_count/inputs";
    outputFilesPath = "/mock/heat/nested/resource_group_with_dynamic_count/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }


  @Test
  public void testTranslatePortToNetNestedByResourceGroupConnection() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/porttonetresourcegroupconnection/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/porttonetresourcegroupconnection/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslatePortToNetSharedConnection() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/porttosharednetresourcegrouplinking/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/porttosharednetresourcegrouplinking/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateSecurityGroupToSharedPortConnection() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/securityruletosharedportresourcegrouplinking/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/securityruletosharedportresourcegrouplinking/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateResourceGroupIndexVar() throws Exception {
    inputFilesPath = "/mock/heat/nested/resourceGroupIndexVar/inputs";
    outputFilesPath = "/mock/heat/nested/resourceGroupIndexVar/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateResourceGroupInvalidIndexVar() throws Exception {
    thrown.expect(CoreException.class);
    thrown.expectMessage(
        "'index_var' property has invalid value. Actual value is '{get_param=index_parameter}' while 'String' value expected.");

    inputFilesPath = "/mock/heat/nested/resourceGroupInvalid/inputs";
    outputFilesPath = "/mock/heat/nested/resourceGroupInvalid/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateMDNS() throws Exception {
    inputFilesPath = "/mock/heat/nested/resourceGroupMDNS/inputs";
    outputFilesPath = "/mock/heat/nested/resourceGroupMDNS/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }


}