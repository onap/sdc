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

package org.openecomp.sdc.tosca.datatypes.model;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.YamlUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shiria
 * @since September 06, 2016.
 */
public class CapabilityDefinitionTest {

  @Test
  public void cloneTest() {
    CapabilityDefinition capDef1 = new CapabilityDefinition();
    Map<String, AttributeDefinition> attributes = new HashMap<>();
    attributes.put("key1", getAttributeDefinition());
    capDef1.setAttributes(attributes);

    capDef1.setDescription("This is my desc");
    capDef1.setOccurrences(getMockOccurrences());

    Map<String, PropertyDefinition> properties = new HashMap<>();
    PropertyDefinition propertyDefinition = getMockPropertyDefinition();
    properties.put("key1", propertyDefinition);
    capDef1.setProperties(properties);
    capDef1.setType("My Type");
    List<String> valid_source_types = new ArrayList<>();
    valid_source_types.add("nonono");
    capDef1.setValid_source_types(valid_source_types);

    CapabilityDefinition capDef2 = capDef1.clone();
    NodeType nodeType = new NodeType();
    nodeType.setCapabilities(new HashMap<>());
    nodeType.getCapabilities().put("cap1", capDef1);
    nodeType.getCapabilities().put("cap2", capDef2);

    String yamlString = new YamlUtil().objectToYaml(nodeType);
    Boolean passResult = !yamlString.contains("&") && !yamlString.contains("*");
    Assert.assertEquals(true, passResult);
  }

  private PropertyDefinition getMockPropertyDefinition() {
    PropertyDefinition propertyDefinition = new PropertyDefinition();
    propertyDefinition.setConstraints(getMockConstraints());
    propertyDefinition.setDescription("desc");
    propertyDefinition.setType("typeProp");
    propertyDefinition.set_default(5);
    propertyDefinition.setEntry_schema(getMockEntrySchema());
    propertyDefinition.setRequired(false);
    propertyDefinition.setStatus(Status.UNSUPPORTED);
    return propertyDefinition;
  }

  private Object[] getMockOccurrences() {
    Object[] occurrences = new Object[2];
    occurrences[0] = 2;
    occurrences[1] = ToscaConstants.UNBOUNDED;
    return occurrences;
  }

  private ArtifactDefinition getMockArtifactDefinition() {
    ArtifactDefinition artifactDefinition = new ArtifactDefinition();
    artifactDefinition.setType("type1");
    artifactDefinition.setDescription("description of openecomp def");
    artifactDefinition.setDeploy_path("my deployment path");
    artifactDefinition.setFile("my file");
    artifactDefinition.setRepository("my repository");
    return artifactDefinition;
  }

  private AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attributeDefinition = new AttributeDefinition();
    attributeDefinition.setDescription("desc1");
    attributeDefinition.setType("type1");
    attributeDefinition.set_default("none");
    attributeDefinition.setEntry_schema(getMockEntrySchema());
    attributeDefinition.setStatus(Status.UNSUPPORTED);
    return attributeDefinition;
  }

  private EntrySchema getMockEntrySchema() {
    EntrySchema entrySchema = new EntrySchema();
    entrySchema.setType("string");
    entrySchema.setDescription("string for string");
    List<Constraint> constraints = getMockConstraints();
    entrySchema.setConstraints(constraints);
    return entrySchema;
  }

  private List<Constraint> getMockConstraints() {
    List<Constraint> constraints = new ArrayList<>();
    Constraint constraint = new Constraint();
    constraint.setEqual("5");
    constraints.add(constraint);
    return constraints;
  }

}
