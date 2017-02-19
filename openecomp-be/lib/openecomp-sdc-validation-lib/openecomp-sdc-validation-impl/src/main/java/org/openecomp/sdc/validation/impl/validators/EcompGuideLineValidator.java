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

package org.openecomp.sdc.validation.impl.validators;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.core.utilities.yaml.YamlUtil;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.errors.Messages;
import org.openecomp.core.validation.interfaces.Validator;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.utils.AsdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.model.heat.ForbiddenHeatResourceTypes;
import org.openecomp.sdc.heat.datatypes.DefinedHeatParameterTypes;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;
import org.openecomp.sdc.heat.datatypes.model.Environment;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.datatypes.model.ResourceReferenceFunctions;
import org.openecomp.sdc.heat.services.HeatStructureUtil;
import org.openecomp.sdc.heat.services.manifest.ManifestUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class EcompGuideLineValidator extends HeatValidator implements Validator {
  @Override
  public void validate(GlobalValidationContext globalContext) {

    ManifestContent manifestContent;
    try {
      manifestContent = checkValidationPreCondition(globalContext);
    } catch (Exception exception) {
      return;
    }

    //global validations
    Set<String> baseFiles = validateManifest(manifestContent, globalContext);

    Map<String, FileData.Type> fileTypeMap = ManifestUtil.getFileTypeMap(manifestContent);
    Map<String, FileData> fileEnvMap = ManifestUtil.getFileAndItsEnv(manifestContent);
    globalContext
        .getFiles()
        .stream()
        .filter(fileName -> FileData
            .isHeatFile(fileTypeMap.get(fileName)))
        .forEach(fileName -> validate(fileName,
            fileEnvMap.get(fileName) != null ? fileEnvMap.get(fileName).getFile() : null,
            fileTypeMap, baseFiles, globalContext));
  }

  private void validate(String fileName, String envFileName, Map<String, FileData.Type> fileTypeMap,
                        Set<String> baseFiles, GlobalValidationContext globalContext) {
    HeatOrchestrationTemplate heatOrchestrationTemplate =
        checkHeatOrchestrationPreCondition(fileName, globalContext);
    if (heatOrchestrationTemplate == null) {
      return;
    }

    validateBaseFile(fileName, baseFiles, heatOrchestrationTemplate, globalContext);
    validateHeatVolumeFile(fileName, fileTypeMap, heatOrchestrationTemplate, globalContext);
    validateHeatNamingConvention(fileName, heatOrchestrationTemplate, globalContext);
    validateHeatNovaResource(fileName, envFileName, heatOrchestrationTemplate, globalContext);
    validateResourceTypeIsForbidden(fileName, heatOrchestrationTemplate, globalContext);
    validateFixedIpsNamingConvention(fileName, heatOrchestrationTemplate, globalContext);
  }

  private void validateHeatNovaResource(String fileName, String envFileName,
                                        HeatOrchestrationTemplate heatOrchestrationTemplate,
                                        GlobalValidationContext globalContext) {
    Map<String, String> uniqueResourcePortNetworkRole = new HashMap<>();
    //if no resources exist return
    if (heatOrchestrationTemplate.getResources() == null
        || heatOrchestrationTemplate.getResources().size() == 0) {
      return;
    }

    heatOrchestrationTemplate
        .getResources()
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue().getType()
            .equals(HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource()))
        .forEach(entry -> validateNovaServerResourceType(entry.getKey(), fileName, envFileName,
            entry, uniqueResourcePortNetworkRole, heatOrchestrationTemplate, globalContext));
  }

  private void validateNovaServerResourceType(String resourceId, String fileName,
                                              String envFileName,
                                              Map.Entry<String, Resource> resourceEntry,
                                              Map<String, String> uniqueResourcePortNetworkRole,
                                              HeatOrchestrationTemplate heatOrchestrationTemplate,
                                              GlobalValidationContext globalValidationContext) {
    validateNovaServerResourceMetaData(fileName, resourceId,
        heatOrchestrationTemplate.getResources().get(resourceId), globalValidationContext);
    validateNovaServerResourceNetworkUniqueRole(fileName, resourceId, heatOrchestrationTemplate,
        globalValidationContext);
    validateNovaServerNamingConvention(fileName, envFileName, resourceEntry,
        globalValidationContext);
    validateNovaServerAvailabilityZoneName(fileName, resourceEntry, globalValidationContext);
    validateImageAndFlavorFromNovaServer(fileName, resourceEntry, globalValidationContext);
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
                .getErrorWithParameters(Messages.MISSING_NOVA_SERVER_METADATA.getErrorMessage(),
                    resourceId));
      } else if (novaServerPropMetadata instanceof Map) {
        TreeMap<String, Object> propertyMap = new TreeMap(new Comparator<String>() {

          @Override
          public int compare(String o1, String o2) {
            return o1.compareToIgnoreCase(o2);
          }

          @Override
          public boolean equals(Object obj) {
            return false;
          }
        });
        propertyMap.putAll((Map) novaServerPropMetadata);
        if (!propertyMap.containsKey("vf_module_id")) {
          globalValidationContext.addMessage(fileName, ErrorLevel.WARNING,
              ErrorMessagesFormatBuilder.getErrorWithParameters(
                  Messages.MISSING_NOVA_SERVER_VF_MODULE_ID.getErrorMessage(), resourceId));
        }
        if (!propertyMap.containsKey("vnf_id")) {
          globalValidationContext.addMessage(fileName, ErrorLevel.WARNING,
              ErrorMessagesFormatBuilder
                  .getErrorWithParameters(Messages.MISSING_NOVA_SERVER_VNF_ID.getErrorMessage(),
                      resourceId));
        }
      }
    }
  }

  private void validateNovaServerResourceNetworkUniqueRole(String fileName, String resourceId,
                                                           HeatOrchestrationTemplate
                                                               heatOrchestrationTemplate,
                                                           GlobalValidationContext
                                                               globalValidationContext) {

    String network;
    String role;
    Map<String, String> uniqueResourcePortNetworkRole = new HashMap<>();

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
          Map portNetwork =
              getPortNetwork(fileName, resourceId, portResource, globalValidationContext);
          if (Objects.nonNull(portNetwork)) {
            network = (String) portNetwork.get("get_param");
            if (Objects.nonNull(network)) {
              role = getNetworkRole(network);
              if (role != null && uniqueResourcePortNetworkRole.containsKey(role)) {
                globalValidationContext.addMessage(fileName, ErrorLevel.WARNING,
                    ErrorMessagesFormatBuilder.getErrorWithParameters(
                        Messages.RESOURCE_CONNECTED_TO_TWO_EXTERNAL_NETWORKS_WITH_SAME_ROLE
                            .getErrorMessage(), resourceId, role));
              } else {
                uniqueResourcePortNetworkRole.put(role, portResourceId);
              }
            }
          }
        }
      }
    }
  }


  private Map getPortNetwork(String fileName, String resourceId, Resource portResource,
                             GlobalValidationContext globalValidationContext) {
    Object portNetwork = portResource.getProperties().get("network_id");
    if (portNetwork == null) {
      portNetwork = portResource.getProperties().get("network");
    }
    if (!(portNetwork instanceof Map)) {
      globalValidationContext.addMessage(fileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
          .getErrorWithParameters(Messages.MISSING_GET_PARAM.getErrorMessage(),
              "network or network_id", resourceId));
      return null;
    }
    return (Map) portNetwork;
  }

  private List<String> getNovaNetworkPortResourceList(String filename, List propertyNetworkValue,
                                                      GlobalValidationContext globalContext) {
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
    } else if (network.contains("net_name")) {
      return network.substring(0, network.indexOf("_net_name"));
    } else if (network.contains("net_fqdn")) {
      return network.substring(0, network.indexOf("_net_fqdn"));
    }
    return null;
  }

  private void validateHeatNamingConvention(String fileName,
                                            HeatOrchestrationTemplate heatOrchestrationTemplate,
                                            GlobalValidationContext globalContext) {
    validatePortNetworkNamingConvention(fileName, heatOrchestrationTemplate, globalContext);
  }

  private void validatePortNetworkNamingConvention(String fileName,
                                                   HeatOrchestrationTemplate
                                                       heatOrchestrationTemplate,
                                                   GlobalValidationContext globalContext) {
    if (MapUtils.isEmpty(heatOrchestrationTemplate.getResources())) {
      return;
    }
    String[] regexList = new String[]{".*_net_id", ".*_net_name", ".*_net_fqdn"};

    heatOrchestrationTemplate
        .getResources()
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue().getType() != null && entry.getValue().getType()
            .equals(HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource()))
        .forEach(entry -> entry.getValue()
            .getProperties()
            .entrySet()
            .stream()
            .filter(propertyEntry -> propertyEntry != null
                && (propertyEntry.getKey().toLowerCase().equals("network".toLowerCase())
                ||
                propertyEntry.getKey().equals("network_id")))
            .forEach(propertyEntry -> validateParamNamingConvention(fileName, entry.getKey(),
                propertyEntry.getValue(), regexList,
                Messages.NETWORK_PARAM_NOT_ALIGNED_WITH_GUIDE_LINE, globalContext)));
  }

  private void validateParamNamingConvention(String fileName, String resourceId,
                                             Object propertyValue, String[] regexList,
                                             Messages message,
                                             GlobalValidationContext globalContext) {
    Object paramName;
    if (propertyValue instanceof Map) {
      paramName = ((Map) propertyValue).get("get_param");
      if (paramName instanceof String) {
        if (!evalPattern((String) paramName, regexList)) {
          globalContext.addMessage(fileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
              .getErrorWithParameters(message.getErrorMessage(), (String) paramName, resourceId));
        }
      }
    } else {
      globalContext.addMessage(fileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
          .getErrorWithParameters(Messages.MISSING_GET_PARAM.getErrorMessage(),
              "network or network_id", resourceId));
    }
  }

  private boolean evalPattern(Object paramVal, String[] regexList) {
    String value = "";
    if (paramVal instanceof String) {
      value = ((String) paramVal);
    }
    if (paramVal instanceof Integer) {
      value = paramVal.toString();
    }
    return evalPattern(value, regexList);
  }

  private boolean evalPattern(String paramVal, String[] regexList) {

    for (String regex : regexList) {
      if (Pattern.matches(regex, paramVal)) {
        return true;
      }
    }

    return false;
  }


  private void validateHeatVolumeFile(String fileName, Map<String, FileData.Type> fileTypeMap,
                                      HeatOrchestrationTemplate heatOrchestrationTemplate,
                                      GlobalValidationContext globalContext) {
    //if not heat volume return
    if (!fileTypeMap.get(fileName).equals(FileData.Type.HEAT_VOL)) {
      return;
    }

    //if no resources exist return
    if (heatOrchestrationTemplate.getResources() == null
        || heatOrchestrationTemplate.getResources().size() == 0) {
      return;
    }

    Set<String> expectedExposedResources = new HashSet<>();
    Set<String> actualExposedResources = new HashSet<>();
    heatOrchestrationTemplate.getResources()
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue().getType()
            .equals(HeatResourcesTypes.CINDER_VOLUME_RESOURCE_TYPE.getHeatResource()))
        .forEach(entry -> expectedExposedResources.add(entry.getKey()));

    if (heatOrchestrationTemplate.getOutputs() != null) {

      heatOrchestrationTemplate.getOutputs().entrySet()
          .stream()
          .filter(entry -> isPropertyValueGetResource(fileName, entry.getValue().getValue(),
              globalContext))
          .forEach(entry -> actualExposedResources.add(
              getResourceIdFromPropertyValue(fileName, entry.getValue().getValue(),
                  globalContext)));
    }

    actualExposedResources.stream().forEach(expectedExposedResources::remove);

    if (expectedExposedResources.size() > 0) {
      expectedExposedResources
          .stream()
          .forEach(name -> globalContext.addMessage(fileName, ErrorLevel.WARNING,
              ErrorMessagesFormatBuilder
                  .getErrorWithParameters(Messages.VOLUME_HEAT_NOT_EXPOSED.getErrorMessage(),
                      name)));
    }
  }

  private void validateBaseFile(String fileName, Set<String> baseFiles,
                                HeatOrchestrationTemplate heatOrchestrationTemplate,
                                GlobalValidationContext globalContext) {

    //if not base return
    if (baseFiles == null || !baseFiles.contains(fileName)) {
      return;
    }

    //if no resources exist return
    if (heatOrchestrationTemplate.getResources() == null
        || heatOrchestrationTemplate.getResources().size() == 0) {
      return;
    }

    Set<String> expectedExposedResources = new HashSet<>();
    Set<String> actualExposedResources = new HashSet<>();
    heatOrchestrationTemplate.getResources()
        .entrySet()
        .stream()
        .filter(entry -> isExpectedToBeExposed(entry.getValue().getType()))
        .forEach(entry -> expectedExposedResources.add(entry.getKey()));

    if (heatOrchestrationTemplate.getOutputs() != null) {

      heatOrchestrationTemplate.getOutputs().entrySet()
          .stream()
          .filter(entry -> isPropertyValueGetResource(fileName, entry.getValue().getValue(),
              globalContext))
          .forEach(entry -> actualExposedResources.add(
              getResourceIdFromPropertyValue(fileName, entry.getValue().getValue(),
                  globalContext)));
    }
    actualExposedResources.stream().forEach(expectedExposedResources::remove);

    if (expectedExposedResources.size() > 0) {
      expectedExposedResources
          .stream()
          .forEach(name -> globalContext.addMessage(fileName, ErrorLevel.WARNING,
              ErrorMessagesFormatBuilder
                  .getErrorWithParameters(Messages.RESOURCE_NOT_DEFINED_IN_OUTPUT.getErrorMessage(),
                      name)));
    }
  }

  private void validateResourceTypeIsForbidden(String fileName,
                                               HeatOrchestrationTemplate heatOrchestrationTemplate,
                                               GlobalValidationContext globalContext) {
    if (MapUtils.isEmpty(heatOrchestrationTemplate.getResources())) {
      return;
    }

    heatOrchestrationTemplate.getResources()
        .entrySet()
        .stream()
        .filter(entry ->
            ForbiddenHeatResourceTypes.findByForbiddenHeatResource(entry.getValue().getType())
                != null)
        .filter(entry -> ForbiddenHeatResourceTypes
            .findByForbiddenHeatResource(entry.getValue().getType())
            .equals(ForbiddenHeatResourceTypes.HEAT_FLOATING_IP_TYPE))
        .forEach(entry -> globalContext.addMessage(fileName, ErrorLevel.WARNING,
            ErrorMessagesFormatBuilder
                .getErrorWithParameters(Messages.FLOATING_IP_NOT_IN_USE.getErrorMessage(),
                    entry.getKey())));
  }


  private void validateFixedIpsNamingConvention(String fileName,
                                                HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                GlobalValidationContext globalContext) {
    if (MapUtils.isEmpty(heatOrchestrationTemplate.getResources())) {
      return;
    }

    heatOrchestrationTemplate.getResources()
        .entrySet()
        .stream()
        .filter(entry -> HeatResourcesTypes.findByHeatResource(entry.getValue().getType()) != null)
        .filter(entry -> HeatResourcesTypes.findByHeatResource(entry.getValue().getType())
            .equals(HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE))
        .forEach(entry -> checkNeutronPortFixedIpsName(fileName, entry, globalContext));
  }

  private void validateImageAndFlavorFromNovaServer(String fileName,
                                                    Map.Entry<String, Resource> resourceEntry,
                                                    GlobalValidationContext globalContext) {
    if (MapUtils.isEmpty(resourceEntry.getValue().getProperties())) {
      return;
    }

    String[] imageOrFlavorAsParameters = new String[]{"image", "flavor"};
    Map<String, Object> propertiesMap = resourceEntry.getValue().getProperties();

    for (String imageOrFlavor : imageOrFlavorAsParameters) {
      checkImageAndFlavorNames(fileName, imageOrFlavor, resourceEntry.getKey(), propertiesMap,
          globalContext);
    }
  }

  private void checkImageAndFlavorNames(String fileName, String imageOrFlavor, String resourceId,
                                        Map<String, Object> propertiesMap,
                                        GlobalValidationContext globalContext) {
    Object nameValue =
        propertiesMap.get(imageOrFlavor) == null ? null : propertiesMap.get(imageOrFlavor);
    String[] regexList = new String[]{".*_" + imageOrFlavor + "_name"};

    if (Objects.nonNull(nameValue)) {
      if (nameValue instanceof Map) {
        String imageOrFlavorName = getWantedNameFromPropertyValueGetParam(nameValue);
        if (Objects.nonNull(imageOrFlavorName)) {
          if (!evalPattern(imageOrFlavorName, regexList)) {
            globalContext.addMessage(fileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                .getErrorWithParameters(
                    Messages.WRONG_IMAGE_OR_FLAVOR_NAME_NOVA_SERVER.getErrorMessage(),
                    imageOrFlavor, resourceId));
          }
        }
      } else {
        globalContext.addMessage(fileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
            .getErrorWithParameters(Messages.MISSING_GET_PARAM.getErrorMessage(), imageOrFlavor,
                resourceId));
      }
    }
  }


  @SuppressWarnings("unchecked")
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
    if (Objects.nonNull(fixedIps) && fixedIps instanceof List) {
      List<Object> fixedIpsList = (List<Object>) fixedIps;
      for (Object fixedIpsObject : fixedIpsList) {
        Map.Entry<String, Object> fixedIpsEntry =
            ((Map<String, Object>) fixedIpsObject).entrySet().iterator().next();
        if (Objects.nonNull(fixedIpsEntry)) {
          if (fixedIpsEntry.getValue() instanceof Map) {
            String fixedIpsName = getWantedNameFromPropertyValueGetParam(fixedIpsEntry.getValue());
            if (Objects.nonNull(fixedIpsName)) {
              if (!evalPattern(fixedIpsName, regexList)) {
                globalContext.addMessage(fileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                    .getErrorWithParameters(
                        Messages.FIXED_IPS_NOT_ALIGNED_WITH_GUIDE_LINES.getErrorMessage(),
                        resourceEntry.getKey()));
              }
            }
          } else {
            globalContext.addMessage(fileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                .getErrorWithParameters(Messages.MISSING_GET_PARAM.getErrorMessage(), "fixed_ips",
                    resourceEntry.getKey()));
          }
        }
      }
    }
  }


  private void validateNovaServerNamingConvention(String fileName, String envFileName,
                                                  Map.Entry<String, Resource> resourceEntry,
                                                  GlobalValidationContext globalContext) {
    if (MapUtils.isEmpty(resourceEntry.getValue().getProperties())) {
      return;
    }

    checkIfNovaNameByGuidelines(fileName, envFileName, resourceEntry, globalContext);
  }

  private void checkIfNovaNameByGuidelines(String fileName, String envFileName,
                                           Map.Entry<String, Resource> resourceEntry,
                                           GlobalValidationContext globalContext) {
    if (MapUtils.isEmpty(resourceEntry.getValue().getProperties())) {
      return;
    }

    Object novaServerName = resourceEntry.getValue().getProperties().get("name");
    Map novaNameMap;
    String novaName;
    if (Objects.nonNull(novaServerName)) {
      if (novaServerName instanceof Map) {
        novaNameMap = (Map) novaServerName;
        Object novaNameGetParam =
            novaNameMap.get(ResourceReferenceFunctions.GET_PARAM.getFunction()) == null ? null
                : novaNameMap.get(ResourceReferenceFunctions.GET_PARAM.getFunction());
        if (Objects.nonNull(novaNameGetParam)) {
          checkNovaNameGetParamValueMap(fileName, novaNameGetParam, resourceEntry, globalContext);
          novaName = novaNameGetParam instanceof List ? (String) ((List) novaNameGetParam).get(0)
              : (String) novaNameGetParam;
          checkIfNovaNameParameterInEnvIsStringOrList(fileName, envFileName, resourceEntry,
              novaName, globalContext);
        }
      } else {
        globalContext.addMessage(fileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
            .getErrorWithParameters(Messages.MISSING_GET_PARAM.getErrorMessage(),
                "nova server name", resourceEntry.getKey()));
      }
    }

  }

  private void checkIfNovaNameParameterInEnvIsStringOrList(String fileName, String envFileName,
                                                           Map.Entry<String, Resource>
                                                               resourceEntry,
                                                           String novaServerName,
                                                           GlobalValidationContext globalContext) {
    if (Objects.nonNull(envFileName)) {
      Environment environment = validateEnvContent(envFileName, globalContext);

      if (environment != null && MapUtils.isNotEmpty(environment.getParameters())) {
        Object novaServerNameEnvValue =
            environment.getParameters().containsKey(novaServerName) ? environment.getParameters()
                .get(novaServerName) : null;
        if (Objects.nonNull(novaServerNameEnvValue)) {
          if (!DefinedHeatParameterTypes
              .isNovaServerEnvValueIsFromRightType(novaServerNameEnvValue)) {
            globalContext.addMessage(fileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                .getErrorWithParameters(
                    Messages.NOVA_SERVER_NAME_NOT_ALIGNED_WITH_GUIDE_LINES.getErrorMessage(),
                    resourceEntry.getKey()));
          }
        }
      }
    }
  }


  private void validateNovaServerAvailabilityZoneName(String fileName,
                                                      Map.Entry<String, Resource> resourceEntry,
                                                      GlobalValidationContext globalContext) {
    String[] regexList = new String[]{"availability_zone_(\\d+)"};

    if (MapUtils.isEmpty(resourceEntry.getValue().getProperties())) {
      return;
    }

    Object availabilityZoneMap =
        resourceEntry.getValue().getProperties().containsKey("availability_zone") ? resourceEntry
            .getValue().getProperties().get("availability_zone") : null;

    if (Objects.nonNull(availabilityZoneMap)) {
      if (availabilityZoneMap instanceof Map) {
        String availabilityZoneName = getWantedNameFromPropertyValueGetParam(availabilityZoneMap);

        if (availabilityZoneName != null) {
          if (!evalPattern(availabilityZoneName, regexList)) {
            globalContext.addMessage(fileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                .getErrorWithParameters(
                    Messages.AVAILABILITY_ZONE_NOT_ALIGNED_WITH_GUIDE_LINES.getErrorMessage(),
                    resourceEntry.getKey()));
          }
        }
      } else {
        globalContext.addMessage(fileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
            .getErrorWithParameters(Messages.MISSING_GET_PARAM.getErrorMessage(),
                "availability_zone", resourceEntry.getKey()));
      }
    }

  }

  @SuppressWarnings("unchecked")
  private void checkNovaNameGetParamValueMap(String fileName, Object getParamValue,
                                             Map.Entry<String, Resource> resourceEntry,
                                             GlobalValidationContext globalContext) {
    if (getParamValue instanceof List) {
      List<Object> getParamNameList = (List) getParamValue;
      String[] regexName = new String[]{".*_names"};
      isNovaNameAsListLegal(fileName, getParamNameList, regexName, resourceEntry, globalContext);
    } else if (getParamValue instanceof String) {
      String[] regexName = new String[]{".*_name_(\\d+)"};
      isNovaNameAsStringLegal(fileName, (String) getParamValue, regexName, resourceEntry,
          globalContext);
    }

  }


  private void isNovaNameAsListLegal(String fileName, List<Object> getParamNameList,
                                     String[] regexName, Map.Entry<String, Resource> resourceEntry,
                                     GlobalValidationContext globalContext) {

    if (getParamNameList.size() != 2 || !evalPattern(getParamNameList.get(0), regexName)) {
      globalContext.addMessage(fileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
          .getErrorWithParameters(
              Messages.NOVA_SERVER_NAME_NOT_ALIGNED_WITH_GUIDE_LINES.getErrorMessage(),
              resourceEntry.getKey()));
    }
  }

  private boolean isNovaNameAsStringLegal(String fileName, String novaName, String[] regexName,
                                          Map.Entry<String, Resource> resourceEntry,
                                          GlobalValidationContext globalContext) {
    if (!evalPattern(novaName, regexName)) {
      globalContext.addMessage(fileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
          .getErrorWithParameters(
              Messages.NOVA_SERVER_NAME_NOT_ALIGNED_WITH_GUIDE_LINES.getErrorMessage(),
              resourceEntry.getKey()));
      return false;
    }
    return true;
  }

  private String getWantedNameFromPropertyValueGetParam(Object value) {
    Set<String> paramName = HeatStructureUtil
        .getReferencedValuesByFunctionName(null, ResourceReferenceFunctions.GET_PARAM.getFunction(),
            value, null);
    if (paramName != null && CollectionUtils.isNotEmpty(paramName)) {
      return (String) paramName.toArray()[0];
    }
    return null;
  }

  private String getResourceIdFromPropertyValue(String filename, Object value,
                                                GlobalValidationContext globalContext) {
    Set<String> referenceValues = HeatStructureUtil.getReferencedValuesByFunctionName(filename,
        ResourceReferenceFunctions.GET_RESOURCE.getFunction(), value, globalContext);
    if (referenceValues != null && CollectionUtils.isNotEmpty(referenceValues)) {
      return (String) referenceValues.toArray()[0];
    }
    return null;
  }

  private boolean isPropertyValueGetResource(String filename, Object value,
                                             GlobalValidationContext globalContext) {
    Set<String> referenceValues = HeatStructureUtil.getReferencedValuesByFunctionName(filename,
        ResourceReferenceFunctions.GET_RESOURCE.getFunction(), value, globalContext);
    return referenceValues != null && (referenceValues.size() > 0);
  }

  private boolean isExpectedToBeExposed(String type) {
    return HeatResourcesTypes.isResourceExpectedToBeExposed(type);
  }

  private Set<String> validateManifest(ManifestContent manifestContent,
                                       GlobalValidationContext globalContext) {
    Set<String> baseFiles = ManifestUtil.getBaseFiles(manifestContent);
    if (baseFiles == null || baseFiles.size() == 0) {
      globalContext.addMessage(
          AsdcCommon.MANIFEST_NAME,
          ErrorLevel.WARNING,
          ErrorMessagesFormatBuilder
              .getErrorWithParameters(Messages.MISSIN_BASE_HEAT_FILE.getErrorMessage()));
    } else if (baseFiles.size() > 1) {
      String baseFileList = getElementListAsString(baseFiles);
      globalContext.addMessage(
          AsdcCommon.MANIFEST_NAME,
          ErrorLevel.WARNING,
          ErrorMessagesFormatBuilder
              .getErrorWithParameters(Messages.MULTI_BASE_HEAT_FILE.getErrorMessage(),
                  baseFileList));
    }
    return baseFiles;
  }

  private String getElementListAsString(Set<String> elementCollection) {

    return "[" + CommonMethods.collectionToCommaSeparatedString(elementCollection)  + "]";
  }


  private Environment validateEnvContent(String envFileName,
                                         GlobalValidationContext globalContext) {
    Environment envContent;
    try {
      envContent =
          new YamlUtil().yamlToObject(globalContext.getFileContent(envFileName), Environment.class);
    } catch (Exception exception) {
      return null;
    }
    return envContent;
  }

  private HeatOrchestrationTemplate checkHeatOrchestrationPreCondition(String fileName,
                                                                       GlobalValidationContext
                                                                           globalContext) {
    HeatOrchestrationTemplate heatOrchestrationTemplate;
    try {
      heatOrchestrationTemplate = new YamlUtil()
          .yamlToObject(globalContext.getFileContent(fileName), HeatOrchestrationTemplate.class);

    } catch (Exception exception) {
      return null;
    }
    return heatOrchestrationTemplate;
  }
}