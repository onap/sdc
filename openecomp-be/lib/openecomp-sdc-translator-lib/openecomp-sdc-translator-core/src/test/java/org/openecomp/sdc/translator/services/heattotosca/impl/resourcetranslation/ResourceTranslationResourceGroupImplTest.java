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

package org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

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
  public void testTranslateMDNS() throws Exception {
    inputFilesPath = "/mock/heat/nested/resourceGroupMDNS/inputs";
    outputFilesPath = "/mock/heat/nested/resourceGroupMDNS/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }


}
