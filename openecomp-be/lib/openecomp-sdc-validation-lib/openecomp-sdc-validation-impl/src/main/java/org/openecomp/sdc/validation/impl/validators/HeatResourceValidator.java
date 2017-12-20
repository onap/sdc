package org.openecomp.sdc.validation.impl.validators;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.validation.ErrorMessageCode;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Output;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.datatypes.model.ResourceReferenceFunctions;
import org.openecomp.sdc.heat.services.HeatStructureUtil;
import org.openecomp.sdc.heat.services.manifest.ManifestUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.validation.ValidationContext;
import org.openecomp.sdc.validation.base.ResourceBaseValidator;
import org.openecomp.sdc.validation.type.ConfigConstants;
import org.openecomp.sdc.validation.type.HeatResourceValidationContext;
import org.openecomp.sdc.validation.util.ValidationUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class HeatResourceValidator extends ResourceBaseValidator {
  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceBaseValidator.class);
  private static final ErrorMessageCode ERROR_CODE_HTR_1 = new ErrorMessageCode("HTR1");

  @Override
  public void init(Map<String, Object> properties) {
    super.init((Map<String, Object>) properties.get(ConfigConstants.Resource_Base_Validator));
  }

  @Override
  public ValidationContext createValidationContext(String fileName,
                                                   String envFileName,
                                                   HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                   GlobalValidationContext globalContext) {
    ManifestContent manifestContent = new ManifestContent();
    try {
      manifestContent = ValidationUtil.validateManifest(globalContext);
    } catch (Exception exception) {
      LOGGER.debug("", exception);
    }
    Set<String> baseFiles = ManifestUtil.getBaseFiles(manifestContent);
    String baseFileName = CollectionUtils.isEmpty(baseFiles) ? null : baseFiles.iterator().next();
    globalContext.setMessageCode(ERROR_CODE_HTR_1);
    HeatOrchestrationTemplate baseHot =
        ValidationUtil.checkHeatOrchestrationPreCondition(baseFileName, globalContext);
    Set<String> securityGroupsNamesFromBaseFileOutputs = baseFileName == null ? new HashSet<>()
        : checkForBaseFilePortsExistenceAndReturnSecurityGroupNamesFromOutputsIfNot
            (baseHot);

    Map<String, Resource> resourcesMap =
        heatOrchestrationTemplate.getResources() == null ? new HashMap<>()
            : heatOrchestrationTemplate.getResources();

    Map<String, Output> outputMap = heatOrchestrationTemplate.getOutputs() == null ? new HashMap<>()
        : heatOrchestrationTemplate.getOutputs();

    Map<String, Map<String, Map<String, List<String>>>>
        typeToPointingResourcesMap = new HashMap<>();

    initTypeRelationsMap
        (fileName, resourcesMap, outputMap,
            securityGroupsNamesFromBaseFileOutputs, typeToPointingResourcesMap, globalContext);

    return new HeatResourceValidationContext
        (heatOrchestrationTemplate, typeToPointingResourcesMap, envFileName);
  }

  private void initTypeRelationsMap(String fileName,
                                    Map<String, Resource> resourceMap,
                                    Map<String, Output> outputMap,
                                    Set<String> securityGroupsNamesFromBaseFileOutputs,
                                    Map<String, Map<String, Map<String, List<String>>>> typeToPointingResourcesMap,
                                    GlobalValidationContext globalContext) {

    initTypeRelationsMapFromResourcesMap
        (fileName, resourceMap,
            typeToPointingResourcesMap, globalContext);

    initTypeRelationsMapFromOutputsMap
        (fileName, resourceMap, outputMap,
            typeToPointingResourcesMap, globalContext);
  }

  private void initTypeRelationsMapFromOutputsMap(String fileName,
                                                  Map<String, Resource> resourceMap,
                                                  Map<String, Output> outputMap,
                                                  Map<String, Map<String, Map<String, List<String>>>> typeToPointingResourcesMap,
                                                  GlobalValidationContext globalContext) {
    for (Map.Entry<String, Output> outputEntry : outputMap.entrySet()) {
      Object outputValue = outputEntry.getValue().getValue();
      Set<String> referencedResources = HeatStructureUtil
          .getReferencedValuesByFunctionName(fileName,
              ResourceReferenceFunctions.GET_RESOURCE.getFunction(), outputValue, globalContext);

      updateRelationsMapWithOutputsReferences
          (outputEntry, resourceMap, referencedResources, typeToPointingResourcesMap);


    }
  }

  private void updateRelationsMapWithOutputsReferences(Map.Entry<String, Output> outputEntry,
                                                       Map<String, Resource> resourceMap,
                                                       Set<String> referencedResources,
                                                       Map<String, Map<String, Map<String, List<String>>>> typeToPointingResourcesMap) {

    for (String pointedResourceName : referencedResources) {
      Resource pointedResource = resourceMap.get(pointedResourceName);

      if (Objects.nonNull(pointedResource)) {
        initCurrentResourceTypeInMap(pointedResourceName, pointedResource.getType(),
            "output", typeToPointingResourcesMap);

        typeToPointingResourcesMap
            .get(pointedResource.getType()).get(pointedResourceName)
            .get("output").add(outputEntry.getKey());
      }
    }
  }

  private void initTypeRelationsMapFromResourcesMap(String fileName,
                                                    Map<String, Resource> resourceMap,
                                                    Map<String, Map<String, Map<String, List<String>>>> typeToPointingResourcesMap,
                                                    GlobalValidationContext globalContext) {
    for (Map.Entry<String, Resource> resourceEntry : resourceMap.entrySet()) {
      Resource pointingResource = resourceEntry.getValue();
      Map<String, Object> properties =
          pointingResource.getProperties() == null ? new HashMap<>()
              : pointingResource.getProperties();

      Set<String> referencedResourcesByGetResource =
          getResourcesIdsPointedByCurrentResource(fileName, ResourceReferenceFunctions.GET_RESOURCE,
              properties, globalContext);

      Set<String> referencedResourcesByGetAttr =
          handleGetAttrBetweenResources(properties);

      referencedResourcesByGetResource.addAll(referencedResourcesByGetAttr);

      updateRelationsMapWithCurrentResourceReferences
          (resourceMap, resourceEntry, referencedResourcesByGetResource,
              typeToPointingResourcesMap);
    }
  }

  private void updateRelationsMapWithSecurityGroupsFromBaseFileOutput(String fileName,
                                                                      Map<String, Resource> resourcesMap,
                                                                      Map.Entry<String, Resource> resourceEntry,
                                                                      Map<String, Object> properties,
                                                                      Set<String> securityGroupsNamesFromBaseFileOutputs,
                                                                      Map<String, Map<String, Map<String, List<String>>>> typeToPointingResourcesMap,
                                                                      GlobalValidationContext globalContext) {

    Set<String> candidateSecurityGroupUsedFromBaseFile = getResourcesIdsPointedByCurrentResource
        (fileName, ResourceReferenceFunctions.GET_PARAM, properties, globalContext);
    removeNonSecurityGroupNamesFromList
        (candidateSecurityGroupUsedFromBaseFile, securityGroupsNamesFromBaseFileOutputs);

    for (String usedSecurityGroupId : candidateSecurityGroupUsedFromBaseFile) {
      updateMapWithRelationsBetweenResources
          (usedSecurityGroupId,
              HeatResourcesTypes.NEUTRON_SECURITY_GROUP_RESOURCE_TYPE.getHeatResource(),
              resourceEntry, typeToPointingResourcesMap);

    }
  }

  private void removeNonSecurityGroupNamesFromList(
      Set<String> candidateSecurityGroupUsedFromBaseFile,
      Set<String> securityGroupsNamesFromBaseFileOutputs) {

    Set<String> nonSecurityGroupNames = new HashSet<>();
    for (String candidateSecurityGroup : candidateSecurityGroupUsedFromBaseFile) {
      if (!securityGroupsNamesFromBaseFileOutputs.contains(candidateSecurityGroup)) {
        nonSecurityGroupNames.add(candidateSecurityGroup);
      }
    }

    candidateSecurityGroupUsedFromBaseFile.removeAll(nonSecurityGroupNames);
  }

  private void updateRelationsMapWithCurrentResourceReferences(Map<String, Resource> resourceMap,
                                                               Map.Entry<String, Resource> currentResourceEntry,
                                                               Set<String> referencedResourcesFromCurrentResource,
                                                               Map<String, Map<String, Map<String, List<String>>>> typeToPointingResourcesMap) {

    for (String pointedResourceName : referencedResourcesFromCurrentResource) {
      Resource pointedResource = resourceMap.get(pointedResourceName);
      if (Objects.nonNull(pointedResource)) {
        String pointedResourceType = pointedResource.getType();

        updateMapWithRelationsBetweenResources
            (pointedResourceName, pointedResourceType,
                currentResourceEntry, typeToPointingResourcesMap);
      }
    }
  }

  private void updateMapWithRelationsBetweenResources(String pointedResourceName,
                                                      String pointedResourceType,
                                                      Map.Entry<String, Resource> currentResourceEntry,
                                                      Map<String, Map<String, Map<String, List<String>>>> typeToPointingResourcesMap) {

    initCurrentResourceTypeInMap(pointedResourceName, pointedResourceType,
        currentResourceEntry.getValue().getType(), typeToPointingResourcesMap);

    typeToPointingResourcesMap.get(pointedResourceType).get(pointedResourceName).get
        (currentResourceEntry.getValue().getType()).add(currentResourceEntry.getKey());
  }

  private void initCurrentResourceTypeInMap(String resourceName, String resourceType,
                                            String pointingResourceType,
                                            Map<String, Map<String, Map<String, List<String>>>> typeToPointingResourcesMap) {

    typeToPointingResourcesMap.putIfAbsent(resourceType, new HashMap<>());
    typeToPointingResourcesMap.get(resourceType).putIfAbsent(resourceName, new HashMap<>());
    typeToPointingResourcesMap.get(resourceType).get(resourceName).putIfAbsent
        (pointingResourceType, new ArrayList<>());
  }

  private Set<String> handleGetAttrBetweenResources(Map<String, Object> properties) {
    Set<String> referencedResourcesByGetAttr = new HashSet<>();
    for (Map.Entry<String, Object> proprtyEntry : properties.entrySet()) {
      referencedResourcesByGetAttr.addAll(getGetAttrReferencesInCaseOfContrail(proprtyEntry
          .getValue()));
    }

    return referencedResourcesByGetAttr;
  }


  private Set<String> getGetAttrReferencesInCaseOfContrail(Object propertyValue) {
    Object value;
    Set<String> getAttrReferences = new HashSet<>();

    if (propertyValue instanceof Map) {
      if (((Map) propertyValue).containsKey("get_attr")) {
        value = ((Map) propertyValue).get("get_attr");
        if (value instanceof List) {
          if (((List) value).size() == 2 && ((List) value).get(1).equals("fq_name")) {
            if (((List) value).get(0) instanceof String) {
              getAttrReferences.add((String) ((List) value).get(0));
              return getAttrReferences;
            } else {
              LOGGER.warn("invalid format of 'get_attr' function - " + propertyValue.toString());
            }
          }
        }
      } else {
        Collection<Object> valCollection = ((Map) propertyValue).values();
        for (Object entryValue : valCollection) {
          getAttrReferences.addAll(getGetAttrReferencesInCaseOfContrail(entryValue));
        }
      }
    } else if (propertyValue instanceof List) {
      for (Object prop : (List) propertyValue) {
        getAttrReferences.addAll(getGetAttrReferencesInCaseOfContrail(prop));
      }
    }

    return getAttrReferences;
  }


  private Set<String> getResourcesIdsPointedByCurrentResource(String fileName,
                                                              ResourceReferenceFunctions function,
                                                              Map<String, Object> properties,
                                                              GlobalValidationContext globalContext) {

    Set<String> referencedResources = new HashSet<>();
    for (Map.Entry<String, Object> propertyEntry : properties.entrySet()) {
      referencedResources
          .addAll(HeatStructureUtil
              .getReferencedValuesByFunctionName(fileName,
                  function.getFunction(),
                  propertyEntry.getValue(),
                  globalContext));
    }

    return referencedResources;
  }

  private Set<String> checkForBaseFilePortsExistenceAndReturnSecurityGroupNamesFromOutputsIfNot(
      HeatOrchestrationTemplate heatOrchestrationTemplate) {
    Set<String> securityGroupsNamesFromOutputsMap = new HashSet<>();

    if (heatOrchestrationTemplate != null) {
      Map<String, Resource> resourceMap = heatOrchestrationTemplate.getResources();
      if (!isPortResourceExistInBaseFile(resourceMap)) {
        getSecurityGroupsReferencedResourcesFromOutputs(securityGroupsNamesFromOutputsMap,
            heatOrchestrationTemplate.getOutputs(), resourceMap);
      }
    }
    return securityGroupsNamesFromOutputsMap;
  }

  private boolean isPortResourceExistInBaseFile(Map<String, Resource> resourceMap) {
    for (Map.Entry<String, Resource> resourceEntry : resourceMap.entrySet()) {
      if (resourceEntry.getValue().getType()
          .equals(HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource())) {
        return true;
      }
    }

    return false;
  }

  private void getSecurityGroupsReferencedResourcesFromOutputs(
      Set<String> securityGroupsNamesFromOutputsMap, Map<String, Output> outputMap,
      Map<String, Resource> resourceMap) {

    if (MapUtils.isNotEmpty(outputMap)) {
      for (Map.Entry<String, Output> outputEntry : outputMap.entrySet()) {
        Object outputValue = outputEntry.getValue().getValue();
        if (Objects.nonNull(outputValue) && outputValue instanceof Map) {
          String resourceName = (String) ((Map) outputValue)
              .get(ResourceReferenceFunctions.GET_RESOURCE.getFunction());
          if (Objects.nonNull(resourceName)) {
            Resource resource = resourceMap.get(resourceName);
            if (Objects.nonNull(resource) && resource.getType().equals(
                HeatResourcesTypes.NEUTRON_SECURITY_GROUP_RESOURCE_TYPE.getHeatResource())) {
              securityGroupsNamesFromOutputsMap.add(outputEntry.getKey());
            }
          }
        }
      }
    }
  }
}
