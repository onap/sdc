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

package org.openecomp.sdc.tosca.services;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.sdc.tosca.datatypes.model.*;
import org.openecomp.sdc.common.errors.CoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

/**
 * @author shiria
 * @since September 15, 2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataModelUtilTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testAddSubstitutionMapping() throws Exception {
    thrown.expect(CoreException.class);
    thrown.expectMessage(
        "Invalid action, can't add 'Substitution Mapping' to 'Service Template', 'Service Template' entity is NULL.");
    DataModelUtil.addSubstitutionMapping(null, new SubstitutionMapping());
  }

  @Test
  public void testAddSubstitutionMappingReq() throws Exception {
    thrown.expect(CoreException.class);
    thrown.expectMessage(
        "Invalid action, can't add 'Substitution Mapping Requirements' to 'Service Template', 'Service Template' entity is NULL.");
    DataModelUtil.addSubstitutionMappingReq(null, "123", new ArrayList<>());
  }

  @Test
  public void testAddNodeTemplate() throws Exception {
    thrown.expect(CoreException.class);
    thrown.expectMessage(
        "Invalid action, can't add 'Node Template' to 'Service Template', 'Service Template' entity is NULL.");
    DataModelUtil.addNodeTemplate(null, "123", new NodeTemplate());
  }

  @Test
  public void testAddPolicyDefinition() throws Exception {
    thrown.expect(CoreException.class);
    thrown.expectMessage(
        "Invalid action, can't add 'Policy Definition' to 'Service Template', 'Service Template' entity is NULL.");
    DataModelUtil.addPolicyDefinition(null, "123", new PolicyDefinition());
  }

  @Test
  public void testAddNodeType() throws Exception {
    thrown.expect(CoreException.class);
    thrown.expectMessage(
        "Invalid action, can't add 'Node Type' to 'Service Template', 'Service Template' entity is NULL.");
    DataModelUtil.addNodeType(null, "123", new NodeType());
  }

  @Test
  public void testAddRelationshipTemplate() throws Exception {
    thrown.expect(CoreException.class);
    thrown.expectMessage(
        "Invalid action, can't add 'Relationship Template' to 'Service Template', 'Service Template' entity is NULL.");
    DataModelUtil.addRelationshipTemplate(null, "123", new RelationshipTemplate());
  }

  @Test
  public void testAddRequirementAssignment() throws Exception {
    thrown.expect(CoreException.class);
    thrown.expectMessage(
        "Invalid action, can't add 'Requirement Assignment' to 'Node Template', 'Node Template' entity is NULL.");
    DataModelUtil.addRequirementAssignment(null, "123", new RequirementAssignment());
  }

  @Test
  public void testGetNodeTemplate() throws Exception {
    thrown.expect(CoreException.class);
    thrown.expectMessage(
        "Invalid action, can't add 'Node Template' to 'Service Template', 'Service Template' entity is NULL.");
    DataModelUtil.addNodeTemplate(null, "123", new NodeTemplate());
  }

  @Test
  public void testGetNodeType() throws Exception {
    thrown.expect(CoreException.class);
    thrown.expectMessage(
        "Invalid action, can't add 'Node Type' to 'Service Template', 'Service Template' entity is NULL.");
    DataModelUtil.addNodeType(null, "123", new NodeType());
  }

  @Test
  public void testAddGroupToTopologyTemplate() throws Exception {
    thrown.expect(CoreException.class);
    thrown.expectMessage(
        "Invalid action, can't add 'Group Definition' to 'Service Template', 'Service Template' entity is NULL.");
    DataModelUtil.addGroupDefinitionToTopologyTemplate(null, "123", new GroupDefinition());
  }


  @Test
  public void testGetRelationshipTemplate(){
    RelationshipTemplate relationshipTemplate = new RelationshipTemplate();
    String testingRelationshipType = "testingRelationshipType";
    relationshipTemplate.setType(testingRelationshipType);
    TopologyTemplate topologyTemplate = new TopologyTemplate();
    topologyTemplate.setRelationship_templates(new HashMap<>());
    String relationId = "rtest";
    topologyTemplate.getRelationship_templates().put(relationId, relationshipTemplate);
    ServiceTemplate serviceTemplate = new ServiceTemplate();
    serviceTemplate.setTopology_template(topologyTemplate);

    Optional<RelationshipTemplate> relationshipTemplateOut =
            DataModelUtil.getRelationshipTemplate(serviceTemplate, relationId);
    Assert.assertNotNull(relationshipTemplateOut);
    Assert.assertEquals(true,relationshipTemplateOut.isPresent());
    Assert.assertEquals(testingRelationshipType, relationshipTemplateOut.get().getType());
  }

  @Test
  public void testGetEmptyRelationshipTemplate(){
    ServiceTemplate serviceTemplate = new ServiceTemplate();
    String relationId = "rtest";
    Optional<RelationshipTemplate> relationshipTemplateOut =
            DataModelUtil.getRelationshipTemplate(serviceTemplate, relationId);
    Assert.assertNotNull(relationshipTemplateOut);
    Assert.assertEquals(false,relationshipTemplateOut.isPresent());
  }
}
