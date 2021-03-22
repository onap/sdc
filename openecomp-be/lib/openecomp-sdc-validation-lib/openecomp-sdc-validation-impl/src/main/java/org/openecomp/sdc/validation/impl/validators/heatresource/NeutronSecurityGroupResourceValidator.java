/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.validation.impl.validators.heatresource;

import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.validation.ErrorMessageCode;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.validation.ResourceValidator;
import org.openecomp.sdc.validation.ValidationContext;
import org.openecomp.sdc.validation.type.HeatResourceValidationContext;
import org.openecomp.sdc.validation.type.ValidatorConstants;

/**
 * Created by TALIO on 2/27/2017.
 */
public class NeutronSecurityGroupResourceValidator implements ResourceValidator {

    private static final ErrorMessageCode ERROR_CODE_HSG1 = new ErrorMessageCode("HSG1");

    @Override
    public void validate(String fileName, Map.Entry<String, Resource> resourceEntry, GlobalValidationContext globalContext,
                         ValidationContext validationContext) {
        HeatResourceValidationContext heatResourceValidationContext = (HeatResourceValidationContext) validationContext;
        validateSecurityGroupIsUsed(fileName, resourceEntry, heatResourceValidationContext, globalContext);
    }

    private void validateSecurityGroupIsUsed(String fileName, Map.Entry<String, Resource> resourceEntry,
                                             HeatResourceValidationContext heatResourceValidationContext, GlobalValidationContext globalContext) {
        Map<String, Map<String, List<String>>> securityGroupsPointedByOtherResources = heatResourceValidationContext
            .getFileLevelResourceDependencies().get(HeatResourcesTypes.NEUTRON_SECURITY_GROUP_RESOURCE_TYPE.getHeatResource());
        if (MapUtils.isEmpty(securityGroupsPointedByOtherResources)) {
            return;
        }
        Map<String, List<String>> resourcesPointingCurrSecurityGroup = securityGroupsPointedByOtherResources.get(resourceEntry.getKey());
        if (isSecurityGroupNotInUse(resourcesPointingCurrSecurityGroup)) {
            globalContext.addMessage(fileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                .getErrorWithParameters(ERROR_CODE_HSG1, Messages.RESOURCE_NOT_IN_USE.getErrorMessage(), ValidatorConstants.Security_Group,
                    resourceEntry.getKey()));
        }
    }

    private boolean isSecurityGroupNotInUse(Map<String, List<String>> referencingResourcesToCurrSecurityGroup) {
        return MapUtils.isEmpty(referencingResourcesToCurrSecurityGroup);
    }
}
