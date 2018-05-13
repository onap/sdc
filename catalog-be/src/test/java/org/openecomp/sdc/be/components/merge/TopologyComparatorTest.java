package org.openecomp.sdc.be.components.merge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.utils.ComponentInstanceBuilder;
import org.openecomp.sdc.be.components.utils.ObjectGenerator;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import fj.data.Either;

public class TopologyComparatorTest {

    @InjectMocks
    private TopologyComparator testInstance;

    @Mock
    private RelationsComparator relationsComparator;

    @Mock
    private ToscaOperationFacade toscaOperationFacade;

    @Mock
    private ComponentsUtils componentsUtils;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void compareTopologies_NotSameNumOfInstances() throws Exception {
        Resource resourceWith2Instances = ObjectGenerator.buildResourceWithComponentInstance("inst1", "inst2");
        Resource resourceWith1Instances = ObjectGenerator.buildResourceWithComponentInstance("inst1");
        Resource resourceWithNoInstances = new Resource();
        assertTrue(testInstance.isTopologyChanged(resourceWithNoInstances, resourceWith2Instances).left().value());
        assertTrue(testInstance.isTopologyChanged(resourceWithNoInstances, resourceWith1Instances).left().value());
        assertTrue(testInstance.isTopologyChanged(resourceWith1Instances, resourceWith2Instances).left().value());
    }

    @Test
    public void compareTopologies_notSameInstanceNames() throws Exception {
        Resource resource1 = ObjectGenerator.buildResourceWithComponentInstance("inst1", "inst2");
        Resource resource2 = ObjectGenerator.buildResourceWithComponentInstance("inst1", "inst3");
        assertTrue(testInstance.isTopologyChanged(resource1, resource2).left().value());
    }

    @Test
    public void compareTopologies_notSameInstanceTypes_notSameOriginInstanceTypes() throws Exception {
        ComponentInstance inst1 = new ComponentInstanceBuilder().setName("inst1").setComponentUid("inst1").setToscaName("a.b.c").build();
        ComponentInstance inst2 = new ComponentInstanceBuilder().setName("inst2").setComponentUid("inst2").setToscaName("a.b.c.d").build();
        ComponentInstance inst2DiffType = new ComponentInstanceBuilder().setName("inst2").setComponentUid("inst2DiffType").setToscaName("a.b.c.d.e").build();
        Resource resource1 = ObjectGenerator.buildResourceWithComponentInstances(inst1, inst2);
        Resource resource2 = ObjectGenerator.buildResourceWithComponentInstances(inst1, inst2DiffType);
        Resource inst2OriginResource = new ResourceBuilder().setInvariantUUid("inst2Invariant").build();
        Resource inst2DiffTypeOriginResource = new ResourceBuilder().setInvariantUUid("inst2DiffTypeInvariant").build();
        when(toscaOperationFacade.getToscaElement(inst2.getComponentUid())).thenReturn(Either.left(inst2OriginResource));
        when(toscaOperationFacade.getToscaElement(inst2DiffType.getComponentUid())).thenReturn(Either.left(inst2DiffTypeOriginResource));
        assertTrue(testInstance.isTopologyChanged(resource1, resource2).left().value());
    }

    @Test
    public void compareTopologies_notSameInstanceTypes_failToFetchOriginComponent() throws Exception {
        ComponentInstance inst1 = new ComponentInstanceBuilder().setName("inst1").setComponentUid("inst1").setToscaName("a.b.c").build();
        ComponentInstance inst1DiffOriginCmpt = new ComponentInstanceBuilder().setName("inst1").setComponentUid("inst1Diff").setToscaName("a.b.c.d").build();
        Resource resource1 = ObjectGenerator.buildResourceWithComponentInstances(inst1);
        Resource resource2 = ObjectGenerator.buildResourceWithComponentInstances(inst1DiffOriginCmpt);
        when(toscaOperationFacade.getToscaElement(inst1.getComponentUid())).thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.GENERAL_ERROR)).thenReturn(ActionStatus.GENERAL_ERROR);
        assertEquals(ActionStatus.GENERAL_ERROR, testInstance.isTopologyChanged(resource1, resource2).right().value());
    }

    @Test
    public void compareTopologies_notSameRelations() throws Exception {
        ComponentInstance inst1 = new ComponentInstanceBuilder().setName("inst1").setToscaName("a.b.c").build();
        ComponentInstance inst2 = new ComponentInstanceBuilder().setName("inst2").setToscaName("a.b.c.d").build();
        Resource resource1 = ObjectGenerator.buildResourceWithComponentInstances(inst1, inst2);
        Resource resource2 = ObjectGenerator.buildResourceWithComponentInstances(inst1, inst2);
        when(relationsComparator.isRelationsChanged(resource1, resource2)).thenReturn(true);
        assertTrue(testInstance.isTopologyChanged(resource1, resource2).left().value());
    }

    @Test
    public void compareTopologies_sameInstances_sameRelations_noTopologyChange() throws Exception {
        ComponentInstance inst1 = new ComponentInstanceBuilder().setName("inst1").setToscaName("a.b.c").build();
        ComponentInstance inst2 = new ComponentInstanceBuilder().setName("inst2").setToscaName("a.b.c.d").build();
        Resource resource1 = ObjectGenerator.buildResourceWithComponentInstances(inst1, inst2);
        Resource resource2 = ObjectGenerator.buildResourceWithComponentInstances(inst1, inst2);
        when(relationsComparator.isRelationsChanged(resource1, resource2)).thenReturn(false);
        assertFalse(testInstance.isTopologyChanged(resource1, resource2).left().value());
    }

    @Test
    public void compareTopologies_sameInstancesInvariant_sameRelations_noTopologyChange() throws Exception {
        ComponentInstance inst1 = new ComponentInstanceBuilder().setName("inst1").setComponentUid("inst1").setToscaName("a.b.c").build();
        ComponentInstance inst2 = new ComponentInstanceBuilder().setName("inst2").setComponentUid("inst2").setToscaName("a.b.c.d").build();
        ComponentInstance inst2DiffType = new ComponentInstanceBuilder().setName("inst2").setComponentUid("inst2DiffType").setToscaName("a.b.c.d.e").build();
        Resource resource1 = ObjectGenerator.buildResourceWithComponentInstances(inst1, inst2);
        Resource resource2 = ObjectGenerator.buildResourceWithComponentInstances(inst1, inst2DiffType);
        Resource inst2OriginResource = new ResourceBuilder().setInvariantUUid("inst2Invariant").build();
        when(toscaOperationFacade.getToscaElement(inst2.getComponentUid())).thenReturn(Either.left(inst2OriginResource));
        when(toscaOperationFacade.getToscaElement(inst2DiffType.getComponentUid())).thenReturn(Either.left(inst2OriginResource));
        when(relationsComparator.isRelationsChanged(resource1, resource2)).thenReturn(false);
        assertFalse(testInstance.isTopologyChanged(resource1, resource2).left().value());
    }
}