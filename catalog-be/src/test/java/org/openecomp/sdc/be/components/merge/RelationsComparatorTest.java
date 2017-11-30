package org.openecomp.sdc.be.components.merge;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.components.utils.ComponentInstanceBuilder;
import org.openecomp.sdc.be.components.utils.ObjectGenerator;
import org.openecomp.sdc.be.components.utils.RelationsBuilder;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.Resource;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RelationsComparatorTest {

    public static final String INSTANCE1 = "instance1";
    public static final String INSTANCE2 = "instance2";
    RelationsComparator testInstance;

    private RequirementCapabilityRelDef relation1, relation2, relation3, relation4;
    private ComponentInstance componentInstance1, componentInstance2;

    @Before
    public void setUp() {
        testInstance = new RelationsComparator();
        componentInstance1 =  new ComponentInstanceBuilder().setName(INSTANCE1).setId(INSTANCE1).build();
        componentInstance2 =  new ComponentInstanceBuilder().setName(INSTANCE2).setId(INSTANCE2).build();
        buildRelations();
    }

    @Test
    public void isRelationsChanged_sameRelationships() throws Exception {
        Resource oldResource = ObjectGenerator.buildResourceWithRelationships(relation1, relation2, relation3, relation4);
        oldResource.setComponentInstances(Arrays.asList(componentInstance1, componentInstance2));
        Resource newResource = ObjectGenerator.buildResourceWithRelationships(relation4, relation3, relation1, relation2);
        newResource.setComponentInstances(Arrays.asList(componentInstance1, componentInstance2));
        assertFalse(testInstance.isRelationsChanged(oldResource, newResource));
    }

    @Test
    public void isRelationsChanged_notSameAmountOfRelations()  {
        Resource oldResource = ObjectGenerator.buildResourceWithRelationships(relation1, relation2);
        oldResource.setComponentInstances(Arrays.asList(componentInstance1, componentInstance2));
        Resource newResource = ObjectGenerator.buildResourceWithRelationships(relation1, relation2, relation3);
        newResource.setComponentInstances(Arrays.asList(componentInstance1, componentInstance2));
        assertTrue(testInstance.isRelationsChanged(oldResource, newResource));
    }

    @Test
    public void isRelationsChanged_notSameFromNode() throws Exception {
        RequirementCapabilityRelDef relation2DifType = buildRelation("2", INSTANCE1);
        relation2DifType.setFromNode(INSTANCE2);
        isRelationsChangedTest(relation2DifType);
    }

    @Test
    public void isRelationsChanged_notSameType() throws Exception {
        RequirementCapabilityRelDef relation2DifType = buildRelation("2", INSTANCE1);
        relation2DifType.getSingleRelationship().getRelationship().setType("someDiffType");
        isRelationsChangedTest(relation2DifType);
    }

    @Test
    public void isRelationsChanged_notSameCapability() throws Exception {
        RequirementCapabilityRelDef relation2DifType = buildRelation("2", INSTANCE1);
        relation2DifType.getSingleRelationship().setCapabilityUid("someDiffUid");
        isRelationsChangedTest(relation2DifType);
    }

    @Test
    public void isRelationsChanged_notSameReqName() throws Exception {
        RequirementCapabilityRelDef relation2DifType = buildRelation("2", INSTANCE1);
        relation2DifType.getSingleRelationship().setRequirement("someDiffReq");
        isRelationsChangedTest(relation2DifType);
    }

    @Test
    public void isRelationsChanged_notSameToNode() throws Exception {
        RequirementCapabilityRelDef relation2DifType = buildRelation("2", INSTANCE1);
        relation2DifType.setToNode("someDiffNode");
        isRelationsChangedTest(relation2DifType);
    }

    private void isRelationsChangedTest(RequirementCapabilityRelDef relation2DifType) {
        Resource oldResource = ObjectGenerator.buildResourceWithRelationships(relation1, relation2);
        oldResource.setComponentInstances(Arrays.asList(componentInstance1, componentInstance2));
        Resource newResource = ObjectGenerator.buildResourceWithRelationships(relation1, relation2DifType);
        newResource.setComponentInstances(Arrays.asList(componentInstance1, componentInstance2));
        assertTrue(testInstance.isRelationsChanged(oldResource, newResource));
    }


    private void buildRelations() {
        relation1 = buildRelation("1", INSTANCE1);
        relation2 = buildRelation("2", INSTANCE1);
        relation3 = buildRelation("3", INSTANCE2);
        relation4 = buildRelation("4", INSTANCE2);
    }

    private RequirementCapabilityRelDef buildRelation(String postFix, String instance) {
        return new RelationsBuilder()
                .setFromNode(instance)
                .setCapabilityUID("cap" + postFix)
                .setRelationType("type" + postFix)
                .setRequirementName("req" + postFix)
                .setToNode(instance)
                .build();
    }
}