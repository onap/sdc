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

package org.openecomp.sdc.translator.services.heattotosca.globaltypes;

import org.openecomp.sdc.tosca.datatypes.ToscaDataType;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.model.Constraint;
import org.openecomp.sdc.tosca.datatypes.model.DataType;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.PropertyDefinition;
import org.openecomp.sdc.tosca.datatypes.model.PropertyType;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.services.heattotosca.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AbstractSubstituteGlobalType {

  private AbstractSubstituteGlobalType() {
  }

  /**
   * Create service template service template.
   *
   * @return the service template
   */
  public static ServiceTemplate createServiceTemplate() {
    ServiceTemplate serviceTemplate = new ServiceTemplate();
    serviceTemplate.setTosca_definitions_version(ToscaConstants.TOSCA_DEFINITIONS_VERSION);
    serviceTemplate.setMetadata(
        DataModelUtil.createMetadata(Constants.ABSTRACT_SUBSTITUTE_TEMPLATE_NAME, "1.0.0", null));
    serviceTemplate.setImports(GlobalTypesUtil.createCommonImportList());
    serviceTemplate.setDescription("Abstract Substitute Global Types");
    serviceTemplate.setData_types(createGlobalDataTypes());
    serviceTemplate.setNode_types(createGlobalNodeTypes());
    return serviceTemplate;
  }

  private static Map<String, NodeType> createGlobalNodeTypes() {
    Map<String, NodeType> globalNodeTypes = new HashMap<>();
    globalNodeTypes.put(ToscaNodeType.ABSTRACT_SUBSTITUTE.getDisplayName(),
        createAbstractSubstituteNodeType());
    return globalNodeTypes;
  }

  private static NodeType createAbstractSubstituteNodeType() {
    NodeType nodeType = new NodeType();
    nodeType.setDerived_from(ToscaNodeType.ROOT.getDisplayName());
    nodeType.setProperties(createAbstractSubstituteProperties());
    return nodeType;
  }

  private static Map<String, PropertyDefinition> createAbstractSubstituteProperties() {
    Map<String, PropertyDefinition> props = new HashMap<>();
    props.put(ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME, DataModelUtil
        .createPropertyDefinition(ToscaDataType.SUBSTITUTION_FILTERING.getDisplayName(),
            "Substitution Filter", true, null, null, null, null));

    return props;
  }


  private static Map<String, DataType> createGlobalDataTypes() {
    Map<String, DataType> globalDataTypes = new HashMap<>();
    globalDataTypes.put(ToscaDataType.SUBSTITUTION_FILTER.getDisplayName(),
        createSubstitutionFilterDataType());
    globalDataTypes.put(ToscaDataType.SUBSTITUTION_FILTERING.getDisplayName(),
        createSubstitutionFilteringDataType());
    return globalDataTypes;
  }

  private static DataType createSubstitutionFilterDataType() {
    DataType dataType = new DataType();
    dataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    dataType.setDescription("Substitution Filter");
    Map<String, PropertyDefinition> properties = new HashMap<>();
    properties.put(ToscaConstants.COUNT_PROPERTY_NAME, DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Count", false, null, null,
            null, 1));
    properties.put(ToscaConstants.SCALING_ENABLED_PROPERTY_NAME, DataModelUtil
        .createPropertyDefinition(PropertyType.BOOLEAN.getDisplayName(),
            "Indicates whether service scaling is enabled", false, null, null, null, true));
    properties.put(ToscaConstants.SUBSTITUTE_SERVICE_TEMPLATE_PROPERTY_NAME, DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Substitute Service Template", true, null, null, null, null));
    properties.put("mandatory", DataModelUtil
        .createPropertyDefinition(PropertyType.BOOLEAN.getDisplayName(), "Mandatory", false, null,
            null, null, true));
    properties.put("index_variable", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Index variable", false,
            createMinLengthConstraint(), null, null, "%index%"));

    dataType.setProperties(properties);
    return dataType;
  }

  private static List<Constraint> createMinLengthConstraint() {
    List<Constraint> constraints;
    constraints = new ArrayList<>();
    Constraint constraint = new Constraint();
    constraint.setMin_length(3);
    constraints.add(constraint);
    return constraints;
  }

  private static DataType createSubstitutionFilteringDataType() {
    DataType dataType = new DataType();
    dataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    dataType.setDescription("Substitution Filter");
    Map<String, PropertyDefinition> properties = new HashMap<>();
    properties.put(ToscaConstants.COUNT_PROPERTY_NAME, DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Count", false, null, null,
            null, 1));
    properties.put(ToscaConstants.INDEX_VALUE_PROPERTY_NAME, DataModelUtil
        .createPropertyDefinition(PropertyType.INTEGER.getDisplayName(),
            "Index value of the substitution service template runtime instance", false,
            createIndexValueConstraint(), null, null, 0));
    properties.put(ToscaConstants.SCALING_ENABLED_PROPERTY_NAME, DataModelUtil
        .createPropertyDefinition(PropertyType.BOOLEAN.getDisplayName(),
            "Indicates whether service scaling is enabled", false, null, null, null, true));
    properties.put(ToscaConstants.SUBSTITUTE_SERVICE_TEMPLATE_PROPERTY_NAME, DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Substitute Service Template", true, null, null, null, null));
    properties.put("mandatory", DataModelUtil
        .createPropertyDefinition(PropertyType.BOOLEAN.getDisplayName(), "Mandatory", false, null,
            null, null, true));


    dataType.setProperties(properties);
    return dataType;
  }

  private static List<Constraint> createIndexValueConstraint() {
    List<Constraint> constraints;
    constraints = new ArrayList<>();
    Constraint constraint = new Constraint();
    constraint.setGreater_or_equal(0);
    constraints.add(constraint);
    return constraints;
  }


}
