package org.openecomp.sdc.validation.impl.validators.namingconvention;

import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.validation.ResourceValidator;
import org.openecomp.sdc.validation.ValidationContext;
import org.openecomp.sdc.validation.type.NamingConventionValidationContext;
import org.openecomp.sdc.validation.util.ValidationUtil;

import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;

/**
 * Created by TALIO on 2/23/2017.
 */
public class NeutronPortNamingConventionValidator implements ResourceValidator {
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private static final String  ERROR_CODE_J1="[J-1] :";
  private static final String  ERROR_CODE_J2="[J-2] :";
  private static final String  ERROR_CODE_J3="[J-3] :";
  private static final String  ERROR_CODE_J4="[J-4] :";
  @Override
  public void validate(String fileName, Map.Entry<String, Resource> resourceEntry,
                       GlobalValidationContext globalContext, ValidationContext validationContext) {

    NamingConventionValidationContext namingConventionValidationContext =
        (NamingConventionValidationContext)validationContext;
    validatePortNetworkNamingConvention(fileName, namingConventionValidationContext.getHeatOrchestrationTemplate(), globalContext);
    validateFixedIpsNamingConvention(fileName, namingConventionValidationContext.getHeatOrchestrationTemplate(), globalContext);
  }

  private void validatePortNetworkNamingConvention(String fileName,
                                                   HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                   GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    if (MapUtils.isEmpty(heatOrchestrationTemplate.getResources())) {
      mdcDataDebugMessage.debugExitMessage("file", fileName);
      return;
    }
    String[] regexList = new String[]{".*_net_id", ".*_net_name", ".*_net_fqdn"};

    heatOrchestrationTemplate
        .getResources()
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue().getType()
            .equals(HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource()))
        .forEach(entry -> entry.getValue()
            .getProperties()
            .entrySet()
            .stream()
            .filter(propertyEntry ->
                propertyEntry.getKey().toLowerCase().equals("network".toLowerCase())
                    || propertyEntry.getKey().equals("network_id"))
            .forEach(propertyEntry -> validateParamNamingConvention(fileName, entry.getKey(),
                propertyEntry.getValue(), "Port", "Network", regexList,
                Messages.PARAMETER_NAME_NOT_ALIGNED_WITH_GUIDELINES, globalContext)));

    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }

  private void validateFixedIpsNamingConvention(String fileName,
                                                HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    if (MapUtils.isEmpty(heatOrchestrationTemplate.getResources())) {
      mdcDataDebugMessage.debugExitMessage("file", fileName);
      return;
    }

    heatOrchestrationTemplate.getResources()
        .entrySet()
        .stream()
        .filter(entry -> HeatResourcesTypes.findByHeatResource(entry.getValue().getType()) != null)
        .filter(entry -> HeatResourcesTypes.findByHeatResource(entry.getValue().getType())
            .equals(HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE))
        .forEach(entry -> checkNeutronPortFixedIpsName(fileName, entry, globalContext));

    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }

  private void checkNeutronPortFixedIpsName(String fileName,
                                            Map.Entry<String, Resource> resourceEntry,
                                            GlobalValidationContext globalContext) {
    String[] regexList =
        new String[]{"[^_]+_[^_]+_ips", "[^_]+_[^_]+_v6_ips", "[^_]+_[^_]+_ip_(\\d+)",
            "[^_]+_[^_]+_v6_ip_(\\d+)"};

    if (MapUtils.isEmpty(resourceEntry.getValue().getProperties())) {
      return;
    }

    Map<String, Object> propertiesMap = resourceEntry.getValue().getProperties();
    Object fixedIps = propertiesMap.get("fixed_ips");
    if (nonNull(fixedIps) && fixedIps instanceof List) {
      List<Object> fixedIpsList = (List<Object>) fixedIps;
      for (Object fixedIpsObject : fixedIpsList) {
        Map.Entry<String, Object> fixedIpsEntry =
            ((Map<String, Object>) fixedIpsObject).entrySet().iterator().next();
        if (nonNull(fixedIpsEntry)) {
          if (fixedIpsEntry.getValue() instanceof Map) {
            String fixedIpsName = ValidationUtil.getWantedNameFromPropertyValueGetParam
                (fixedIpsEntry
                .getValue());
            if (nonNull(fixedIpsName)) {
              if (!ValidationUtil.evalPattern(fixedIpsName, regexList)) {
                globalContext.addMessage(
                    fileName,
                    ErrorLevel.WARNING, ErrorMessagesFormatBuilder.getErrorWithParameters(
                        Messages.PARAMETER_NAME_NOT_ALIGNED_WITH_GUIDELINES.getErrorMessage(),ERROR_CODE_J1,
                        "Port", "Fixed_IPS", fixedIpsName, resourceEntry.getKey()),
                    LoggerTragetServiceName.VALIDATE_FIXED_IPS_NAME,
                    LoggerErrorDescription.NAME_NOT_ALIGNED_WITH_GUIDELINES);
              }
            }
          } else {
            globalContext.addMessage(
                fileName,
                ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                    .getErrorWithParameters(Messages.MISSING_GET_PARAM.getErrorMessage(),
                        ERROR_CODE_J2,"fixed_ips", resourceEntry.getKey()),
                LoggerTragetServiceName.VALIDATE_FIXED_IPS_NAME,
                LoggerErrorDescription.MISSING_GET_PARAM);
          }
        }
      }
    }
  }

  private void validateParamNamingConvention(String fileName, String resourceId,
                                             Object propertyValue, String resourceType,
                                             String wrongPropertyFormat, String[] regexList,
                                             Messages message,
                                             GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    Object paramName;
    if (propertyValue instanceof Map) {
      paramName = ((Map) propertyValue).get("get_param");
      if (paramName instanceof String) {
        if (!ValidationUtil.evalPattern((String) paramName, regexList)) {
          globalContext.addMessage(
              fileName,
              ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                  .getErrorWithParameters(message.getErrorMessage(),ERROR_CODE_J3, resourceType,
                      wrongPropertyFormat, (String) paramName, resourceId),
              LoggerTragetServiceName.VALIDATE_PORT_NETWORK_NAME,
              LoggerErrorDescription.NAME_NOT_ALIGNED_WITH_GUIDELINES);
        }
      }
    } else {
      globalContext.addMessage(
          fileName,
          ErrorLevel.WARNING,
          ErrorMessagesFormatBuilder
              .getErrorWithParameters(Messages.MISSING_GET_PARAM.getErrorMessage(),ERROR_CODE_J4,
                  "network or network_id", resourceId),
          LoggerTragetServiceName.VALIDATE_PORT_NETWORK_NAME,
          LoggerErrorDescription.MISSING_GET_PARAM);
    }

    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }
}
