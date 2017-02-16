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

package org.openecomp.sdc.translator.services.heattotosca.mapping;

import org.openecomp.core.utilities.yaml.YamlUtil;
import org.openecomp.sdc.heat.datatypes.HeatBoolean;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.tosca.datatypes.ToscaArtifactType;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;
import org.openecomp.sdc.tosca.datatypes.model.ArtifactDefinition;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.Template;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.ToscaFileOutputService;
import org.openecomp.sdc.tosca.services.impl.ToscaFileOutputServiceCsarImpl;
import org.openecomp.sdc.translator.services.heattotosca.Constants;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.services.heattotosca.impl.ResourceTranslationBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


public class TranslatorHeatToToscaFunctionConverter {
  private static final String UNSUPPORTED_RESOURCE = "UNSUPPORTED_RESOURCE_";
  private static final String UNSUPPORTED_ATTRIBUTE = "UNSUPPORTED_ATTRIBUTE_";

  protected static Set<String> functionNameSet;

  static {
    functionNameSet = new HashSet<>();
    functionNameSet.add("get_param");
    functionNameSet.add("get_attr");
    functionNameSet.add("get_resource");
    functionNameSet.add("get_file");
  }

  /**
   * Gets tosca function.
   *
   * @param functionName              the function name
   * @param function                  the function
   * @param heatFileName              the heat file name
   * @param heatOrchestrationTemplate the heat orchestration template
   * @param template                  the template
   * @param context                   the context
   * @return the tosca function
   */
  public static Object getToscaFunction(String functionName, Object function, String heatFileName,
                                        HeatOrchestrationTemplate heatOrchestrationTemplate,
                                        Template template, TranslationContext context) {
    Object returnValue = null;
    if ("get_param".equals(functionName)) {
      returnValue =
          handleGetParamFunction(function, heatFileName, heatOrchestrationTemplate, context);
    } else if ("get_attr".equals(functionName)) {
      returnValue =
          handleGetAttrFunction(function, heatFileName, heatOrchestrationTemplate, context);
    } else if ("get_resource".equals(functionName)) {
      returnValue =
          handleGetResourceFunction(function, heatFileName, heatOrchestrationTemplate, context);
    } else if ("get_file".equals(functionName)) {
      returnValue = handleGetFileFunction(function, template);
    }
    return returnValue;
  }

  private static Object handleGetFileFunction(Object function, Template template) {
    String file = ((String) function).replace("file:///", "");
    Object returnValue;
    final String artifactId = file.split("\\.")[0];

    returnValue = new HashMap<>();
    List artifactParameters = new ArrayList();
    artifactParameters.add(0, ToscaConstants.MODELABLE_ENTITY_NAME_SELF);
    ((Map) returnValue).put(ToscaFunctions.GET_ARTIFACT.getDisplayName(), artifactParameters);
    artifactParameters.add(1, artifactId);

    ToscaFileOutputService toscaFileOutputService = new ToscaFileOutputServiceCsarImpl();
    if (template != null) {
      if (template instanceof NodeTemplate) {
        NodeTemplate nodeTemplate = (NodeTemplate) template;
        ArtifactDefinition artifactDefinition =
            createArtifactDefinition(file, toscaFileOutputService);
        if (nodeTemplate.getArtifacts() == null) {
          nodeTemplate.setArtifacts(new HashMap<>());
        }
        nodeTemplate.getArtifacts().put(artifactId, artifactDefinition);
      }
    }
    return returnValue;
  }

  private static Object handleGetResourceFunction(Object function, String heatFileName,
                                                  HeatOrchestrationTemplate
                                                          heatOrchestrationTemplate,
                                                  TranslationContext context) {
    Object returnValue;
    Optional<String> resourceTranslatedId = ResourceTranslationBase
        .getResourceTranslatedId(heatFileName, heatOrchestrationTemplate, (String) function,
            context);
    if (resourceTranslatedId.isPresent()) {
      returnValue = resourceTranslatedId.get();
    } else {
      returnValue = UNSUPPORTED_RESOURCE + function;
    }
    return returnValue;
  }

  private static Object handleGetAttrFunction(Object function, String heatFileName,
                                              HeatOrchestrationTemplate heatOrchestrationTemplate,
                                              TranslationContext context) {
    Object returnValue = new HashMap<>();
    List<Object> attributeFunctionExpression =
        translateGetAttributeFunctionExpression(function, heatFileName, heatOrchestrationTemplate,
            context);
    if (isResourceSupported(attributeFunctionExpression.get(0).toString())
        && isAttributeSupported(attributeFunctionExpression.get(0).toString())) {
      ((Map) returnValue)
          .put(ToscaFunctions.GET_ATTRIBUTE.getDisplayName(), attributeFunctionExpression);
    } else {
      returnValue = attributeFunctionExpression;
    }
    return returnValue;
  }

  private static Object handleGetParamFunction(Object function, String heatFileName,
                                               HeatOrchestrationTemplate heatOrchestrationTemplate,
                                               TranslationContext context) {
    Map returnValue = new HashMap<>();
    returnValue.put(ToscaFunctions.GET_INPUT.getDisplayName(),
        translateGetParamFunctionExpression(function, heatFileName, heatOrchestrationTemplate,
            context));
    return returnValue;
  }

  private static ArtifactDefinition createArtifactDefinition(Object function,
                                                             ToscaFileOutputService
                                                                     toscaFileOutputService) {
    ArtifactDefinition artifactDefinition = new ArtifactDefinition();
    artifactDefinition.setType(ToscaArtifactType.DEPLOYMENT.getDisplayName());
    artifactDefinition
        .setFile("../" + toscaFileOutputService.getArtifactsFolderName() + "/" + function);
    return artifactDefinition;
  }

  private static Object translateGetParamFunctionExpression(Object function, String heatFileName,
                                                            HeatOrchestrationTemplate
                                                                    heatOrchestrationTemplate,
                                                            TranslationContext context) {
    Object returnValue = null;
    if (function instanceof String) {
      returnValue = function;
    } else {
      if (function instanceof List) {
        returnValue = new ArrayList<>();
        for (int i = 0; i < ((List) function).size(); i++) {
          Object paramValue = ((List) function).get(i);
          if ((paramValue instanceof Map && !((Map) paramValue).isEmpty())) {
            Map<String, Object> paramMap = (Map) paramValue;
            Map.Entry<String, Object> entry = paramMap.entrySet().iterator().next();
            ((List) returnValue).add(
                getToscaFunction(entry.getKey(), entry.getValue(), heatFileName,
                    heatOrchestrationTemplate, null, context));
          } else {
            ((List) returnValue).add(paramValue);
          }
        }
      }
    }

    return returnValue;
  }

  private static List<Object> translateGetAttributeFunctionExpression(
          Object function,String heatFileName,
          HeatOrchestrationTemplate heatOrchestrationTemplate,
          TranslationContext context) {
    List<String> attributeParamList = (List) function;
    List<Object> toscaAttributeParamList = new ArrayList<>();

    Optional<String> resourceTranslatedId =
        handleResourceName(attributeParamList.get(0), heatFileName, heatOrchestrationTemplate,
            context);
    if (!resourceTranslatedId.isPresent()) {
      //unsupported resource
      toscaAttributeParamList.add(UNSUPPORTED_RESOURCE + attributeParamList.get(0));
      return toscaAttributeParamList;
    } else {
      toscaAttributeParamList.add(resourceTranslatedId.get());
    }

    Optional<List<Object>> toscaAttList =
        handleAttributeName(attributeParamList, heatOrchestrationTemplate, heatFileName, context);
    if (!toscaAttList.isPresent()) {
      //Unsupported attribute
      toscaAttributeParamList.clear();
      toscaAttributeParamList
          .add(UNSUPPORTED_ATTRIBUTE + attributeParamList.get(0) + "." + attributeParamList.get(1));
      return toscaAttributeParamList;
    } else {
      toscaAttributeParamList.addAll(toscaAttList.get());
    }

    Optional<List<String>> toscaIndexOrKey = handleAttributeIndexOrKey(attributeParamList);
    if (toscaIndexOrKey.isPresent()) {
      toscaAttributeParamList.addAll(toscaIndexOrKey.get());
    }

    return toscaAttributeParamList;
  }

  private static Optional<List<String>> handleAttributeIndexOrKey(List<String> attributeParamList) {
    List<String> attributeIndexOrKey = new ArrayList<>();
    if (attributeParamList.size() < 2) {
      return Optional.empty();
    }

    for (int i = 2; i < attributeParamList.size(); i++) {
      attributeIndexOrKey.add(attributeParamList.get(i));
    }

    return Optional.of(attributeIndexOrKey);
  }

  private static Optional<List<Object>> handleAttributeName(List<String> attributeParamList,
                                                            HeatOrchestrationTemplate
                                                                    heatOrchestrationTemplate,
                                                            String heatFileName,
                                                            TranslationContext context) {
    String resourceId = attributeParamList.get(0);
    Resource resource =
        HeatToToscaUtil.getResource(heatOrchestrationTemplate, resourceId, heatFileName);

    if (attributeParamList.size() == 1) {
      return getResourceTranslatedAttributesList(resource, context);
    }

    if (HeatToToscaUtil.isNestedResource(resource)) {
      return getNestedResourceTranslatedAttribute(attributeParamList.get(1));
    } else {
      return getResourceTranslatedAttribute(resource, attributeParamList.get(1), context);
    }
  }

  private static Optional<List<Object>> getResourceTranslatedAttribute(Resource resource,
                                                                       String attributeName,
                                                                       TranslationContext context) {
    List<Object> translatedAttributesList = new ArrayList<>();
    String translatedAttribute =
        context.getElementMapping(resource.getType(), Constants.ATTR, attributeName);
    if (translatedAttribute != null) {
      translatedAttributesList.add(translatedAttribute);
      return Optional.of(translatedAttributesList);
    } else {   //unsupported attribute
      return Optional.empty();
    }
  }

  private static Optional<List<Object>> getNestedResourceTranslatedAttribute(String attributeName) {
    List<Object> translatedAttributesList = new ArrayList<>();

    if (attributeName.startsWith(HeatConstants.GET_ATT_FROM_RESOURCE_GROUP_PREFIX)) {
      String[] attributeSplit = attributeName.split("\\.");
      if (attributeSplit.length == 2) {
        translatedAttributesList.add(attributeSplit[1]);
      } else if (attributeSplit.length == 3) {
        translatedAttributesList.add(attributeSplit[2]);
        translatedAttributesList.add(Integer.valueOf(attributeSplit[1]));
      } else {
        return Optional.empty();
      }
    } else {
      translatedAttributesList.add(attributeName);
    }
    return Optional.of(translatedAttributesList);
  }

  private static Optional<List<Object>> getResourceTranslatedAttributesList(Resource resource,
                                                                            TranslationContext
                                                                                    context) {
    List<Object> translatedAttributes = new ArrayList<>();
    if (HeatToToscaUtil.isNestedResource(resource)) {
      Optional<String> nestedFile = HeatToToscaUtil.getNestedFile(resource);
      if (!nestedFile.isPresent()) {
        return Optional.empty();
      }
      HeatOrchestrationTemplate nestedHeatOrchestrationTemplate = new YamlUtil()
          .yamlToObject(context.getFiles().getFileContent(nestedFile.get()),
              HeatOrchestrationTemplate.class);
      translatedAttributes.addAll(nestedHeatOrchestrationTemplate.getOutputs().keySet());
      return Optional.of(translatedAttributes);

    } else {
      Map<String, String> resourceMappingAttributes =
          context.getElementMapping(resource.getType(), Constants.ATTR);
      Set<String> mappingAttributes = new HashSet<>();
      mappingAttributes
          .addAll(resourceMappingAttributes.values().stream().collect(Collectors.toList()));
      translatedAttributes.addAll(mappingAttributes);
      return Optional.of(translatedAttributes);
    }
  }

  private static Optional<String> handleResourceName(String resourceId, String heatFileName,
                                                     HeatOrchestrationTemplate
                                                             heatOrchestrationTemplate,
                                                     TranslationContext context) {
    return ResourceTranslationBase
        .getResourceTranslatedId(heatFileName, heatOrchestrationTemplate, resourceId, context);
  }

  public static boolean isResourceSupported(String translatedResourceId) {
    return !translatedResourceId.startsWith(UNSUPPORTED_RESOURCE);
  }

  public static boolean isAttributeSupported(String translatedAttName) {
    return !translatedAttName.startsWith(UNSUPPORTED_ATTRIBUTE);
  }

  /**
   * Translate fn split function optional.
   *
   * @param propertyValue       the property value
   * @param listSize            the list size
   * @param includeBooleanValue the include boolean value
   * @return the optional
   */
  public static Optional<List<Map<String, List>>> translateFnSplitFunction(
          Object propertyValue,int listSize,
          boolean includeBooleanValue) {
    List<Map<String, List>> tokenPropertyValueList = new ArrayList<>();

    if (propertyValue instanceof Map && !((Map) propertyValue).isEmpty()) {
      Map<String, Object> propMap = (Map) propertyValue;
      Map.Entry<String, Object> entry = propMap.entrySet().iterator().next();
      Object entity = entry.getValue();
      String key = entry.getKey();
      String tokenChar;

      if (key.equals("Fn::Split") && entity instanceof List) {
        tokenChar = (String) ((List) entity).get(0);
        Object refParameter = ((List) entity).get(1);

        for (int substringIndex = 0; substringIndex < listSize; substringIndex++) {
          Map<String, List> tokenPropertyValue = new HashMap<>();
          tokenPropertyValue.put("token", new ArrayList<>());

          if (refParameter instanceof Map && ((Map) refParameter).get("Ref") != null) {
            Map<String, String> stringWithToken = new HashMap<>();
            ((Map) stringWithToken)
                .put(ToscaFunctions.GET_INPUT.getDisplayName(), ((Map) refParameter).get("Ref"));
            tokenPropertyValue.get("token").add(stringWithToken);
          } else if (refParameter instanceof String) {
            if (includeBooleanValue) {
              StringBuffer booleanBuffer = new StringBuffer();
              String[] booleanValueList = ((String) refParameter).split(tokenChar);
              for (int i = 0; i < booleanValueList.length; i++) {
                if (i == 0) {
                  booleanBuffer.append(HeatBoolean.eval(booleanValueList[i]));
                } else {
                  booleanBuffer.append(tokenChar);
                  booleanBuffer.append(HeatBoolean.eval(booleanValueList[i]));
                }
              }
              tokenPropertyValue.get("token").add(booleanBuffer.toString());
            } else {
              tokenPropertyValue.get("token").add(refParameter);
            }
          }
          tokenPropertyValue.get("token").add(tokenChar);
          tokenPropertyValue.get("token").add(substringIndex);
          tokenPropertyValueList.add(tokenPropertyValue);
        }

        return Optional.of(tokenPropertyValueList);

      }
    }

    return Optional.empty();
  }
}
