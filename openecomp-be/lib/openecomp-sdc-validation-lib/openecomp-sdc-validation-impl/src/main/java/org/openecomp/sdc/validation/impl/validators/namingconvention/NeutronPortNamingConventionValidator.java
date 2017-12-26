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

package org.openecomp.sdc.validation.impl.validators.namingconvention;

import static java.util.Objects.nonNull;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.validation.ErrorMessageCode;
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

public class NeutronPortNamingConventionValidator implements ResourceValidator {
  private static final MdcDataDebugMessage MDC_DATA_DEBUG_MESSAGE = new MdcDataDebugMessage();
  private static final ErrorMessageCode ERROR_CODE_NNP1 = new ErrorMessageCode("NNP1");
  private static final ErrorMessageCode ERROR_CODE_NNP2 = new ErrorMessageCode("NNP2");
  private static final ErrorMessageCode ERROR_CODE_NNP3 = new ErrorMessageCode("NNP3");

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

    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("file", fileName);

    if (MapUtils.isEmpty(heatOrchestrationTemplate.getResources())) {
      MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", fileName);
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
                            ("network").equalsIgnoreCase(propertyEntry.getKey())
                                    || ("network_id").equals(propertyEntry.getKey()))
                    .forEach(propertyEntry -> validateParamNamingConvention(fileName, entry.getKey(),
                            propertyEntry.getValue(),  regexList,
                            Messages.PARAMETER_NAME_NOT_ALIGNED_WITH_GUIDELINES, globalContext)));

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", fileName);
  }

  private void validateFixedIpsNamingConvention(String fileName,
                                                HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                GlobalValidationContext globalContext) {

    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("file", fileName);

    if (MapUtils.isEmpty(heatOrchestrationTemplate.getResources())) {
      MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", fileName);
      return;
    }

    heatOrchestrationTemplate.getResources()
            .entrySet()
            .stream()
            .filter(entry -> HeatResourcesTypes.findByHeatResource(entry.getValue().getType()) != null)
            .filter(entry -> HeatResourcesTypes.findByHeatResource(entry.getValue().getType())
                    .equals(HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE))
            .forEach(entry -> checkNeutronPortFixedIpsName(fileName, entry, globalContext));

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", fileName);
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

        validateFixedIpsName(fileName, resourceEntry, globalContext, regexList, fixedIpsEntry);


      }
    }
  }

  private void validateFixedIpsName(String fileName, Map.Entry<String, Resource> resourceEntry,
                                    GlobalValidationContext globalContext,
                                    String[] regexList, Map.Entry<String, Object> fixedIpsEntry) {
    if (nonNull(fixedIpsEntry)) {
      if (fixedIpsEntry.getValue() instanceof Map) {

        String fixedIpsName = ValidationUtil
                .getWantedNameFromPropertyValueGetParam(fixedIpsEntry.getValue());
          if (nonNull(fixedIpsName) && !ValidationUtil
                  .evalPattern(fixedIpsName, regexList)) {
            globalContext.addMessage(
                    fileName,
                    ErrorLevel.WARNING, ErrorMessagesFormatBuilder.getErrorWithParameters(
                            ERROR_CODE_NNP1,
                            Messages.PARAMETER_NAME_NOT_ALIGNED_WITH_GUIDELINES.getErrorMessage(),
                            "Port", "Fixed_IPS", fixedIpsName, resourceEntry.getKey()),
                    LoggerTragetServiceName.VALIDATE_FIXED_IPS_NAME,
                    LoggerErrorDescription.NAME_NOT_ALIGNED_WITH_GUIDELINES);
          }


      } else {
        globalContext.addMessage(
                fileName,
                ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                        .getErrorWithParameters(
                                ERROR_CODE_NNP2, Messages.MISSING_GET_PARAM.getErrorMessage(),
                                "fixed_ips", resourceEntry.getKey()),
                LoggerTragetServiceName.VALIDATE_FIXED_IPS_NAME,
                LoggerErrorDescription.MISSING_GET_PARAM);
      }
    }
  }

  private void validateParamNamingConvention(String fileName, String resourceId,
                                             Object propertyValue,
                                              String[] regexList,
                                             Messages message,
                                             GlobalValidationContext globalContext) {

    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("file", fileName);

    Object paramName;
    if (propertyValue instanceof Map) {
      paramName = ((Map) propertyValue).get("get_param");
        if (paramName instanceof String && !ValidationUtil
                .evalPattern((String) paramName, regexList)) {
          globalContext.addMessage(
                  fileName,
                  ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                          .getErrorWithParameters(ERROR_CODE_NNP3, message.getErrorMessage(), "Port",
                                  "Network", (String) paramName, resourceId),
                  LoggerTragetServiceName.VALIDATE_PORT_NETWORK_NAME,
                  LoggerErrorDescription.NAME_NOT_ALIGNED_WITH_GUIDELINES);
        }

    } else {
      globalContext.addMessage(
              fileName,
              ErrorLevel.WARNING,
              ErrorMessagesFormatBuilder
                      .getErrorWithParameters(
                              ERROR_CODE_NNP2, Messages.MISSING_GET_PARAM.getErrorMessage(),
                              "network or network_id", resourceId),
              LoggerTragetServiceName.VALIDATE_PORT_NETWORK_NAME,
              LoggerErrorDescription.MISSING_GET_PARAM);
    }

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", fileName);
  }
}
