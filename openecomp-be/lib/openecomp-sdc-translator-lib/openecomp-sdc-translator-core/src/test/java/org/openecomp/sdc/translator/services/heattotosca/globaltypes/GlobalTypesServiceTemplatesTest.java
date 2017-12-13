/*
 * Copyright © 2016-2017 European Support Limited
 *
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
 */

package org.openecomp.sdc.translator.services.heattotosca.globaltypes;

import static org.openecomp.sdc.translator.services.heattotosca.globaltypes.GlobalTypesServiceTemplates.getGlobalTypesServiceTemplates;

import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GlobalTypesServiceTemplatesTest {

  @Test(expectedExceptions = CoreException.class,
      expectedExceptionsMessageRegExp = "Failed to generate globalTypes")
  public void testGetGlobalTypesServiceTemplatesOnboardingMethodNull() {
    Map<String, ServiceTemplate> globalTypesServiceTemplates = getGlobalTypesServiceTemplates(null);
  }

  @Test
  public void testGetGlobalTypesServiceTemplatesOnboardingMethodToscaCsar() {
    Map<String, ServiceTemplate> globalTypesServiceTemplates =
        getGlobalTypesServiceTemplates(OnboardingTypesEnum.CSAR);
    Assert.assertNotNull(globalTypesServiceTemplates);
    Assert.assertEquals(globalTypesServiceTemplates.size(), 24);
    Set<String> globalTypeFolders = globalTypesServiceTemplates.keySet();
    List<String> onapGlobalTypes = globalTypeFolders.stream()
        .filter(resourceKey -> resourceKey.contains("onap"))
        .collect(Collectors.toList());
    Assert.assertNotNull(onapGlobalTypes);
    Assert.assertEquals(onapGlobalTypes.size(), 5);
  }

  @Test
  public void testGetGlobalTypesServiceTemplatesOnboardingMethodHeat() {
    Map<String, ServiceTemplate> globalTypesServiceTemplates =
        getGlobalTypesServiceTemplates(OnboardingTypesEnum.ZIP);
    Assert.assertNotNull(globalTypesServiceTemplates);
    Assert.assertEquals(globalTypesServiceTemplates.size(), 19);
    Set<String> globalTypeFolders = globalTypesServiceTemplates.keySet();
    List<String> onapGlobalTypes = globalTypeFolders.stream()
        .filter(resourceKey -> resourceKey.contains("onap"))
        .collect(Collectors.toList());
    Assert.assertEquals(onapGlobalTypes.size(), 0);
  }

  @Test
  public void testGetGlobalTypesServiceTemplatesOnboardingMethodManual() {
    Map<String, ServiceTemplate> globalTypesServiceTemplates =
        getGlobalTypesServiceTemplates(OnboardingTypesEnum.MANUAL);
    Assert.assertNotNull(globalTypesServiceTemplates);
    Assert.assertEquals(globalTypesServiceTemplates.size(), 19);
    Set<String> globalTypeFolders = globalTypesServiceTemplates.keySet();
    List<String> onapGlobalTypes = globalTypeFolders.stream()
        .filter(resourceKey -> resourceKey.contains("onap"))
        .collect(Collectors.toList());
    Assert.assertEquals(onapGlobalTypes.size(), 0);
  }

  @Test
  public void testGetGlobalTypesServiceTemplatesOnboardingMethodNone() {
    Map<String, ServiceTemplate> globalTypesServiceTemplates =
        getGlobalTypesServiceTemplates(OnboardingTypesEnum.NONE);
    Assert.assertNotNull(globalTypesServiceTemplates);
    Assert.assertEquals(globalTypesServiceTemplates.size(), 19);
    Set<String> globalTypeFolders = globalTypesServiceTemplates.keySet();
    List<String> onapGlobalTypes = globalTypeFolders.stream()
        .filter(resourceKey -> resourceKey.contains("onap"))
        .collect(Collectors.toList());
    Assert.assertEquals(onapGlobalTypes.size(), 0);
  }

}