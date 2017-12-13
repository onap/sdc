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

package org.openecomp.sdc.validation.impl.validators;

import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Output;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.datatypes.model.ResourceReferenceFunctions;
import org.openecomp.sdc.heat.services.HeatStructureUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.validation.ValidationContext;
import org.openecomp.sdc.validation.base.ResourceBaseValidator;
import org.openecomp.sdc.validation.type.ConfigConstants;
import org.openecomp.sdc.validation.type.HeatResourceValidationContext;

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

  @Override
  public void init(Map<String, Object> properties) {
    super.init((Map<String, Object>) properties.get(ConfigConstants.Resource_Base_Validator));
  }

  @Override
  public ValidationContext createValidationContext(String fileName,
                                                      String envFileName,
                                                      HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                      GlobalValidationContext globalContext) {
    Map<String, Resource> resourcesMap =
        heatOrchestrationTemplate.getResources() == null ? new HashMap<>()
            : heatOrchestrationTemplate.getResources();

    Map<String, Output> outputMap = heatOrchestrationTemplate.getOutputs() == null ? new HashMap<>()
        : heatOrchestrationTemplate.getOutputs();

    Map<String, Map<String, Map<String, List<String>>>>
        typeToPointingResourcesMap = new HashMap<>();

    initTypeRelationsMap (fileName, resourcesMap, outputMap,
             typeToPointingResourcesMap, globalContext);

    return new HeatResourceValidationContext (heatOrchestrationTemplate, typeToPointingResourcesMap,
            envFileName );
  }

  private void initTypeRelationsMap (String fileName,
                     Map<String, Resource> resourceMap,
                     Map<String, Output> outputMap,
                     Map<String, Map<String, Map<String, List<String>>>> typeToPointingResourcesMap,
                     GlobalValidationContext globalContext ) {
    initTypeRelationsMapFromResourcesMap (fileName, resourceMap,
            typeToPointingResourcesMap, globalContext);

    initTypeRelationsMapFromOutputsMap (fileName, resourceMap, outputMap,
            typeToPointingResourcesMap, globalContext);
  }

  private void initTypeRelationsMapFromOutputsMap (String fileName,
                                                  Map<String, Resource> resourceMap,
                                                  Map<String, Output> outputMap,
                                                  Map<String, Map<String, Map<String, List<String>>>> typeToPointingResourcesMap,
                                                  GlobalValidationContext globalContext ) {
    for (Map.Entry<String, Output> outputEntry : outputMap.entrySet()) {
      Object outputValue = outputEntry.getValue().getValue();
      Set<String> referencedResources = HeatStructureUtil
          .getReferencedValuesByFunctionName(fileName,
              ResourceReferenceFunctions.GET_RESOURCE.getFunction(), outputValue, globalContext);

      updateRelationsMapWithOutputsReferences (outputEntry, resourceMap, referencedResources, typeToPointingResourcesMap);
    }
  }

  private void updateRelationsMapWithOutputsReferences (Map.Entry<String, Output> outputEntry,
                                                       Map<String, Resource> resourceMap,
                                                       Set<String> referencedResources,
                                                       Map<String, Map<String, Map<String, List<String>>>> typeToPointingResourcesMap ) {

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
          pointingResource.getProperties() == null ? new HashMap<>() : pointingResource.getProperties();

      Set<String> referencedResourcesByGetResource =
          getResourcesIdsPointedByCurrentResource(fileName, ResourceReferenceFunctions.GET_RESOURCE,
              properties, globalContext);

      Set<String> referencedResourcesByGetAttr =
          handleGetAttrBetweenResources(properties);

      referencedResourcesByGetResource.addAll(referencedResourcesByGetAttr);

      updateRelationsMapWithCurrentResourceReferences
          (resourceMap, resourceEntry, referencedResourcesByGetResource, typeToPointingResourcesMap);
    }
  }

  private void updateRelationsMapWithCurrentResourceReferences(Map<String, Resource> resourceMap,
                                                               Map.Entry<String, Resource> currentResourceEntry,
                                                               Set<String> referencedResourcesFromCurrentResource,
                                                               Map<String, Map<String, Map<String, List<String>>>> typeToPointingResourcesMap) {

    for (String pointedResourceName : referencedResourcesFromCurrentResource) {
      Resource pointedResource = resourceMap.get(pointedResourceName);
      if (Objects.nonNull(pointedResource)) {
        String pointedResourceType = pointedResource.getType();

        updateMapWithRelationsBetweenResources (pointedResourceName, pointedResourceType,
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

    typeToPointingResourcesMap.get(pointedResourceType).get(pointedResourceName)
            .get(currentResourceEntry.getValue().getType()).add(currentResourceEntry.getKey());
  }

  private void initCurrentResourceTypeInMap(String resourceName, String resourceType,
                                            String pointingResourceType,
                                            Map<String, Map<String, Map<String, List<String>>>> typeToPointingResourcesMap) {

    typeToPointingResourcesMap.putIfAbsent(resourceType, new HashMap<>());
    typeToPointingResourcesMap.get(resourceType).putIfAbsent(resourceName, new HashMap<>());
    typeToPointingResourcesMap.get(resourceType).get(resourceName)
            .putIfAbsent (pointingResourceType, new ArrayList<>());
  }

  private Set<String> handleGetAttrBetweenResources (Map<String, Object> properties) {
    Set<String> referencedResourcesByGetAttr = new HashSet<>();
    for (Map.Entry<String, Object> proprtyEntry : properties.entrySet()) {
      referencedResourcesByGetAttr.addAll(getGetAttrReferencesInCaseOfContrail(proprtyEntry
          .getValue()));
    }

    return referencedResourcesByGetAttr;
  }


  private Set<String> getGetAttrReferencesInCaseOfContrail(Object propertyValue) {
    Set<String> getAttrReferences = new HashSet<>();

    if (propertyValue instanceof Map) {
      if (((Map) propertyValue).containsKey("get_attr")) {
        if (validateAndAddAttrReferences(propertyValue, getAttrReferences)) {
          return getAttrReferences;
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

  private boolean validateAndAddAttrReferences(Object propertyValue, Set<String> getAttrReferences) {
    Object value;
    value = ((Map) propertyValue).get("get_attr");
    if (value instanceof List && ((List) value).size() == 2
            && ("fq_name").equals(((List) value).get(1))) {
      if (((List) value).get(0) instanceof String) {
        getAttrReferences.add((String) ((List) value).get(0));
        return true;
      } else {
        LOGGER.warn("invalid format of 'get_attr' function - " + propertyValue.toString());
      }
  }
    return false;
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
}
