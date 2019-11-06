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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.core.validation.ErrorMessageCode;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.DefinedHeatParameterTypes;
import org.openecomp.sdc.heat.datatypes.model.Environment;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.datatypes.model.ResourceReferenceFunctions;
import org.openecomp.sdc.heat.services.HeatStructureUtil;
import org.openecomp.sdc.validation.ResourceValidator;
import org.openecomp.sdc.validation.ValidationContext;
import org.openecomp.sdc.validation.type.NamingConventionValidationContext;
import org.openecomp.sdc.validation.util.ValidationUtil;

public class NovaServerNamingConventionGuideLineValidator implements ResourceValidator {
  private static final String AVAILABILITY_ZONE = "availability_zone";
  private static final String SERVER = "Server";
  private static final ErrorMessageCode ERROR_CODE_NNS1 = new ErrorMessageCode("NNS1");
  private static final ErrorMessageCode ERROR_CODE_NNS2 = new ErrorMessageCode("NNS2");
  private static final ErrorMessageCode ERROR_CODE_NNS3 = new ErrorMessageCode("NNS3");
  private static final ErrorMessageCode ERROR_CODE_NNS4 = new ErrorMessageCode("NNS4");
  private static final ErrorMessageCode ERROR_CODE_NNS5 = new ErrorMessageCode("NNS5");
  private static final ErrorMessageCode ERROR_CODE_NNS6 = new ErrorMessageCode("NNS6");
  private static final ErrorMessageCode ERROR_CODE_NNS7 = new ErrorMessageCode("NNS7");
  private static final ErrorMessageCode ERROR_CODE_NNS8 = new ErrorMessageCode("NNS8");
  private static final ErrorMessageCode ERROR_CODE_NNS9 = new ErrorMessageCode("NNS9");
  private static final ErrorMessageCode ERROR_CODE_NNS10 = new ErrorMessageCode("NNS10");
  private static final ErrorMessageCode ERROR_CODE_NNS11 = new ErrorMessageCode("NNS11");
  private static final ErrorMessageCode ERROR_CODE_NNS12 = new ErrorMessageCode("NNS12");
  private static final ErrorMessageCode ERROR_CODE_NNS13 = new ErrorMessageCode("NNS13");
  private static final ErrorMessageCode ERROR_CODE_NNS14 = new ErrorMessageCode("NNS14");

  @Override
  public void validate(String fileName, Map.Entry<String, Resource> resourceEntry,
                       GlobalValidationContext globalContext, ValidationContext validationContext) {

    NamingConventionValidationContext namingConventionValidationContext =
            (NamingConventionValidationContext)validationContext;
    validateHeatNovaResource(fileName, namingConventionValidationContext.getEnvFileName(),
            namingConventionValidationContext.getHeatOrchestrationTemplate(),
            globalContext);
  }

  private void validateHeatNovaResource(String fileName, String envFileName,
                                        HeatOrchestrationTemplate heatOrchestrationTemplate,
                                        GlobalValidationContext globalContext) {
    //if no resources exist return
    if (MapUtils.isEmpty(heatOrchestrationTemplate.getResources())) {
      return;
    }

    heatOrchestrationTemplate
            .getResources()
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().getType()
                    .equals(HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource()))
            .forEach( entry -> validateNovaServerResourceType(entry.getKey(), fileName, envFileName,
                    entry, new HashMap<String, String>(), heatOrchestrationTemplate, globalContext));
  }

  private void validateNovaServerResourceType(String resourceId, String fileName,
                                              String envFileName,
                                              Map.Entry<String, Resource> resourceEntry,
                                              Map<String, String> uniqueResourcePortNetworkRole,
                                              HeatOrchestrationTemplate heatOrchestrationTemplate,
                                              GlobalValidationContext globalContext) {
    validateNovaServerResourceMetaData(fileName, resourceId,
            heatOrchestrationTemplate.getResources().get(resourceId), globalContext);
    validateNovaServerResourceNetworkUniqueRole(fileName, resourceId, uniqueResourcePortNetworkRole,
            heatOrchestrationTemplate, globalContext);
    validateAvailabilityZoneName(fileName, resourceEntry, globalContext);
    validateNovaServerNameImageAndFlavor(fileName, envFileName, resourceEntry, globalContext);
  }

  @SuppressWarnings("unchecked")
  private void validateNovaServerResourceMetaData(String fileName, String resourceId,
                                                  Resource resource,
                                                  GlobalValidationContext globalValidationContext) {
    Map<String, Object> novaServerProp = resource.getProperties();
    Object novaServerPropMetadata;
    if (MapUtils.isNotEmpty(novaServerProp)) {
      novaServerPropMetadata = novaServerProp.get("metadata");
      if (novaServerPropMetadata == null) {
        globalValidationContext.addMessage(
                fileName,
                ErrorLevel.WARNING,
                ErrorMessagesFormatBuilder
                        .getErrorWithParameters(
                                ERROR_CODE_NNS1, Messages.MISSING_NOVA_SERVER_METADATA.getErrorMessage(),
                                resourceId));
      } else if (novaServerPropMetadata instanceof Map) {
        TreeMap<String, Object> propertyMap = new TreeMap((Comparator<String>) String::compareToIgnoreCase);
        propertyMap.putAll((Map) novaServerPropMetadata);
        if (!propertyMap.containsKey("vf_module_id")) {
          globalValidationContext.addMessage(
                  fileName,
                  ErrorLevel.WARNING,
                  ErrorMessagesFormatBuilder.getErrorWithParameters(
                          ERROR_CODE_NNS2, Messages.MISSING_NOVA_SERVER_VF_MODULE_ID.getErrorMessage(),
                          resourceId));
        }
        if (!propertyMap.containsKey("vnf_id")) {
          globalValidationContext.addMessage(
                  fileName, ErrorLevel.WARNING,
                  ErrorMessagesFormatBuilder
                          .getErrorWithParameters(
                                  ERROR_CODE_NNS3, Messages.MISSING_NOVA_SERVER_VNF_ID.getErrorMessage(),
                                  resourceId));
        }
      }
    }
  }

  private void validateNovaServerResourceNetworkUniqueRole(String fileName, String resourceId,
                                                           Map<String, String> uniqueResourcePortNetworkRole,
                                                           HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                           GlobalValidationContext globalValidationContext) {
      if (MapUtils.isEmpty(heatOrchestrationTemplate.getResources().get(resourceId).getProperties())) {
          return;
      }
    Object propertyNetworkValue =
            heatOrchestrationTemplate.getResources().get(resourceId).getProperties().get("networks");
    if (propertyNetworkValue != null && propertyNetworkValue instanceof List) {
      List<String> portResourceIdList =
              getNovaNetworkPortResourceList(fileName, (List) propertyNetworkValue,
                      globalValidationContext);
      for (String portResourceId : portResourceIdList) {
        Resource portResource = heatOrchestrationTemplate.getResources().get(portResourceId);

        if (portResource != null && portResource.getType()
                .equals(HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource())) {
          validateUniqueResourcePortNetworkRole(fileName, resourceId,
                  uniqueResourcePortNetworkRole, globalValidationContext,
                  portResourceId, portResource);
        }
      }
    }
  }

  private void validateUniqueResourcePortNetworkRole(String fileName, String resourceId,
                                      Map<String, String> uniqueResourcePortNetworkRole,
                                      GlobalValidationContext globalValidationContext,
                                      String portResourceId, Resource portResource) {
    String role = null;
    Object network;
    Map portNetwork =
            getPortNetwork(fileName, resourceId, portResource, globalValidationContext);
    if (Objects.nonNull(portNetwork)) {
      network = portNetwork.get("get_param");
      if (Objects.nonNull(network)) {
        if (network instanceof String ){
          role = getNetworkRole((String)network);
        }else if (network instanceof List){
          role = getNetworkRole((String)((List) network).get(0));
        }
        if (role != null && uniqueResourcePortNetworkRole.containsKey(role)) {
          globalValidationContext.addMessage(
                  fileName,
                  ErrorLevel.WARNING,
                  ErrorMessagesFormatBuilder.getErrorWithParameters(
                          ERROR_CODE_NNS12, Messages.RESOURCE_CONNECTED_TO_TWO_EXTERNAL_NETWORKS_WITH_SAME_ROLE
                                  .getErrorMessage(), role, resourceId));
        } else {
          uniqueResourcePortNetworkRole.put(role, resourceId);
        }
      }
    }
  }

  private List<String> getNovaNetworkPortResourceList(String filename, List propertyNetworkValue,
                                                      GlobalValidationContext globalContext) {
    globalContext.setMessageCode(ERROR_CODE_NNS14);
    List<String> portResourceIdList = new ArrayList<>();
    for (Object propValue : propertyNetworkValue) {
      Object portPropValue = ((Map) propValue).get("port");
      Collection<String> portResourceIds = HeatStructureUtil
              .getReferencedValuesByFunctionName(filename, "get_resource", portPropValue,
                      globalContext);
      if (portResourceIds != null) {
        portResourceIdList.addAll(portResourceIds);
      }
    }

    return portResourceIdList;
  }

  private String getNetworkRole(String network) {
    if (network == null) {
      return null;
    }
    if (network.contains("_net_id")) {
      return network.substring(0, network.indexOf("_net_id"));
    } else if (network.contains("_net_name")) {
      return network.substring(0, network.indexOf("_net_name"));
    } else if (network.contains("_net_fqdn")) {
      return network.substring(0, network.indexOf("_net_fqdn"));
    }
    return null;
  }

  private Map getPortNetwork(String fileName, String resourceId, Resource portResource,
                             GlobalValidationContext globalValidationContext) {
    Object portNetwork = portResource.getProperties().get("network_id");
    if (portNetwork == null) {
      portNetwork = portResource.getProperties().get("network");
    }
    if (!(portNetwork instanceof Map)) {
      globalValidationContext.addMessage(
              fileName,
              ErrorLevel.WARNING,
              ErrorMessagesFormatBuilder
                      .getErrorWithParameters(
                              ERROR_CODE_NNS4, Messages.MISSING_GET_PARAM.getErrorMessage(),
                              "network or network_id", resourceId));
      return null;
    }
    return (Map) portNetwork;
  }

  private void validateAvailabilityZoneName(String fileName,
                                            Map.Entry<String, Resource> resourceEntry,
                                            GlobalValidationContext globalContext) {
    String[] regexList = new String[]{"availability_zone_(\\d+)"};

    if (MapUtils.isEmpty(resourceEntry.getValue().getProperties())) {
      return;
    }

    Object availabilityZoneMap =  resourceEntry.getValue().getProperties()
            .get(AVAILABILITY_ZONE);

    if (nonNull(availabilityZoneMap)) {
      if (availabilityZoneMap instanceof Map) {
        String availabilityZoneName = ValidationUtil.getWantedNameFromPropertyValueGetParam
                (availabilityZoneMap);

          if (availabilityZoneName != null && !ValidationUtil
                  .evalPattern(availabilityZoneName, regexList)) {
            globalContext.addMessage(
                    fileName,
                    ErrorLevel.WARNING, ErrorMessagesFormatBuilder.getErrorWithParameters(
                            ERROR_CODE_NNS5, Messages.PARAMETER_NAME_NOT_ALIGNED_WITH_GUIDELINES.getErrorMessage(),
                            ValidationUtil.getMessagePartAccordingToResourceType(resourceEntry),
                            "Availability Zone", availabilityZoneName, resourceEntry.getKey()));
          }
      } else {
        globalContext.addMessage(
                fileName,
                ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                        .getErrorWithParameters(
                                ERROR_CODE_NNS6, Messages.MISSING_GET_PARAM.getErrorMessage(),
                                AVAILABILITY_ZONE, resourceEntry.getKey()));
      }
    }
  }

  private void validateNovaServerNameImageAndFlavor(String fileName, String envFileName,
                                                    Map.Entry<String, Resource> resourceEntry,
                                                    GlobalValidationContext globalContext) {
    String novaName =
            validateNovaServerNamingConvention(fileName, envFileName, resourceEntry, globalContext);
    Map<String, String> legalNovaNamingConventionMap =
            validateImageAndFlavorFromNovaServer(fileName, resourceEntry, globalContext);

    if (Objects.nonNull(novaName)) {
      legalNovaNamingConventionMap.put("name", novaName);
    }

    if (legalNovaNamingConventionMap.keySet().size() > 1) {
      validateNovaServerNameImageAndFlavorSync(fileName, resourceEntry,
              legalNovaNamingConventionMap, globalContext);
    }
  }

  private String validateNovaServerNamingConvention(String fileName, String envFileName,
                                                    Map.Entry<String, Resource> resourceEntry,
                                                    GlobalValidationContext globalContext) {
    if (MapUtils.isEmpty(resourceEntry.getValue().getProperties())) {
      return null;
    }
    return checkIfNovaNameByGuidelines(fileName, envFileName, resourceEntry, globalContext);
  }

  private Map<String, String> validateImageAndFlavorFromNovaServer(String fileName,
                                                                   Map.Entry<String, Resource> resourceEntry,
                                                                   GlobalValidationContext globalContext) {
      Map<String, String> imageAndFlavorLegalNames = new HashMap<>();

    if (MapUtils.isEmpty(resourceEntry.getValue().getProperties())) {
      return imageAndFlavorLegalNames;
    }

    Pair<String, String> imagePair = new ImmutablePair<>("image", ".*_image_name");
    Pair<String, String> flavorPair = new ImmutablePair<>("flavor", ".*_flavor_name");
    List<Pair<String, String>> imageFlavorPairs = Arrays.asList(imagePair, flavorPair);
    Map<String, Object> propertiesMap = resourceEntry.getValue().getProperties();

    for (Pair<String, String> imageOrFlavor : imageFlavorPairs) {
      boolean isErrorInImageOrFlavor =
              isErrorExistWhenValidatingImageOrFlavorNames(fileName, imageOrFlavor, resourceEntry,
                      propertiesMap, globalContext);
      if (!isErrorInImageOrFlavor) {
        Object nameValue = propertiesMap.get(imageOrFlavor.getKey());
        String imageOrFlavorName = ValidationUtil.getWantedNameFromPropertyValueGetParam
                (nameValue);
        imageAndFlavorLegalNames.put(imageOrFlavor.getKey(), imageOrFlavorName);
      }
    }
    return imageAndFlavorLegalNames;
  }

  private String checkIfNovaNameByGuidelines(String fileName, String envFileName,
                                             Map.Entry<String, Resource> resourceEntry,
                                             GlobalValidationContext globalContext) {
    if (MapUtils.isEmpty(resourceEntry.getValue().getProperties())) {
      return null;
    }
    Object novaNameGetParam = getNovaServerName(resourceEntry);
    String novaName = null;
    if (nonNull(novaNameGetParam)) {
      novaName =
              checkNovaNameGetParamValueMap(fileName, novaNameGetParam, resourceEntry, globalContext);
      checkIfNovaNameParameterInEnvIsStringOrList(fileName, envFileName, novaName, resourceEntry,
              globalContext);
    } else {
      globalContext.addMessage(
              fileName,
              ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                      .getErrorWithParameters(
                              ERROR_CODE_NNS7, Messages.MISSING_GET_PARAM.getErrorMessage(),
                              "nova server name", resourceEntry.getKey()));
    }

    return novaName;
  }

  private boolean isErrorExistWhenValidatingImageOrFlavorNames(String fileName,
                                                               Pair<String, String> propertyNameAndRegex,
                                                               Map.Entry<String, Resource> resourceEntry,
                                                               Map<String, Object> propertiesMap,
                                                               GlobalValidationContext globalContext) {
    String propertyName = propertyNameAndRegex.getKey();
    Object nameValue = propertiesMap.get(propertyName);
    String[] regexList = new String[]{propertyNameAndRegex.getValue()};


    if (nonNull(nameValue)) {
      if (nameValue instanceof Map) {
        globalContext.setMessageCode(ERROR_CODE_NNS13);
        if (ValidationUtil.validateMapPropertyValue(fileName, resourceEntry, globalContext,
                propertyName,
                nameValue, regexList)) {
          return true;
        }
      } else {
        globalContext.addMessage(
                fileName,
                ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                        .getErrorWithParameters(
                                ERROR_CODE_NNS8, Messages.MISSING_GET_PARAM.getErrorMessage(),
                                propertyName, resourceEntry.getKey()));
        return true;
      }

      return false;
    }
    return false;
  }

  private Object getNovaServerName(Map.Entry<String, Resource> resourceEntry) {
    Object novaServerName = resourceEntry.getValue().getProperties().get("name");
    Map novaNameMap;
      if (nonNull(novaServerName) && novaServerName instanceof Map) {
        novaNameMap = (Map) novaServerName;
        return novaNameMap.get(ResourceReferenceFunctions.GET_PARAM.getFunction());
      }

    return null;
  }

  @SuppressWarnings("unchecked")
  private String checkNovaNameGetParamValueMap(String fileName, Object getParamValue,
                                               Map.Entry<String, Resource> resourceEntry,
                                               GlobalValidationContext globalContext) {
    if (getParamValue instanceof List) {
      List<Object> getParamNameList = (List) getParamValue;
      String[] regexName = new String[]{".*_names"};
      return isNovaNameAsListLegal(fileName, regexName, getParamNameList, resourceEntry,
              globalContext);
    } else if (getParamValue instanceof String) {
      String[] regexName = new String[]{".*_name_(\\d+)"};
      return isNovaNameAsStringLegal(fileName, (String) getParamValue, regexName, resourceEntry,
              globalContext);
    }

    return null;
  }

  private void checkIfNovaNameParameterInEnvIsStringOrList(String fileName,
                                                           String envFileName,
                                                           String novaServerName,
                                                           Map.Entry<String, Resource> resourceEntry,
                                                           GlobalValidationContext globalContext) {
    if (nonNull(envFileName)) {
      Environment environment = ValidationUtil.validateEnvContent(envFileName, globalContext);

      if (environment != null && MapUtils.isNotEmpty(environment.getParameters())) {
        Object novaServerNameEnvValue = environment.getParameters()
                        .get(novaServerName);
          if (Objects.nonNull(novaServerNameEnvValue) && !DefinedHeatParameterTypes
                  .isNovaServerEnvValueIsFromRightType(novaServerNameEnvValue)) {
            globalContext.addMessage(
                    fileName,
                    ErrorLevel.WARNING, ErrorMessagesFormatBuilder.getErrorWithParameters(
                            ERROR_CODE_NNS9, Messages.PARAMETER_NAME_NOT_ALIGNED_WITH_GUIDELINES.getErrorMessage(),
                            SERVER, "Name",
                            novaServerNameEnvValue.toString(), resourceEntry.getKey()));
          }
      }
    }
  }

  private String isNovaNameAsListLegal(String fileName,
                                       String[] regexName,
                                       List<Object> getParamNameList,
                                       Map.Entry<String, Resource> resourceEntry,
                                       GlobalValidationContext globalContext) {

    if (getParamNameList.size() != 2 || !ValidationUtil.evalPattern(getParamNameList.get(0),
            regexName)) {
      globalContext.addMessage(
              fileName,
              ErrorLevel.WARNING,
              ErrorMessagesFormatBuilder.getErrorWithParameters(
                      ERROR_CODE_NNS10, Messages.PARAMETER_NAME_NOT_ALIGNED_WITH_GUIDELINES.getErrorMessage(),
                      SERVER,
                      "name", getParamNameList.toString(), resourceEntry.getKey()));
      return null;
    }

    return (String) getParamNameList.get(0);
  }

  private String isNovaNameAsStringLegal(String fileName,
                                         String novaName,
                                         String[] regexName,
                                         Map.Entry<String, Resource> resourceEntry,
                                         GlobalValidationContext globalContext) {
    if (!ValidationUtil.evalPattern(novaName, regexName)) {
      globalContext.addMessage(
              fileName,
              ErrorLevel.WARNING,
              ErrorMessagesFormatBuilder.getErrorWithParameters(
                      ERROR_CODE_NNS10, Messages.PARAMETER_NAME_NOT_ALIGNED_WITH_GUIDELINES.getErrorMessage(),
                      SERVER,
                      "name", novaName, resourceEntry.getKey()));
      return null;
    }
    return novaName;
  }

  private void validateNovaServerNameImageAndFlavorSync(String fileName,
                                                        Map.Entry<String, Resource> resourceEntry,
                                                        Map<String, String> legalNovaNamingConventionNames,
                                                        GlobalValidationContext globalContext) {
    List<String> vmNames = new LinkedList<>();

    for (Map.Entry<String, String> nameEntry : legalNovaNamingConventionNames.entrySet()) {
      vmNames.add(getVmName(nameEntry.getValue(), nameEntry.getKey()));
    }

    vmNames.removeIf(Objects::isNull);

    if (!isVmNameSync(vmNames)) {
      globalContext.addMessage(
              fileName,
              ErrorLevel.WARNING,
              ErrorMessagesFormatBuilder.getErrorWithParameters(
                      ERROR_CODE_NNS11, Messages.NOVA_NAME_IMAGE_FLAVOR_NOT_CONSISTENT.getErrorMessage(),
                      resourceEntry.getKey()));
    }
  }

  private String getVmName(String nameToGetVmNameFrom, String stringToGetIndexOf) {
    int vmIndex =
            nameToGetVmNameFrom == null ? -1 : nameToGetVmNameFrom.indexOf(stringToGetIndexOf);
    String vmName = null;
    if (nameToGetVmNameFrom != null) {
      vmName = vmIndex < 0 ? null
              : trimNonAlphaNumericCharactersFromEndOfString(nameToGetVmNameFrom.substring(0, vmIndex));
    }
    return vmName;
  }

  private boolean isVmNameSync(List<String> namesToCompare) {
    int size = namesToCompare.size();
    for (int i = 0; i < size - 1; i++) {
      if (!namesToCompare.get(i).equals(namesToCompare.get(i + 1))) {
        return false;
      }
    }
    return true;
  }

  private String trimNonAlphaNumericCharactersFromEndOfString(String toTrim) {
    int stringSize = toTrim.length();
    int stringLength = stringSize - 1;
    String[] regexList = new String[]{"[^a-zA-Z0-9]"};

    while (stringLength >= 0) {
      if (!ValidationUtil.evalPattern(String.valueOf(toTrim.charAt(stringLength)), regexList)) {
        break;
      }
      stringLength--;
    }

    return toTrim.substring(0, stringLength + 1);
  }
}
