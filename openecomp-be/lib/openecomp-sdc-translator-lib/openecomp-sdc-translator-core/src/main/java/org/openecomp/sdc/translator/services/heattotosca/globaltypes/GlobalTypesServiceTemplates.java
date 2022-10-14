/*
 * Copyright © 2016-2018 European Support Limited
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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.Constants;
import org.openecomp.sdc.translator.utils.ResourceWalker;

public class GlobalTypesServiceTemplates {

    private static final String ONAP_FILEPATH_REGEX = ".*" + Constants.GLOBAL_TYPES + "(/onap/|\\\\onap\\\\).*";
    private static final Map<OnboardingTypesEnum, Map<String, ServiceTemplate>> onboardingGlobalTypesServiceTemplates;

    static {
        Map<String, String> globalTypes;
        try {
            globalTypes = ResourceWalker.readResourcesFromDirectory(Constants.GLOBAL_TYPES);
        } catch (CoreException coreException) {
            throw coreException;
        } catch (Exception exception) {
            throw new CoreException(
                (new ErrorCode.ErrorCodeBuilder()).withMessage(Constants.FAILED_TO_GENERATE_GLOBAL_TYPES).withId(Constants.GLOBAL_TYPES_READ_ERROR)
                    .withCategory(ErrorCategory.APPLICATION).build(), exception);
        }
        onboardingGlobalTypesServiceTemplates = init(globalTypes);
    }

    private GlobalTypesServiceTemplates() {
    }

    public static Map<String, ServiceTemplate> getGlobalTypesServiceTemplates(OnboardingTypesEnum onboardingType) {
        if (onboardingType == null) {
            throw new CoreException(
                (new ErrorCode.ErrorCodeBuilder()).withMessage(Constants.FAILED_TO_GENERATE_GLOBAL_TYPES).withId(Constants.INVALID_ONBOARDING_TYPE)
                    .withCategory(ErrorCategory.APPLICATION).build());
        }
        return onboardingGlobalTypesServiceTemplates.get(onboardingType);
    }

    private static Map<OnboardingTypesEnum, Map<String, ServiceTemplate>> init(Map<String, String> globalTypes) {
        Map<OnboardingTypesEnum, Map<String, ServiceTemplate>> onboardingGlobalTypesServiceTemplates = new EnumMap<>(OnboardingTypesEnum.class);
        Map<String, ServiceTemplate> zipOnboardingGlobalTypes = getOnboardingGlobalTypes(globalTypes, OnboardingTypesEnum.ZIP);
        Map<String, ServiceTemplate> csarOnboardingGlobalTypes = getOnboardingGlobalTypes(globalTypes, OnboardingTypesEnum.CSAR);
        Map<String, ServiceTemplate> manualOnboardingGlobalTypes = getOnboardingGlobalTypes(globalTypes, OnboardingTypesEnum.MANUAL);
        Map<String, ServiceTemplate> defaultOnboardingGlobalTypes = getOnboardingGlobalTypes(globalTypes, OnboardingTypesEnum.NONE);
        onboardingGlobalTypesServiceTemplates.put(OnboardingTypesEnum.ZIP, zipOnboardingGlobalTypes);
        onboardingGlobalTypesServiceTemplates.put(OnboardingTypesEnum.CSAR, csarOnboardingGlobalTypes);
        onboardingGlobalTypesServiceTemplates.put(OnboardingTypesEnum.MANUAL, manualOnboardingGlobalTypes);
        onboardingGlobalTypesServiceTemplates.put(OnboardingTypesEnum.NONE, defaultOnboardingGlobalTypes);
        return onboardingGlobalTypesServiceTemplates;
    }

    private static Map<String, ServiceTemplate> getOnboardingGlobalTypes(Map<String, String> globalTypes, OnboardingTypesEnum onboardingType) {
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

    private static boolean isTypeValidCandidateForCsarPacking(String globalTypeResourceKey, OnboardingTypesEnum onboardingType) {
        if (globalTypeResourceKey.contains(Constants.OPENECOMP_INVENTORY)) {
            // this global types folders should not be processed to the CSAR
            return false;
        }
        //Global types specific to csar onboarding should not be packed for other onboarding types
        return !globalTypeResourceKey.matches(ONAP_FILEPATH_REGEX) || onboardingType == OnboardingTypesEnum.CSAR;
    }
}
