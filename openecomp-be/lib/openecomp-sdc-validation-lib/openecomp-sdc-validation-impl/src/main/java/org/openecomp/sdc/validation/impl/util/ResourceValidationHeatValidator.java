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

package org.openecomp.sdc.validation.impl.util;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.errors.Messages;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Output;
import org.openecomp.sdc.heat.datatypes.model.PolicyTypes;
import org.openecomp.sdc.heat.datatypes.model.PropertiesMapKeyTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.datatypes.model.ResourceReferenceFunctions;
import org.openecomp.sdc.heat.datatypes.model.ResourceTypeToMessageString;
import org.openecomp.sdc.heat.services.HeatStructureUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class ResourceValidationHeatValidator {

  /**
   * Validate resource type.
   *
   * @param fileName                               the file name
   * @param baseFileName                           the base file name
   * @param securityGroupsNamesFromBaseFileOutputs the security groups names from base file outputs
   * @param heatOrchestrationTemplate              the heat orchestration template
   * @param globalContext                          the global context
   */
  public static void validateResourceType(String fileName, String baseFileName,
                                          Set<String> securityGroupsNamesFromBaseFileOutputs,
                                          HeatOrchestrationTemplate heatOrchestrationTemplate,
                                          GlobalValidationContext globalContext) {
    Map<String, Resource> resourceMap =
        heatOrchestrationTemplate.getResources() == null ? new HashMap<>()
            : heatOrchestrationTemplate.getResources();
    Map<String, Integer> numberOfVisitsInPort = new HashMap<>();
    Set<String> resourcesNames = resourceMap.keySet();
    Set<String> sharedResourcesFromOutputMap =
        getSharedResourcesNamesFromOutputs(fileName, heatOrchestrationTemplate.getOutputs(),
            globalContext);
    boolean isBaseFile = baseFileName != null && fileName.equals(baseFileName);

    Map<HeatResourcesTypes, List<String>> resourceTypeToNamesListMap = HeatResourcesTypes
        .getListForResourceType(HeatResourcesTypes.NOVA_SERVER_GROUP_RESOURCE_TYPE,
            HeatResourcesTypes.NEUTRON_SECURITY_GROUP_RESOURCE_TYPE,
            HeatResourcesTypes.CONTRAIL_NETWORK_RULE_RESOURCE_TYPE);

    initResourceTypeListWithItsResourcesNames(fileName, resourceTypeToNamesListMap, resourceMap,
        sharedResourcesFromOutputMap, globalContext);
    initVisitedPortsMap(fileName, resourceMap, numberOfVisitsInPort, globalContext);


    for (Map.Entry<String, Resource> resourceEntry : resourceMap.entrySet()) {
      String resourceType = resourceEntry.getValue().getType();
      validateSecurityGroupsFromBaseOutput(fileName, resourceEntry, isBaseFile,
          securityGroupsNamesFromBaseFileOutputs, globalContext);
      checkResourceDependsOn(fileName, resourceEntry.getValue(), resourcesNames, globalContext);

      if (Objects.isNull(resourceType)) {
        globalContext.addMessage(fileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
            .getErrorWithParameters(Messages.INVALID_RESOURCE_TYPE.getErrorMessage(), "null",
                resourceEntry.getKey()));
      } else {
        HeatResourcesTypes heatResourceType = HeatResourcesTypes.findByHeatResource(resourceType);

        if (heatResourceType != null) {
          switch (heatResourceType) {
            case NOVA_SERVER_RESOURCE_TYPE:
              validateNovaServerResourceType(fileName, resourceEntry, numberOfVisitsInPort,
                  resourceTypeToNamesListMap
                      .get(HeatResourcesTypes.NOVA_SERVER_GROUP_RESOURCE_TYPE),
                  heatOrchestrationTemplate, globalContext);
              break;

            case NOVA_SERVER_GROUP_RESOURCE_TYPE:
              validateNovaServerGroupPolicy(fileName, resourceEntry, globalContext);
              break;

            case RESOURCE_GROUP_RESOURCE_TYPE:
              validateResourceGroupType(fileName, resourceEntry, globalContext);
              break;

            case NEUTRON_PORT_RESOURCE_TYPE:
              validateNeutronPortType(fileName, resourceEntry, resourceTypeToNamesListMap
                  .get(HeatResourcesTypes.NEUTRON_SECURITY_GROUP_RESOURCE_TYPE), globalContext);
              break;

            case CONTRAIL_NETWORK_ATTACH_RULE_RESOURCE_TYPE:
              validateContrailAttachPolicyType(resourceEntry, resourceTypeToNamesListMap
                  .get(HeatResourcesTypes.CONTRAIL_NETWORK_RULE_RESOURCE_TYPE));
              break;
            default:
          }
        } else {
          if (HeatValidationService.isNestedResource(resourceType)) {
            handleNestedResourceType(fileName, resourceEntry.getKey(), resourceEntry.getValue(),
                globalContext);
          }
        }
      }
    }

    checkForEmptyResourceNamesInMap(fileName,
        CollectionUtils.isEmpty(securityGroupsNamesFromBaseFileOutputs), resourceTypeToNamesListMap,
        globalContext);
    handleOrphanPorts(fileName, numberOfVisitsInPort, globalContext);
  }


  private static void validateNovaServerResourceType(String fileName,
                                                     Map.Entry<String, Resource> resourceEntry,
                                                     Map<String, Integer> numberOfVisitsInPort,
                                                     List<String> serverGroupResourcesNames,
                                                     HeatOrchestrationTemplate
                                                         heatOrchestrationTemplate,
                                                     GlobalValidationContext globalContext) {
    validateAssignedValueForImageOrFlavorFromNova(fileName, resourceEntry, globalContext);
    validateNovaServerPortBinding(fileName, resourceEntry.getValue(), numberOfVisitsInPort,
        globalContext);
    validateAllServerGroupsPointedByServerExistAndDefined(fileName, resourceEntry,
        serverGroupResourcesNames, heatOrchestrationTemplate, globalContext);

  }


  private static void handleNestedResourceType(String fileName, String resourceName,
                                               Resource resource,
                                               GlobalValidationContext globalContext) {
    validateAllPropertiesMatchNestedParameters(fileName, resourceName, resource, globalContext);
    validateLoopsOfNestingFromFile(fileName, resource.getType(), globalContext);
  }


  private static void validateResourceGroupType(String fileName,
                                                Map.Entry<String, Resource> resourceEntry,
                                                GlobalValidationContext globalContext) {
    Resource resourceDef = HeatStructureUtil
        .getResourceDef(fileName, resourceEntry.getKey(), resourceEntry.getValue(), globalContext);
    // validateResourceGroupTypeIsSupported(fileName, resourceEntry.getKey(),resourceDef.getType(),
    // globalContext);
    if (resourceDef != null) {
      if (Objects.nonNull(resourceDef.getType())
          && HeatValidationService.isNestedResource(resourceDef.getType())) {
        handleNestedResourceType(fileName, resourceDef.getType(), resourceDef, globalContext);
      }
    }
  }


  private static void validateAllPropertiesMatchNestedParameters(String fileName,
                                                                 String resourceName,
                                                                 Resource resource,
                                                                 GlobalValidationContext
                                                                     globalContext) {

    String resourceType = resource.getType();
    if (globalContext.getFileContextMap().containsKey(resourceType)) {
      Set<String> propertiesNames =
          resource.getProperties() == null ? null : resource.getProperties().keySet();
      if (CollectionUtils.isNotEmpty(propertiesNames)) {
        HeatValidationService
            .checkNestedParameters(fileName, resourceType, resourceName, globalContext,
                propertiesNames);
      }
    } else {
      globalContext.addMessage(resourceType, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
          .getErrorWithParameters(Messages.MISSING_NESTED_FILE.getErrorMessage(), resourceType));
    }
  }


  private static void validateAssignedValueForImageOrFlavorFromNova(String fileName,
                                                                    Map.Entry<String, Resource>
                                                                        resourceEntry,
                                                                    GlobalValidationContext
                                                                        globalContext) {

    Resource resource = resourceEntry.getValue();
    Map<String, Object> propertiesMap = resource.getProperties();
    if (propertiesMap.get(PropertiesMapKeyTypes.IMAGE.getKeyMap()) == null
        && propertiesMap.get(PropertiesMapKeyTypes.FLAVOR.getKeyMap()) == null) {
      globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
          .getErrorWithParameters(Messages.MISSING_IMAGE_AND_FLAVOR.getErrorMessage(),
              resourceEntry.getKey()));
    }
  }


  private static void validateLoopsOfNestingFromFile(String fileName, String resourceType,
                                                     GlobalValidationContext globalContext) {
    List<String> filesInLoop = new ArrayList<>(Collections.singletonList(fileName));
    if (HeatValidationService
        .isNestedLoopExistInFile(fileName, resourceType, filesInLoop, globalContext)) {
      globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
          .getErrorWithParameters(Messages.NESTED_LOOP.getErrorMessage(),
              HeatValidationService.drawFilesLoop(filesInLoop)));
    }
  }


  /* validation 22*/
  @SuppressWarnings("unchecked")
  private static void validateNovaServerPortBinding(String fileName, Resource resource,
                                                    Map<String, Integer> numberOfVisitsInPort,
                                                    GlobalValidationContext globalContext) {

    Map<String, Object> propertiesMap = resource.getProperties();
    List<Object> networksList =
        (List<Object>) propertiesMap.get(PropertiesMapKeyTypes.NETWORKS.getKeyMap());

    if (CollectionUtils.isNotEmpty(networksList)) {
      networksList
          .stream()
          .filter(networkObject -> networkObject instanceof Map)
          .forEach(networkObject -> {
            Map<String, Object> portValueMap =
                (Map<String, Object>) ((Map) networkObject).get("port");
            if (MapUtils.isNotEmpty(portValueMap)) {
              checkPortBindingFromMap(fileName, portValueMap, numberOfVisitsInPort, globalContext);
            }
          });
    }
  }

  /* validation 23*/
  @SuppressWarnings("unchecked")
  private static void validateAllServerGroupsPointedByServerExistAndDefined(String fileName,
                                              Map.Entry<String, Resource> resourceEntry,
                                              List<String> serverGroupNamesList,
                                              HeatOrchestrationTemplate heatOrchestrationTemplate,
                                              GlobalValidationContext globalContext) {
    Map<String, Resource> resourcesMap = heatOrchestrationTemplate.getResources();

    Map<String, Object> resourceProperties = resourceEntry.getValue().getProperties();
    Map<String, Object> schedulerHintsMap = resourceProperties == null ? null
        : (Map<String, Object>) resourceProperties
            .get(ResourceReferenceFunctions.SCHEDULER_HINTS.getFunction());

    if (MapUtils.isNotEmpty(schedulerHintsMap)) {
      for (Object serverGroupMap : schedulerHintsMap.values()) {
        Map<String, Object> currentServerMap = (Map<String, Object>) serverGroupMap;
        String serverResourceName = currentServerMap == null ? null
            : (String) currentServerMap.get(ResourceReferenceFunctions.GET_RESOURCE.getFunction());
        Resource serverResource = serverResourceName == null || resourcesMap == null ? null
            : resourcesMap.get(serverResourceName);
        if (serverResource != null && !serverResource.getType()
            .equals(HeatResourcesTypes.NOVA_SERVER_GROUP_RESOURCE_TYPE.getHeatResource())) {
          globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
              .getErrorWithParameters(Messages.SERVER_NOT_DEFINED_FROM_NOVA.getErrorMessage(),
                  serverResourceName, resourceEntry.getKey()));
        } else {
          serverGroupNamesList.remove(serverResourceName);
        }
      }
    }
  }


  /* validation 24*/
  @SuppressWarnings("unchecked")
  private static void validateNovaServerGroupPolicy(String fileName,
                                                    Map.Entry<String, Resource> resourceEntry,
                                                    GlobalValidationContext globalContext) {

    Resource resource = resourceEntry.getValue();
    List<String> policiesList = resource.getProperties() == null ? null
        : (List<String>) resource.getProperties().get("policies");

    if (CollectionUtils.isNotEmpty(policiesList)) {
      if (policiesList.size() == 1) {
        String policy = policiesList.get(0);
        if (!PolicyTypes.isGivenPolicyValid(policy)) {
          globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
              .getErrorWithParameters(Messages.WRONG_POLICY_IN_SERVER_GROUP.getErrorMessage(),
                  resourceEntry.getKey()));
        }
      } else {
        globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
            .getErrorWithParameters(Messages.WRONG_POLICY_IN_SERVER_GROUP.getErrorMessage(),
                resourceEntry.getKey()));
      }
    }
  }


  private static void validateNeutronPortType(String filename,
                                              Map.Entry<String, Resource> resourceEntry,
                                              List<String> securityGroupResourceNameList,
                                              GlobalValidationContext globalContext) {
    validateAllSecurityGroupsAreUsed(filename, resourceEntry, securityGroupResourceNameList,
        globalContext);

  }


  @SuppressWarnings("unchecked")
  private static void validateAllSecurityGroupsAreUsed(String filename,
                                                       Map.Entry<String, Resource> resourceEntry,
                                                       List<String> securityGroupResourceNameList,
                                                       GlobalValidationContext globalContext) {
    Map<String, Object> propertiesMap = resourceEntry.getValue().getProperties();

    if (MapUtils.isEmpty(propertiesMap)) {
      return;
    }

    Object securityGroupsValue = propertiesMap.get("security_groups");

    if (Objects.isNull(securityGroupsValue)) {
      return;
    }

    if (securityGroupsValue instanceof List) {
      List<Object> securityGroupsListFromCurrResource =
          (List<Object>) propertiesMap.get("security_groups");
      for (Object securityGroup : securityGroupsListFromCurrResource) {
        removeSecurityGroupNamesFromListByGivenFunction(filename,
            ResourceReferenceFunctions.GET_RESOURCE.getFunction(), securityGroup,
            securityGroupResourceNameList, globalContext);
      }
    }
  }


  private static void validateSecurityGroupsFromBaseOutput(String filename,
                                                  Map.Entry<String, Resource> resourceEntry,
                                                  boolean isBaseFile,
                                                  Set<String> securityGroupNamesFromBaseOutput,
                                                  GlobalValidationContext globalContext) {
    if (!isBaseFile && CollectionUtils.isNotEmpty(securityGroupNamesFromBaseOutput)) {
      Map<String, Object> propertiesMap = resourceEntry.getValue().getProperties();

      if (MapUtils.isEmpty(propertiesMap)) {
        return;
      }

      for (Map.Entry<String, Object> propertyEntry : propertiesMap.entrySet()) {
        removeSecurityGroupNamesFromListByGivenFunction(filename,
            ResourceReferenceFunctions.GET_PARAM.getFunction(), propertyEntry.getValue(),
            securityGroupNamesFromBaseOutput, globalContext);
      }
    }
  }


  private static void removeSecurityGroupNamesFromListByGivenFunction(String filename,
                                                                      String functionName,
                                                                      Object securityGroup,
                                            Collection<String> securityGroupResourceNameList,
                                            GlobalValidationContext globalContext) {
    Set<String> securityGroupsNamesFromFunction = HeatStructureUtil
        .getReferencedValuesByFunctionName(filename, functionName, securityGroup, globalContext);
    securityGroupsNamesFromFunction.forEach(securityGroupResourceNameList::remove);
  }


  @SuppressWarnings("unchecked")
  private static void validateContrailAttachPolicyType(Map.Entry<String, Resource> resourceEntry,
                                                       List<String> networkPolicyResourceNames) {
    Map<String, Object> propertiesMap = resourceEntry.getValue().getProperties();

    if (MapUtils.isNotEmpty(propertiesMap)) {
      Map<String, Object> policyMap = (Map<String, Object>) propertiesMap.get("policy");
      if (MapUtils.isNotEmpty(policyMap)) {
        List<Object> securityGroupList =
            (List<Object>) policyMap.get(ResourceReferenceFunctions.GET_ATTR.getFunction());
        //noinspection SuspiciousMethodCalls
        if (CollectionUtils.isNotEmpty(securityGroupList)) {
          //noinspection SuspiciousMethodCalls
          networkPolicyResourceNames.remove(securityGroupList.get(0));
        }
      }
    }
  }


  private static void getResourceNamesListFromSpecificResource(String filename,
                                                               List<String> resourcesNames,
                                                               HeatResourcesTypes heatResourcesType,
                                                               Map<String, Resource> resourcesMap,
                                                       Set<String> sharedResourcesFromOutputMap,
                                                       GlobalValidationContext globalContext) {

    for (Map.Entry<String, Resource> resourceEntry : resourcesMap.entrySet()) {
      String resourceType = resourceEntry.getValue().getType();
      if (Objects.isNull(resourceType)) {
        globalContext.addMessage(filename, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
            .getErrorWithParameters(Messages.INVALID_RESOURCE_TYPE.getErrorMessage(), null,
                resourceEntry.getKey()));
      } else {
        if (resourceType.equals(heatResourcesType.getHeatResource())
            && !isSharedResource(resourceEntry.getKey(), sharedResourcesFromOutputMap)) {
          resourcesNames.add(resourceEntry.getKey());
        }
      }
    }
  }


  private static boolean isSharedResource(String resourceName,
                                          Set<String> sharedResourcesFromOutputMap) {
    return !CollectionUtils.isEmpty(sharedResourcesFromOutputMap)
        && sharedResourcesFromOutputMap.contains(resourceName);
  }

  /**
   * Handle not empty resource names list.
   *
   * @param fileName              the file name
   * @param resourcesNameList     the resources name list
   * @param securityOrServerGroup the security or server group
   * @param globalContext         the global context
   */
  public static void handleNotEmptyResourceNamesList(String fileName,
                                                     Collection<String> resourcesNameList,
                                                     String securityOrServerGroup,
                                                     GlobalValidationContext globalContext) {
    if (CollectionUtils.isNotEmpty(resourcesNameList)) {
      resourcesNameList.forEach(name ->
          globalContext
              .addMessage(
                  fileName,
                  ErrorLevel.WARNING,
                  ErrorMessagesFormatBuilder
                      .getErrorWithParameters(
                          Messages.SERVER_OR_SECURITY_GROUP_NOT_IN_USE.getErrorMessage(),
                          securityOrServerGroup, name)));
    }
  }


  private static void initVisitedPortsMap(String filename, Map<String, Resource> resourceMap,
                                          Map<String, Integer> numberOfVisitsInPort,
                                          GlobalValidationContext globalContext) {
    for (Map.Entry<String, Resource> resourceEntry : resourceMap.entrySet()) {
      String resourceType = resourceEntry.getValue().getType();

      if (Objects.isNull(resourceType)) {
        globalContext.addMessage(filename, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
            .getErrorWithParameters(Messages.INVALID_RESOURCE_TYPE.getErrorMessage(), "null",
                resourceEntry.getKey()));
      } else {
        if (resourceType.equals(HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource())) {
          numberOfVisitsInPort.put(resourceEntry.getKey(), 0);
        }
      }
    }
  }

  private static boolean checkIfPortWasVisited(String resourcePortName,
                                               Map<String, Integer> numberOfVisitsInPort) {
    return numberOfVisitsInPort.containsKey(resourcePortName)
        && numberOfVisitsInPort.get(resourcePortName) == 1;
  }


  private static void incrementNumberOfVisitsInPort(String resourcePortName,
                                                    Map<String, Integer> numberOfVisitsInPort) {
    if (numberOfVisitsInPort.containsKey(resourcePortName)) {
      numberOfVisitsInPort.put(resourcePortName, numberOfVisitsInPort.get(resourcePortName) + 1);
    }
  }


  private static void handleOrphanPorts(String fileName, Map<String, Integer> numberOfVisitsInPort,
                                        GlobalValidationContext globalContext) {
    numberOfVisitsInPort
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue() == 0)
        .forEach(entry ->
            globalContext
                .addMessage(
                    fileName,
                    ErrorLevel.WARNING,
                    ErrorMessagesFormatBuilder
                        .getErrorWithParameters(
                            Messages.PORT_NO_BIND_TO_ANY_NOVA_SERVER.getErrorMessage(),
                            entry.getKey())));
  }

  @SuppressWarnings("unchecked")
  private static void checkResourceDependsOn(String fileName, Resource resource,
                                             Set<String> resourcesNames,
                                             GlobalValidationContext globalContext) {
    Object dependencies = resource.getDepends_on();
    if (dependencies instanceof Collection) {
      ((Collection<String>) dependencies)
          .stream()
          .filter(resource_id -> !resourcesNames.contains(resource_id))
          .forEach(resource_id -> globalContext.addMessage(fileName, ErrorLevel.ERROR,
              ErrorMessagesFormatBuilder
                  .getErrorWithParameters(Messages.MISSING_RESOURCE_IN_DEPENDS_ON.getErrorMessage(),
                      (String) resource_id)));
    } else if (dependencies instanceof String) {
      if (!resourcesNames.contains(dependencies)) {
        globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
            .getErrorWithParameters(Messages.MISSING_RESOURCE_IN_DEPENDS_ON.getErrorMessage(),
                (String) dependencies));
      }
    }
  }


  private static void checkPortBindingFromMap(String fileName, Map<String, Object> portValueMap,
                                              Map<String, Integer> numberOfVisitsInPort,
                                              GlobalValidationContext globalContext) {
    String resourcePortName =
        (String) portValueMap.get(ResourceReferenceFunctions.GET_RESOURCE.getFunction());
    if (checkIfPortWasVisited(resourcePortName, numberOfVisitsInPort)) {
      globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
          .getErrorWithParameters(Messages.MORE_THAN_ONE_BIND_FROM_NOVA_TO_PORT.getErrorMessage(),
              (String) portValueMap.get(ResourceReferenceFunctions.GET_RESOURCE.getFunction())));
    } else {
      incrementNumberOfVisitsInPort(resourcePortName, numberOfVisitsInPort);
    }
  }


  private static void initResourceTypeListWithItsResourcesNames(String filename,
                                       Map<HeatResourcesTypes, List<String>> resourcesTypesListMap,
                                       Map<String, Resource> resourcesMap,
                                       Set<String> sharedResourcesFromOutputsMap,
                                       GlobalValidationContext globalContext) {
    for (Map.Entry<HeatResourcesTypes, List<String>> resourcesTypesToListEntry
        : resourcesTypesListMap.entrySet()) {
      HeatResourcesTypes currentType = resourcesTypesToListEntry.getKey();
      List<String> currNamesList = new ArrayList<>();
      getResourceNamesListFromSpecificResource(filename, currNamesList, currentType, resourcesMap,
          sharedResourcesFromOutputsMap, globalContext);
      resourcesTypesListMap.put(currentType, currNamesList);
    }
  }


  private static void checkForEmptyResourceNamesInMap(String fileName,
                                        boolean isBaseFileContainPorts,
                                        Map<HeatResourcesTypes, List<String>> resourcesTypesListMap,
                                        GlobalValidationContext globalContext) {
    if (isBaseFileContainPorts) {
      for (Map.Entry<HeatResourcesTypes, List<String>> resourcesTypesListEntry
          : resourcesTypesListMap.entrySet()) {
        handleNotEmptyResourceNamesList(fileName, resourcesTypesListEntry.getValue(),
            ResourceTypeToMessageString
                .getTypeForMessageFromResourceType(resourcesTypesListEntry.getKey()),
            globalContext);
      }
    }
  }


  private static Set<String> getSharedResourcesNamesFromOutputs(String filename,
                                                  Map<String, Output> outputsMap,
                                                  GlobalValidationContext globalContext) {
    Set<String> sharedResources = new HashSet<>();

    if (MapUtils.isEmpty(outputsMap)) {
      return null;
    }

    for (Map.Entry<String, Output> outputEntry : outputsMap.entrySet()) {
      Output output = outputEntry.getValue();
      Object valueObject = output.getValue();
      if (valueObject instanceof Map) {
        Map<String, Object> outputValueMap = (Map<String, Object>) valueObject;
        Object getResourceValue =
            outputValueMap.get(ResourceReferenceFunctions.GET_RESOURCE.getFunction());
        if (Objects.nonNull(getResourceValue)) {
          if (getResourceValue instanceof String) {
            String resourceName =
                (String) outputValueMap.get(ResourceReferenceFunctions.GET_RESOURCE.getFunction());
            sharedResources.add(resourceName);
          } else {
            globalContext.addMessage(filename, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                .getErrorWithParameters(Messages.INVALID_GET_RESOURCE_SYNTAX.getErrorMessage(),
                    getResourceValue.toString()));
          }
        }

      }
    }

    return sharedResources;
  }
}
