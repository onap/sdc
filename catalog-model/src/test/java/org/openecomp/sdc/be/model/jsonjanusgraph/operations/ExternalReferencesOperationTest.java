package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import fj.data.Either;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.datatypes.elements.MapComponentInstanceExternalRefs;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.GraphTestUtils;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.IdMapper;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by yavivi on 26/01/2018.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
public class ExternalReferencesOperationTest extends ModelTestBase {

    private static final String COMPONENT_ID = "ci-MyComponentName";
    private static final String COMPONENT2_ID = "ci-MyComponentName2";
    private static final String MONITORING_OBJECT_TYPE = "monitoring";
    private static final String WORKFLOW_OBJECT_TYPE = "workflow";
    private static final String REF_1 = "ref1";
    private static final String REF_2 = "ref2";
    private static final String REF_3 = "ref3";
    private static final String REF_4 = "ref4";
    private static final String REF_5 = "ref5";
    //workflow
    private static final String REF_6 = "ref6";

    @Resource
    private ExternalReferencesOperation externalReferenceOperation;

    @Resource
    private JanusGraphDao janusGraphDao;

    private boolean isInitialized;

    private GraphVertex serviceVertex;
    private GraphVertex serviceVertex2;
    private GraphVertex serviceVertex3;

    private String serviceVertexUuid;
    private String serviceVertex2Uuid;
    private String serviceVertex3Uuid;

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
            GraphTestUtils.clearGraph(janusGraphDao);
            initGraphForTest();
            isInitialized = true;
        }
    }

    @Test
    public void testAddComponentInstanceExternalRef(){
        Either<String, ActionStatus> addResult = externalReferenceOperation.addExternalReference(this.serviceVertexUuid, COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_4);
        assertThat(addResult.isLeft()).isEqualTo(true);

        //commit changes to janusgraph
        final JanusGraphOperationStatus commit = this.janusGraphDao.commit();
        assertThat(commit).isEqualTo(JanusGraphOperationStatus.OK);

        assertThat(getServiceExternalRefs()).contains(REF_1, REF_2, REF_3, REF_4);
    }

    @Test
    public void testAddExternalReferences_success() {
        Map<String, List<String>> refsMap = Collections.singletonMap(MONITORING_OBJECT_TYPE, Arrays.asList(REF_1, REF_2));
        externalReferenceOperation.addAllExternalReferences(serviceVertex3Uuid, COMPONENT_ID, refsMap);
        Map<String, List<String>> allExternalReferences = externalReferenceOperation.getAllExternalReferences(serviceVertex3Uuid, COMPONENT_ID);
        assertThat(allExternalReferences.size()).isEqualTo(1);
        assertThat(allExternalReferences).flatExtracting(MONITORING_OBJECT_TYPE).containsExactly(REF_1, REF_2);
        externalReferenceOperation.addAllExternalReferences(serviceVertex3Uuid, COMPONENT2_ID, refsMap);
        Map<String, List<String>> allExternalReferences2 = externalReferenceOperation.getAllExternalReferences(serviceVertex3Uuid, COMPONENT2_ID);
        assertThat(allExternalReferences2.size()).isEqualTo(1);
        assertThat(allExternalReferences2).flatExtracting(MONITORING_OBJECT_TYPE).containsExactly(REF_1, REF_2);
    }

    @Test
    public void testGetAllCIExternalRefs_success() {
        Map<String, List<String>> allExternalReferences = externalReferenceOperation.getAllExternalReferences(serviceVertexUuid, COMPONENT_ID);
        assertThat(allExternalReferences.size()).isEqualTo(2);
        assertThat(allExternalReferences).flatExtracting(WORKFLOW_OBJECT_TYPE).containsExactly(REF_6);
        assertThat(allExternalReferences).flatExtracting(MONITORING_OBJECT_TYPE).containsExactly(REF_1, REF_2, REF_3, REF_5);
    }

    @Test
    public void testGetAllCIExternalRefs_noRefsExist() {
        Map<String, List<String>> allExternalReferences = externalReferenceOperation.getAllExternalReferences(serviceVertex2Uuid, COMPONENT_ID);
        assertThat(allExternalReferences.size()).isZero();
    }

    @Test
    public void testGetAllCIExternalRefs_noSuchComponentInstance() {
        Map<String, List<String>> allExternalReferences = externalReferenceOperation.getAllExternalReferences(serviceVertex2Uuid, "FAKE");
        assertThat(allExternalReferences.size()).isZero();
    }

    @Test(expected=StorageException.class)
    public void testGetAllCIExternalRefs_nonExitingService_throwsException() {
        externalReferenceOperation.getAllExternalReferences("FAKE", COMPONENT_ID);
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

        //commit changes to janusgraph
        final JanusGraphOperationStatus commit = this.janusGraphDao.commit();
        assertThat(commit).isEqualTo(JanusGraphOperationStatus.OK);

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

        //commit changes to janusgraph
        final JanusGraphOperationStatus commit = this.janusGraphDao.commit();
        assertThat(commit).isEqualTo(JanusGraphOperationStatus.OK);

        //Check that ref does not exist anymore
        assertThat(getServiceExternalRefs()).doesNotContain(REF_5).contains(REF_1, REF_2, REF_3, REF_4);
    }

    private List<String> getServiceExternalRefs(){
        //Get service vertex
        final Either<GraphVertex, JanusGraphOperationStatus> externalRefsVertexResult = this.janusGraphDao
            .getChildVertex(this.serviceVertex, EdgeLabelEnum.EXTERNAL_REFS, JsonParseFlagEnum.ParseJson);
        assertThat(externalRefsVertexResult.isLeft()).isEqualTo(true);

        GraphVertex externalRefVertex = externalRefsVertexResult.left().value();

        //Get the full map
        Map<String, MapComponentInstanceExternalRefs> componentInstancesMap = (Map<String, MapComponentInstanceExternalRefs>) externalRefVertex.getJson();
        assertThat(componentInstancesMap).isNotNull();

        //Get Map of external refs by object type
        final MapComponentInstanceExternalRefs mapComponentInstanceExternalRefs = componentInstancesMap.get(COMPONENT_ID);

        //Get List of references
        //final List<String> externalRefsByObjectType = mapComponentInstanceExternalRefs.externalRefsByObjectType(objectType);

        return mapComponentInstanceExternalRefs.getExternalRefsByObjectType(MONITORING_OBJECT_TYPE);
    }

    private void initGraphForTest() {
        //create a service
        this.serviceVertex = GraphTestUtils.createServiceVertex(janusGraphDao, new HashMap<>());
        this.serviceVertexUuid = this.serviceVertex.getUniqueId();

        //monitoring references
        externalReferenceOperation.addExternalReference(serviceVertexUuid, COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_1);
        externalReferenceOperation.addExternalReference(serviceVertexUuid, COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_2);
        externalReferenceOperation.addExternalReference(serviceVertexUuid, COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_3);
        externalReferenceOperation.addExternalReference(serviceVertexUuid, COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_5);

        //workflow references
        externalReferenceOperation.addExternalReference(serviceVertexUuid, COMPONENT_ID, WORKFLOW_OBJECT_TYPE, REF_6);

        //create a service without refs
        serviceVertex2 = GraphTestUtils.createServiceVertex(janusGraphDao, new HashMap<>());
        serviceVertex2Uuid = serviceVertex2.getUniqueId();

        //create a service for adding all references
        serviceVertex3 = GraphTestUtils.createServiceVertex(janusGraphDao, new HashMap<>());
        serviceVertex3Uuid = serviceVertex3.getUniqueId();

        final JanusGraphOperationStatus commit = this.janusGraphDao.commit();
        assertThat(commit).isEqualTo(JanusGraphOperationStatus.OK);
    }
}