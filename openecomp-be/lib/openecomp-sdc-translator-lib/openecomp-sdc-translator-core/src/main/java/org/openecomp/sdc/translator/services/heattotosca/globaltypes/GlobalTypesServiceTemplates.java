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

import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.translator.utils.ResourceWalker;

import java.util.HashMap;
import java.util.Map;

public class GlobalTypesServiceTemplates {

  private static Map<OnboardingTypesEnum, Map<String, ServiceTemplate>>
      onboardingGlobalTypesServiceTemplates;

  static {
    Map<String, String> globalTypes = null;
    try {
      globalTypes = ResourceWalker.readResourcesFromDirectory("globalTypes");
    } catch (CoreException coreException) {
      throw coreException;
    } catch (Exception exception) {
      throw new CoreException((new ErrorCode.ErrorCodeBuilder())
          .withMessage(LoggerErrorDescription.FAILED_TO_GENERATE_GLOBAL_TYPES)
          .withId("GlobalTypes Read Error").withCategory(ErrorCategory.APPLICATION).build(),
          exception);
    }
    init(globalTypes);
  }

  public static Map<String, ServiceTemplate> getGlobalTypesServiceTemplates(OnboardingTypesEnum
                                                                                onboardingType) {
    if (onboardingType == null) {
      throw new CoreException((new ErrorCode.ErrorCodeBuilder())
          .withMessage(LoggerErrorDescription.FAILED_TO_GENERATE_GLOBAL_TYPES)
          .withId("Invalid Onboarding Type").withCategory(ErrorCategory.APPLICATION).build());
    }
    return onboardingGlobalTypesServiceTemplates.get(onboardingType);
  }

  private static void init(Map<String, String> globalTypes) {
    onboardingGlobalTypesServiceTemplates = new HashMap<>();
    Map<String, ServiceTemplate> zipOnboardingGlobalTypes =
        getOnboardingGlobalTypes(globalTypes, OnboardingTypesEnum.ZIP);
    Map<String, ServiceTemplate> csarOnboardingGlobalTypes =
        getOnboardingGlobalTypes(globalTypes, OnboardingTypesEnum.CSAR);
    Map<String, ServiceTemplate> manualOnboardingGlobalTypes =
        getOnboardingGlobalTypes(globalTypes, OnboardingTypesEnum.MANUAL);
    Map<String, ServiceTemplate> defaultOnboardingGlobalTypes =
        getOnboardingGlobalTypes(globalTypes, OnboardingTypesEnum.NONE);
    onboardingGlobalTypesServiceTemplates.put(OnboardingTypesEnum.ZIP, zipOnboardingGlobalTypes);
    onboardingGlobalTypesServiceTemplates.put(OnboardingTypesEnum.CSAR, csarOnboardingGlobalTypes);
    onboardingGlobalTypesServiceTemplates.put(OnboardingTypesEnum.MANUAL,
        manualOnboardingGlobalTypes);
    onboardingGlobalTypesServiceTemplates.put(OnboardingTypesEnum.NONE,
        defaultOnboardingGlobalTypes);
  }

  private GlobalTypesServiceTemplates() {
  }

  private static Map<String, ServiceTemplate> getOnboardingGlobalTypes(Map<String, String>
                                                                         globalTypes,
                                                                       OnboardingTypesEnum
                                                                           onboardingType) {
    Map<String, ServiceTemplate> globalTypesServiceTemplates = new HashMap<>();
    ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
    for (Map.Entry<String, String> globalTypeContent : globalTypes.entrySet()) {
      if (!isTypeValidCandidateForCsarPacking(globalTypeContent.getKey(), onboardingType)) {
        // this global types folders should not be processed to the CSAR
        continue;
      }
      ToscaUtil.addServiceTemplateToMapWithKeyFileName(globalTypesServiceTemplates,
          toscaExtensionYamlUtil.yamlToObject(globalTypeContent.getValue(), ServiceTemplate.class));
    }
    return globalTypesServiceTemplates;
  }

  private static boolean isTypeValidCandidateForCsarPacking(String globalTypeResourceKey,
                                                            OnboardingTypesEnum
                                                                onboardingType) {
    boolean isTypeValidCandidateForCsarPacking = true;
    if (globalTypeResourceKey.contains("openecomp-inventory")) {
      // this global types folders should not be processed to the CSAR
      isTypeValidCandidateForCsarPacking = false;
    }
    if (globalTypeResourceKey.contains("onap")
        && onboardingType != OnboardingTypesEnum.CSAR) {
      //Global types specific to csar onboarding should not be packed for other onboarding types
      isTypeValidCandidateForCsarPacking = false;
    }
    return isTypeValidCandidateForCsarPacking;
  }

}
