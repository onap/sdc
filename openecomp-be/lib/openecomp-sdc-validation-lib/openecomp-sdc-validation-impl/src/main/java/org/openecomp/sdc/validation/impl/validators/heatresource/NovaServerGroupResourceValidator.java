package org.openecomp.sdc.validation.impl.validators.heatresource;

import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.PolicyTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.validation.ResourceValidator;
import org.openecomp.sdc.validation.ValidationContext;
import org.openecomp.sdc.validation.type.HeatResourceValidationContext;
import org.openecomp.sdc.validation.type.ValidatorConstants;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by TALIO on 2/22/2017.
 */
public class NovaServerGroupResourceValidator implements ResourceValidator {
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  private static String ERROR_CODE_E1 = "E-1";
  private static String ERROR_CODE_E2 = "E-2";
  private static String ERROR_CODE_E3 = "E-3";
  private static String ERROR_CODE_E4 = "E-4";

  public void validate(String fileName, Map.Entry<String, Resource> resourceEntry,
                       GlobalValidationContext globalContext, ValidationContext validationContext) {
    validateNovaServerGroupPolicy(fileName, resourceEntry, globalContext);
    validateServerGroupIsUsed
        (fileName, resourceEntry, globalContext, (HeatResourceValidationContext) validationContext);
  }

  @SuppressWarnings("unchecked")
  private static void validateNovaServerGroupPolicy(String fileName,
                                                    Map.Entry<String, Resource> resourceEntry,
                                                    GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    Resource resource = resourceEntry.getValue();
    Object policies =
        resource.getProperties() == null ? null : resource.getProperties().get("policies");

    if (Objects.nonNull(policies) && policies instanceof List) {
      List<Object> policiesList = (List<Object>) policies;
      if (policiesList.size() == 1) {
        Object policy = policiesList.get(0);
        if (!isGivenPolicyValid(policy)) {
          globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                  .getErrorWithParameters(Messages.WRONG_POLICY_IN_SERVER_GROUP.getErrorMessage(),
                      ERROR_CODE_E1,
                      resourceEntry.getKey()),
              LoggerTragetServiceName.VALIDATE_NOVA_SEVER_GROUP_POLICY,
              LoggerErrorDescription.WRONG_POLICY_SERVER_GROUP);
        }
      } else {
        globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                .getErrorWithParameters(Messages.WRONG_POLICY_IN_SERVER_GROUP.getErrorMessage(),
                    ERROR_CODE_E2,
                    resourceEntry.getKey()),
            LoggerTragetServiceName.VALIDATE_NOVA_SEVER_GROUP_POLICY,
            LoggerErrorDescription.WRONG_POLICY_SERVER_GROUP);
      }
    }

    mdcDataDebugMessage.debugExitMessage("file", fileName);
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
                      Messages.RESOURCE_NOT_IN_USE.getErrorMessage(),
                      ERROR_CODE_E3,
                      ValidatorConstants.Server_Group, resourceEntry.getKey()),
              LoggerTragetServiceName.VALIDATE_ALL_SERVER_GROUP_OR_SECURITY_GROUP_IN_USE,
              LoggerErrorDescription.SERVER_GROUP_SECURITY_GROUP_NOT_IN_USE);
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
                      Messages.RESOURCE_NOT_IN_USE.getErrorMessage(),
                      ERROR_CODE_E4,
                      ValidatorConstants.Server_Group, resourceEntry.getKey()),
              LoggerTragetServiceName.VALIDATE_ALL_SERVER_GROUP_OR_SECURITY_GROUP_IN_USE,
              LoggerErrorDescription.SERVER_GROUP_SECURITY_GROUP_NOT_IN_USE);
    }

  }
}
