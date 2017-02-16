package org.openecomp.sdc.tosca.services;

import org.openecomp.sdc.common.errors.CoreException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.openecomp.sdc.tosca.datatypes.model.GroupDefinition;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.PolicyDefinition;
import org.openecomp.sdc.tosca.datatypes.model.RelationshipTemplate;
import org.openecomp.sdc.tosca.datatypes.model.RequirementAssignment;
import org.openecomp.sdc.tosca.datatypes.model.SubstitutionMapping;

import java.util.ArrayList;

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
}