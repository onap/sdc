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

import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Output;
import org.openecomp.sdc.heat.datatypes.model.Parameter;
import org.openecomp.sdc.tosca.datatypes.extend.ToscaAnnotationType;
import org.openecomp.sdc.tosca.datatypes.model.*;
import org.openecomp.sdc.tosca.datatypes.model.heatextend.AnnotationDefinition;
import org.openecomp.sdc.tosca.datatypes.model.heatextend.ParameterDefinitionExt;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.services.heattotosca.FunctionTranslationFactory;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;

import java.util.*;


public class TranslatorHeatToToscaParameterConverter {


  private static Map<String, String> parameterTypeMapping;
  private static Map<String, String> parameterEntrySchemaTypeMapping;

  static {
    parameterEntrySchemaTypeMapping = new HashMap<>();
    parameterEntrySchemaTypeMapping.put("list", "string");
  }

  static {
    parameterTypeMapping = new HashMap<>();
    parameterTypeMapping.put("string", "string");
    parameterTypeMapping.put("number", "float");
    parameterTypeMapping.put("comma_delimited_list", "list");
    parameterTypeMapping.put("json", "json");
    parameterTypeMapping.put("boolean", "boolean");
  }

  /**
   * Parameter converter map.
   *
   * @param parameters                the parameters
   * @param heatOrchestrationTemplate the heat orchestration template
   * @param heatFileName              the heat file name
   * @param context                   the context
   * @return the map
   */
  public static Map<String, ParameterDefinition> parameterConverter(ServiceTemplate serviceTemplate,
      Map<String, Parameter> parameters, HeatOrchestrationTemplate heatOrchestrationTemplate,
      String heatFileName, TranslationContext context) {
    Map<String, ParameterDefinition> parameterDefinitionMap = new HashMap<>();
    for (Map.Entry<String, Parameter> entry : parameters.entrySet()) {
      String heatParamName = entry.getKey();
      String toscaParamName = heatParamName;
      parameterDefinitionMap.put(toscaParamName,
          getToscaParameter(serviceTemplate,heatParamName,toscaParamName, entry.getValue(),
              heatOrchestrationTemplate,
              heatFileName, context));
    }
    return parameterDefinitionMap;
  }

  /**
   * Parameter output converter map.
   *
   * @param parameters                the parameters
   * @param heatOrchestrationTemplate the heat orchestration template
   * @param heatFileName              the heat file name
   * @param context                   the context
   * @return the map
   */
  public static Map<String, ParameterDefinition> parameterOutputConverter(ServiceTemplate
                                                                              serviceTemplate,
      Map<String, Output> parameters, HeatOrchestrationTemplate heatOrchestrationTemplate,
      String heatFileName, TranslationContext context) {
    Map<String, ParameterDefinition> parameterDefinitionMap = new HashMap<>();
    for (Map.Entry<String, Output> entry : parameters.entrySet()) {
      parameterDefinitionMap.put(entry.getKey(),
          getToscaOutputParameter(serviceTemplate,entry.getKey(),entry.getValue(),
              heatOrchestrationTemplate,
              heatFileName,
              context));
    }
    return parameterDefinitionMap;
  }

  /**
   * Gets tosca parameter.
   *
   * @param heatParameter             the heat parameter
   * @param heatOrchestrationTemplate the heat orchestration template
   * @param heatFileName              the heat file name
   * @param context                   the context
   * @return the tosca parameter
   */
  public static ParameterDefinitionExt getToscaParameter(ServiceTemplate serviceTemplate,
                                                         String heatParameterName,
                                                         String toscaParameterName,
                                                         Parameter heatParameter,
                                                         HeatOrchestrationTemplate
                                                             heatOrchestrationTemplate,
                                                         String heatFileName,
                                                         TranslationContext context) {

    ParameterDefinitionExt toscaParameter = new ParameterDefinitionExt();
    toscaParameter.setType(getToscaParameterType(heatParameter.getType()));
    toscaParameter.setEntry_schema(getToscaParameterEntrySchema(toscaParameter.getType()));
    toscaParameter.setLabel(heatParameter.getLabel());
    toscaParameter.setDescription(heatParameter.getDescription());
    toscaParameter.set_default(
        getToscaParameterDefaultValue(serviceTemplate, heatParameterName, heatParameter.get_default(),
            toscaParameter.getType(), heatFileName, heatOrchestrationTemplate, context));
    toscaParameter.setHidden(heatParameter.isHidden());
    toscaParameter.setImmutable(heatParameter.isImmutable());
    toscaParameter.setConstraints(getToscaConstrains(heatParameter.getConstraints()));
    Optional<Map<String, AnnotationDefinition>>  annotaions = getToscaAnnotations(context, heatFileName, heatParameterName, toscaParameterName);
    if (annotaions.isPresent()) {
      toscaParameter.setAnnotations(annotaions.get());
    }

    return toscaParameter;
  }

  private static Optional<Map<String, AnnotationDefinition> > getToscaAnnotations (TranslationContext context, String heatFileName, String heatParameterName, String toscaParameterName){

    if(HeatToToscaUtil.isHeatFileNested(context, heatFileName)){
      return Optional.empty();
    }

    AnnotationDefinition annotationDefinition = new AnnotationDefinition();
    annotationDefinition.setType(ToscaAnnotationType.SOURCE);
    annotationDefinition.setProperties(new HashMap<>());
    List<String> vfModuleList = new ArrayList<>();
    vfModuleList.add( FileUtils.getFileWithoutExtention(heatFileName));
    annotationDefinition.getProperties().put(ToscaConstants.VF_MODULE_LABEL_PROPERTY_NAME, vfModuleList);
    annotationDefinition.getProperties().put(ToscaConstants.SOURCE_TYPE_PROPERTY_NAME, ToscaConstants.HEAT_SOURCE_TYPE);
    if(!heatParameterName.equals(toscaParameterName)){
      annotationDefinition.getProperties().put(ToscaConstants.PARAM_NAME_PROPERTY_NAME, heatParameterName);
    }
    Map<String, AnnotationDefinition> annotationMap = new HashMap<String, AnnotationDefinition>();
    annotationMap.put(ToscaConstants.SOURCE_ANNOTATION_ID, annotationDefinition);
    return Optional.of(annotationMap);

  }

  /**
   * Gets tosca output parameter.
   *
   * @param heatOutputParameter       the heat output parameter
   * @param heatOrchestrationTemplate the heat orchestration template
   * @param heatFileName              the heat file name
   * @param context                   the context
   * @return the tosca output parameter
   */
  public static ParameterDefinitionExt getToscaOutputParameter(ServiceTemplate serviceTemplate,
                                                               String parameterName,
                                                               Output heatOutputParameter,
                                                               HeatOrchestrationTemplate
                                                                   heatOrchestrationTemplate,
                                                               String heatFileName,
                                                               TranslationContext context) {

    ParameterDefinitionExt toscaParameter = new ParameterDefinitionExt();
    toscaParameter.setDescription(heatOutputParameter.getDescription());
    toscaParameter.setValue(
        getToscaParameterDefaultValue(serviceTemplate,parameterName,heatOutputParameter.getValue(),
            toscaParameter.getType(),
            heatFileName, heatOrchestrationTemplate, context));
    return toscaParameter;
  }

  /**
   * Gets tosca parameter default value.
   *
   * @param obj                       the a default
   * @param type                      the type
   * @param heatFileName              the heat file name
   * @param heatOrchestrationTemplate the heat orchestration template
   * @param context                   the context
   * @return the tosca parameter default value
   */
  public static Object getToscaParameterDefaultValue(ServiceTemplate serviceTemplate,
                                                     String parameterName,
                                                     Object obj, String type,
                                                     String heatFileName,
                                                     HeatOrchestrationTemplate
                                                         heatOrchestrationTemplate,
                                                     TranslationContext context) {

    if (obj == null) {
      return null;
    }
    Object toscaDefaultValue = obj;
    if ("list".equals(type)) {
      if (obj instanceof String) {
        return Arrays.asList(((String) obj).split(","));
      } else {
        return toscaDefaultValue;
      }
    }

    return getToscaParameterValue(serviceTemplate,parameterName,toscaDefaultValue, heatFileName,
        heatOrchestrationTemplate,
        context);
  }

  private static Object getToscaParameterValue(ServiceTemplate serviceTemplate,
                                               String parameterName,
                                               Object paramValue, String heatFileName,
                                               HeatOrchestrationTemplate heatOrchestrationTemplate,
                                               TranslationContext context) {
    if (paramValue instanceof Map) {
      if(MapUtils.isEmpty((Map) paramValue)){
        return new HashMap<>();
      }
      Map.Entry<String, Object> functionMapEntry =
          (Map.Entry<String, Object>) ((Map) paramValue).entrySet().iterator().next();
      if (FunctionTranslationFactory.getInstance(functionMapEntry.getKey()).isPresent()) {
        return FunctionTranslationFactory.getInstance(functionMapEntry.getKey()).get()
            .translateFunction(serviceTemplate, null, parameterName, functionMapEntry.getKey(),
                functionMapEntry.getValue(),heatFileName,
                heatOrchestrationTemplate, null, context);
      }
    }

    return paramValue;
  }

  private static List<Constraint> getToscaConstrains(List<Map<String, Object>> constraints) {
    if (constraints == null) {
      return null;
    }

    List<Constraint> constraintList = new ArrayList<>();

    for (Map<String, Object> constraint : constraints) {
      constraintList.addAll(getToscaParameterConstraint(constraint));
    }

    return constraintList;
  }

  private static List<Constraint> getToscaParameterConstraint(Map<String, Object> constraint) {
    List<Constraint> convertedConstraintList = new ArrayList<>();
    Constraint convertedConstraint;

    if (constraint.containsKey("range")) {
      convertedConstraint = new Constraint();
      convertedConstraintList.add(convertedConstraint);
      Integer min = (Integer) ((Map) constraint.get("range")).get("min");
      Integer max = (Integer) ((Map) constraint.get("range")).get("max");
      convertedConstraint.setIn_range(new Integer[]{min, max});

    } else if (constraint.containsKey("length")) {
      Integer min = (Integer) ((Map) constraint.get("length")).get("min");
      Integer max = (Integer) ((Map) constraint.get("length")).get("max");
      if (max != null) {
        convertedConstraint = new Constraint();
        convertedConstraintList.add(convertedConstraint);
        convertedConstraint.setMax_length(max);
      }
      if (min != null) {
        convertedConstraint = new Constraint();
        convertedConstraintList.add(convertedConstraint);
        convertedConstraint.setMin_length(min);
      }
    } else if (constraint.containsKey("allowed_values")) {
      convertedConstraint = new Constraint();
      convertedConstraintList.add(convertedConstraint);
      convertedConstraint.setValid_values((List) constraint.get("allowed_values"));
    } else if (constraint.containsKey("allowed_pattern")) {
      convertedConstraint = new Constraint();
      convertedConstraintList.add(convertedConstraint);
      convertedConstraint.setPattern(constraint.get("allowed_pattern"));
    }

    return convertedConstraintList;
  }

  private static EntrySchema getToscaParameterEntrySchema(String type) {

    if (!parameterEntrySchemaTypeMapping.containsKey(type)) {
      return null;
    }

    EntrySchema entrySchema = new EntrySchema();
    entrySchema.setType(parameterEntrySchemaTypeMapping.get(type));
    return entrySchema;
  }

  protected static String getToscaParameterType(String heatParameterType) {
    return parameterTypeMapping.get(heatParameterType);
  }
}
