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

package org.openecomp.sdc.validation.impl.validators.heatresource;

import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.validation.ErrorMessageCode;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.PolicyTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.validation.ResourceValidator;
import org.openecomp.sdc.validation.ValidationContext;
import org.openecomp.sdc.validation.type.HeatResourceValidationContext;
import org.openecomp.sdc.validation.type.ValidatorConstants;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NovaServerGroupResourceValidator implements ResourceValidator {
  private static final ErrorMessageCode ERROR_CODE_HNG1 = new ErrorMessageCode("HNG1");
  private static final ErrorMessageCode ERROR_CODE_HNG2 = new ErrorMessageCode("HNG2");
  private static final ErrorMessageCode ERROR_CODE_HNG3 = new ErrorMessageCode("HNG3");

  @Override
  public void validate(String fileName, Map.Entry<String, Resource> resourceEntry,
                       GlobalValidationContext globalContext, ValidationContext validationContext) {
    validateNovaServerGroupPolicy(fileName, resourceEntry, globalContext);
    validateServerGroupIsUsed(fileName, resourceEntry, globalContext,
            (HeatResourceValidationContext) validationContext);
  }

  @SuppressWarnings("unchecked")
  private static void validateNovaServerGroupPolicy(String fileName,
                                                    Map.Entry<String, Resource> resourceEntry,
                                                    GlobalValidationContext globalContext) {
    Resource resource = resourceEntry.getValue();
    Object policies =
            resource.getProperties() == null ? null : resource.getProperties().get("policies");

    if (Objects.nonNull(policies) && policies instanceof List) {
      List<Object> policiesList = (List<Object>) policies;
      if (policiesList.size() == 1) {
        Object policy = policiesList.get(0);
        if (!isGivenPolicyValid(policy)) {
          globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                          .getErrorWithParameters(
                                  ERROR_CODE_HNG1, Messages.WRONG_POLICY_IN_SERVER_GROUP.getErrorMessage(),
                                  resourceEntry.getKey()));
        }
      } else {
        globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                        .getErrorWithParameters(ERROR_CODE_HNG1,
                                Messages.WRONG_POLICY_IN_SERVER_GROUP.getErrorMessage(),
                                resourceEntry.getKey()));
      }
    }
  }

  private static boolean isGivenPolicyValid(Object policy) {
    if (policy instanceof Map) {
      return true;
    }
    if (policy instanceof String) {
      return PolicyTypes.isGivenPolicyValid((String) policy);
    }
    return false;
  }

  public void validateServerGroupIsUsed(String fileName,
                                        Map.Entry<String, Resource> resourceEntry,
                                        GlobalValidationContext globalContext,
                                        HeatResourceValidationContext validationContext) {

    Map<String, Map<String, List<String>>> pointedServerGroups =
            validationContext.getFileLevelResourceDependencies().get(HeatResourcesTypes
                    .NOVA_SERVER_GROUP_RESOURCE_TYPE.getHeatResource());

    if (MapUtils.isEmpty(pointedServerGroups)) {
      globalContext
              .addMessage(
                      fileName,
                      ErrorLevel.WARNING,
                      ErrorMessagesFormatBuilder
                              .getErrorWithParameters(
                                      ERROR_CODE_HNG2, Messages.RESOURCE_NOT_IN_USE.getErrorMessage(),
                                      ValidatorConstants.Server_Group, resourceEntry.getKey()));
      return;
    }

    handleServerGroupReferences(fileName, resourceEntry, pointedServerGroups, globalContext);
  }

  private void handleServerGroupReferences(String fileName, Map.Entry<String, Resource>
          resourceEntry, Map<String, Map<String, List<String>>> pointedServerGroups,
                                           GlobalValidationContext globalContext) {
    Map<String, List<String>> resourcesPointingToCurrServerGroup =
            pointedServerGroups.get(resourceEntry.getKey());

    if (MapUtils.isEmpty(resourcesPointingToCurrServerGroup)) {
      globalContext
              .addMessage(
                      fileName,
                      ErrorLevel.WARNING,
                      ErrorMessagesFormatBuilder
                              .getErrorWithParameters(
                                      ERROR_CODE_HNG3, Messages.RESOURCE_NOT_IN_USE.getErrorMessage(),
                                      ValidatorConstants.Server_Group, resourceEntry.getKey()));
    }

  }
}
