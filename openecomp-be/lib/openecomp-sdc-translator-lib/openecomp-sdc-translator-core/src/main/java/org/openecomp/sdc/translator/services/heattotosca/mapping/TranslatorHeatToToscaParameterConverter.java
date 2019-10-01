/*
 * Copyright © 2016-2018 European Support Limited
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

package org.openecomp.sdc.translator.services.heattotosca.mapping;

import static org.openecomp.sdc.translator.services.heattotosca.impl.functiontranslation.FunctionTranslator.getFunctionTranslateTo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.sdc.tosca.datatypes.model.Constraint;
import org.onap.sdc.tosca.datatypes.model.EntrySchema;
import org.onap.sdc.tosca.datatypes.model.ParameterDefinition;
import org.onap.sdc.tosca.datatypes.model.ScalarUnitValidator;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.datatypes.model.heatextend.AnnotationDefinition;
import org.onap.sdc.tosca.datatypes.model.heatextend.ParameterDefinitionExt;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.heat.datatypes.DefinedHeatParameterTypes;
import org.openecomp.sdc.heat.datatypes.ToscaScalarUnitFrequency;
import org.openecomp.sdc.heat.datatypes.ToscaScalarUnitSize;
import org.openecomp.sdc.heat.datatypes.ToscaScalarUnitTime;
import org.openecomp.sdc.heat.datatypes.ToscaScalarUnitTypes;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Output;
import org.openecomp.sdc.heat.datatypes.model.Parameter;
import org.openecomp.sdc.tosca.datatypes.extend.ToscaAnnotationType;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.services.heattotosca.FunctionTranslationFactory;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.impl.functiontranslation.FunctionTranslator;

public class TranslatorHeatToToscaParameterConverter {


  private static Map<String, String> parameterTypeMapping;
  private static Map<String, String> parameterEntrySchemaTypeMapping;
  private static final String RANGE = "range";
  private static final String LENGTH = "length";
  private static final String MIN = "min";
  private static final String MAX = "max";
  private static final String ALLOWED_VALUES = "allowed_values";
  private static final String ALLOWED_PATTERN = "allowed_pattern";


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
                                                                    Map<String, Parameter> parameters,
                                                                    HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                                    String heatFileName, String parentHeatFileName,
                                                                    TranslationContext context,
                                                                    Map<String, Object> heatEnvParameters) {
    Map<String, ParameterDefinition> parameterDefinitionMap = new HashMap<>();
    for (Map.Entry<String, Parameter> entry : parameters.entrySet()) {
      String heatParamName = entry.getKey();
      parameterDefinitionMap.put(heatParamName,
          getToscaParameter(serviceTemplate,heatParamName, entry.getValue(), heatOrchestrationTemplate, heatFileName,
              parentHeatFileName, context, heatEnvParameters));
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
  public static Map<String, ParameterDefinition> parameterOutputConverter(ServiceTemplate serviceTemplate,
                                                                          Map<String, Output> parameters,
                                                                          HeatOrchestrationTemplate heatOrchestrationTemplate,
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
  private static ParameterDefinitionExt getToscaParameter(ServiceTemplate serviceTemplate,
                                                          String heatParameterName,
                                                          Parameter heatParameter,
                                                          HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                          String heatFileName,
                                                          String parentHeatFileName,
                                                          TranslationContext context,
                                                          Map<String, Object> heatEnvParameters) {

    ParameterDefinitionExt toscaParameter = new ParameterDefinitionExt();
    toscaParameter.setType(getToscaParameterType(heatParameter.getType(), heatEnvParameters));
    toscaParameter.setEntry_schema(getToscaParameterEntrySchema(toscaParameter.getType()));
    toscaParameter.setLabel(heatParameter.getLabel());
    toscaParameter.setDescription(heatParameter.getDescription());
    toscaParameter.set_default(
        getToscaParameterDefaultValue(serviceTemplate, heatParameterName, heatParameter.get_default(),
            toscaParameter.getType(), heatFileName, heatOrchestrationTemplate, context));
    toscaParameter.setHidden(heatParameter.isHidden());
    toscaParameter.setImmutable(heatParameter.isImmutable());
    toscaParameter.setConstraints(getToscaConstrains(heatParameter.getConstraints()));
    Optional<Map<String, AnnotationDefinition>>  annotations = getToscaAnnotations(context, heatFileName, parentHeatFileName, heatParameterName);
    annotations.ifPresent(ant->toscaParameter.setAnnotations(annotations.get()));


    return toscaParameter;
  }

  private static Optional<Map<String, AnnotationDefinition> > getToscaAnnotations (TranslationContext context, String heatFileName, String parentHeatFileName, String heatParameterName){

    if(parentHeatFileName != null){
      heatFileName = parentHeatFileName;
    }

    if(!isAnnotationRequired(context, heatFileName)){
      return Optional.empty();
    }

    AnnotationDefinition annotationDefinition = new AnnotationDefinition();
    annotationDefinition.setType(ToscaAnnotationType.SOURCE);
    annotationDefinition.setProperties(new HashMap<>());
    List<String> vfModuleList = new ArrayList<>();
    vfModuleList.add( FileUtils.getFileWithoutExtention(heatFileName));
    annotationDefinition.getProperties().put(ToscaConstants.VF_MODULE_LABEL_PROPERTY_NAME, vfModuleList);
    annotationDefinition.getProperties().put(ToscaConstants.SOURCE_TYPE_PROPERTY_NAME, ToscaConstants.HEAT_SOURCE_TYPE);
    annotationDefinition.getProperties().put(ToscaConstants.PARAM_NAME_PROPERTY_NAME, heatParameterName);
    Map<String, AnnotationDefinition> annotationMap = new HashMap<>();
    annotationMap.put(ToscaConstants.SOURCE_ANNOTATION_ID, annotationDefinition);
    return Optional.of(annotationMap);

  }

  private static boolean isAnnotationRequired(TranslationContext context, String heatFileName){
    return !isNestedServiceTemplate(context, heatFileName);
  }

  private static boolean isNestedServiceTemplate(TranslationContext context, String heatFileName) {
    return HeatToToscaUtil.isHeatFileNested(context, heatFileName);
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
  private static ParameterDefinitionExt getToscaOutputParameter(ServiceTemplate serviceTemplate,
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

        FunctionTranslator functionTranslator = new FunctionTranslator(getFunctionTranslateTo(serviceTemplate, null,
                heatFileName, heatOrchestrationTemplate, context), parameterName,  functionMapEntry.getValue(), null);
        return FunctionTranslationFactory.getInstance(functionMapEntry.getKey()).get()
            .translateFunction(functionTranslator);
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

    if (constraint.containsKey(RANGE)) {
      convertedConstraint = new Constraint();
      convertedConstraintList.add(convertedConstraint);
      Integer min = (Integer) ((Map) constraint.get(RANGE)).get(MIN);
      Integer max = (Integer) ((Map) constraint.get(RANGE)).get(MAX);
      convertedConstraint.setIn_range(new Integer[]{min, max});

    } else if (constraint.containsKey(LENGTH)) {
      Integer min = (Integer) ((Map) constraint.get(LENGTH)).get(MIN);
      Integer max = (Integer) ((Map) constraint.get(LENGTH)).get(MAX);
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
    } else if (constraint.containsKey(ALLOWED_VALUES)) {
      convertedConstraint = new Constraint();
      convertedConstraintList.add(convertedConstraint);
      convertedConstraint.setValid_values((List) constraint.get(ALLOWED_VALUES));
    } else if (constraint.containsKey(ALLOWED_PATTERN)) {
      convertedConstraint = new Constraint();
      convertedConstraintList.add(convertedConstraint);
      convertedConstraint.setPattern(constraint.get(ALLOWED_PATTERN));
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

  protected static String getToscaParameterType(final String heatParameterType,
                                                final Map<String, Object> heatEnvParameters) {
    if (heatEnvParameters != null && DefinedHeatParameterTypes.NUMBER.getType().equals(heatParameterType)) {
      if (getScalarUnitType(heatEnvParameters, ToscaScalarUnitSize.class) != null) {
        return ToscaScalarUnitTypes.SCALAR_UNIT_SIZE.getType();
      } else if (getScalarUnitType(heatEnvParameters, ToscaScalarUnitTime.class) != null) {
        return ToscaScalarUnitTypes.SCALAR_UNIT_TIME.getType();
      } else if (getScalarUnitType(heatEnvParameters, ToscaScalarUnitFrequency.class) != null) {
        return ToscaScalarUnitTypes.SCALAR_UNIT_FREQUENCY.getType();
      }
    }

    return parameterTypeMapping.get(heatParameterType);
  }

  private static <E extends Enum<E>> String getScalarUnitType(final Map<String, Object> heatEnvParameters,
                                                              final Class<E> enumClass) {
    final ScalarUnitValidator scalarUnitValidator = ScalarUnitValidator.getInstance();
    if (Arrays.stream(enumClass.getEnumConstants()).anyMatch(unitType ->
            heatEnvParameters.values().stream().filter(Objects::nonNull)
                .anyMatch(parameterValue -> scalarUnitValidator.isScalarUnit(parameterValue.toString()) &&
                    Arrays.stream(StringUtils.split(parameterValue.toString()))
                        .anyMatch(strParamValue -> strParamValue.equalsIgnoreCase(unitType.name()))))) {
      return enumClass.getTypeName();
    }
    return null;
  }

}
