package org.openecomp.sdc.be.model.jsontitan.operations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.MapComponentInstanceExternalRefs;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.jsontitan.utils.GraphTestUtils;
import org.openecomp.sdc.be.model.jsontitan.utils.IdMapper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fj.data.Either;

/**
 * Created by yavivi on 26/01/2018.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
public class ExternalReferencesOperationTest extends ModelTestBase {

    @Resource
    private ExternalReferencesOperation externalReferenceOperation;

    @Resource
    private TitanDao titanDao;

    private boolean isInitialized;

    private GraphVertex serviceVertex;

    private String serviceVertexUuid;
    private static final String COMPONENT_ID = "ci-MyComponentName";

    private static final String MONITORING_OBJECT_TYPE = "monitoring";
    private static final String WORKFLOW_OBJECT_TYPE = "workflow";
    private static final String REF_1 = "ref1";
    private static final String REF_2 = "ref2";
    private static final String REF_3 = "ref3";
    private static final String REF_4 = "ref4";
    private static final String REF_5 = "ref5";

    //workflow
    private static final String REF_6 = "ref6";

    private IdMapper idMapper;

    @BeforeClass
    public static void initTest(){
        ModelTestBase.init();
    }

    @Before
    public void beforeTest() {
        idMapper = Mockito.mock(IdMapper.class);
        this.externalReferenceOperation.setIdMapper(idMapper);
        when(idMapper.mapComponentNameToUniqueId(Mockito.anyString(), Mockito.any(GraphVertex.class))).thenReturn(COMPONENT_ID);
        if (!isInitialized) {
            GraphTestUtils.clearGraph(titanDao);
            initGraphForTest();
            isInitialized = true;
        }
    }

    @Test
    public void testAddComponentInstanceExternalRef(){
        Either<String, ActionStatus> addResult = externalReferenceOperation.addExternalReference(this.serviceVertexUuid, COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_4);
        assertThat(addResult.isLeft()).isEqualTo(true);

        //commit changes to titan
        final TitanOperationStatus commit = this.titanDao.commit();
        assertThat(commit).isEqualTo(TitanOperationStatus.OK);

        assertThat(getServiceExternalRefs()).contains(REF_1, REF_2, REF_3, REF_4);
    }

    @Test
    public void testGetComponentInstanceExternalRef(){
        assertThat(externalReferenceOperation.getExternalReferences(this.serviceVertexUuid, COMPONENT_ID, MONITORING_OBJECT_TYPE).left().value()).contains(REF_1, REF_2, REF_3, REF_5);
        assertThat(externalReferenceOperation.getExternalReferences(this.serviceVertexUuid, COMPONENT_ID, WORKFLOW_OBJECT_TYPE).left().value()).containsExactly(REF_6);
    }

    @Test
    public void testGetComponentInstanceExternalRefForNonExistingObjectId(){
        assertThat(externalReferenceOperation.getExternalReferences(this.serviceVertexUuid, COMPONENT_ID, MONITORING_OBJECT_TYPE).left().value()).contains(REF_1, REF_2, REF_3, REF_5);
        Either<List<String>, ActionStatus> getResult = externalReferenceOperation.getExternalReferences(this.serviceVertexUuid, COMPONENT_ID, "FAKE_OBJECT_TYPE");
        assertThat(getResult.left().value()).isEmpty();
    }

    @Test
    public void testDeleteComponentInstanceExternalRef(){
        //Test the precondition
        assertThat(getServiceExternalRefs()).contains(REF_5);

        //Remove REF 5
        Either<String, ActionStatus> deleteStatus = externalReferenceOperation.deleteExternalReference(this.serviceVertexUuid, COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_5);
        assertThat(deleteStatus.isLeft()).isEqualTo(true);

        //commit changes to titan
        final TitanOperationStatus commit = this.titanDao.commit();
        assertThat(commit).isEqualTo(TitanOperationStatus.OK);

        //Check that ref does not exist anymore
        assertThat(getServiceExternalRefs()).doesNotContain(REF_5).contains(REF_1, REF_2, REF_3);
    }

    @Test
    public void testUpdateComponentInstanceExternalRef(){
        //Test the precondition
        assertThat(getServiceExternalRefs()).contains(REF_5).doesNotContain(REF_4);

        //Update REF 5 with REF_4
        Either<String, ActionStatus> updateResult = externalReferenceOperation.updateExternalReference(this.serviceVertexUuid, COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_5, REF_4);

        assertThat(updateResult.isLeft()).isEqualTo(true);

        //commit changes to titan
        final TitanOperationStatus commit = this.titanDao.commit();
        assertThat(commit).isEqualTo(TitanOperationStatus.OK);

        //Check that ref does not exist anymore
        assertThat(getServiceExternalRefs()).doesNotContain(REF_5).contains(REF_1, REF_2, REF_3, REF_4);
    }

    private List<String> getServiceExternalRefs(){
        //Get service vertex
        final Either<GraphVertex, TitanOperationStatus> externalRefsVertexResult = this.titanDao.getChildVertex(this.serviceVertex, EdgeLabelEnum.EXTERNAL_REFS, JsonParseFlagEnum.ParseJson);
        assertThat(externalRefsVertexResult.isLeft()).isEqualTo(true);

        GraphVertex externalRefVertex = externalRefsVertexResult.left().value();

        //Get the full map
        Map<String, MapComponentInstanceExternalRefs> componentInstancesMap = (Map<String, MapComponentInstanceExternalRefs>) externalRefVertex.getJson();
        assertThat(componentInstancesMap).isNotNull();

        //Get Map of external refs by object type
        final MapComponentInstanceExternalRefs mapComponentInstanceExternalRefs = componentInstancesMap.get(COMPONENT_ID);

        //Get List of references
        //final List<String> externalRefsByObjectType = mapComponentInstanceExternalRefs.externalRefsByObjectType(objectType);
        final List<String> externalRefsByObjectType = mapComponentInstanceExternalRefs.getExternalRefsByObjectType(MONITORING_OBJECT_TYPE);

        return externalRefsByObjectType;
    }

    private void initGraphForTest() {
        //create a service and add 1 ref
        this.serviceVertex = GraphTestUtils.createServiceVertex(titanDao, new HashMap<>());
        this.serviceVertexUuid = this.serviceVertex.getUniqueId();

        //monitoring references
        externalReferenceOperation.addExternalReference(serviceVertexUuid, COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_1);
        externalReferenceOperation.addExternalReference(serviceVertexUuid, COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_2);
        externalReferenceOperation.addExternalReference(serviceVertexUuid, COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_3);
        externalReferenceOperation.addExternalReference(serviceVertexUuid, COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_5);

        //workflow references
        externalReferenceOperation.addExternalReference(serviceVertexUuid, COMPONENT_ID, WORKFLOW_OBJECT_TYPE, REF_6);

        final TitanOperationStatus commit = this.titanDao.commit();
        assertThat(commit).isEqualTo(TitanOperationStatus.OK);
    }
}