package org.openecomp.sdc.be.model.jsontitan.operations;

import fj.data.Either;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CompositionDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.jsontitan.enums.JsonConstantKeysEnum;
import org.openecomp.sdc.be.model.jsontitan.utils.GraphTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by yavivi on 21/03/2018.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = {"classpath:application-context-test.xml", "classpath:healing-context-test.xml"})
public class ArchiveOperationTest extends ModelTestBase {

    private static final String CI_UID_RES1_CP = "cp_uid";
    private static final String CI_UID_RES2_VL = "vl_uid";
    private static final String CI_UID_SVC_PROXY = "svc_proxy_uid";

    @Resource
    private ArchiveOperation archiveOperation;

    @Resource
    private TitanDao titanDao;

    private boolean isInitialized;

    private GraphVertex serviceVertex1;
    private GraphVertex archivedVertex1;

    GraphVertex archiveVertex;
    GraphVertex catalogVertex;

    private GraphVertex serviceVertex1_0;
    private GraphVertex serviceVertex1_1;
    private GraphVertex serviceVertex2_0;
    private GraphVertex serviceVertex3_0;
    private GraphVertex serviceVertex3_1;

    private GraphVertex serviceVertex0_1;
    private GraphVertex serviceVertex0_2;
    private GraphVertex serviceVertex0_3;
    private GraphVertex serviceVertex0_4;
    private GraphVertex serviceVertex0_5;

    //Composition Elements
    private GraphVertex compositionService;
    private GraphVertex compositionResource1;
    private GraphVertex compositionResource2;
    private GraphVertex compositionServiceProxy;
    private GraphVertex compositionAnotherService;

    //For VSP Archive Notification
    private GraphVertex vfResource0_1;
    private GraphVertex vfResource0_2;
    private GraphVertex vfResource1_0;
    private String csarUuid = "123456789";;

    @BeforeClass
    public static void initTest(){
        ModelTestBase.init();
    }

    @Before
    public void beforeTest() {
        if (!isInitialized) {
            GraphTestUtils.clearGraph(titanDao);
            initGraphForTest();
            isInitialized = true;
        }
    }

    @Test
    public void testArchiveComponentSingleVersion(){
        String componentId = serviceVertex1.getUniqueId();
        Either<List<String>, ActionStatus> actionStatus = this.archiveOperation.archiveComponent(componentId);
        assertThat(actionStatus.isLeft()).isTrue();
        assertArchived(serviceVertex1.getUniqueId());
    }

    @Test
    public void testArchiveComponentFailsWhenInCheckoutSingleVersion(){
        checkoutComponent(serviceVertex1);
        String componentId = serviceVertex1.getUniqueId();
        Either<List<String>, ActionStatus> actionStatus = this.archiveOperation.archiveComponent(componentId);
        assertThat(actionStatus.isLeft()).isFalse();
        assertThat(actionStatus.right().value()).isEqualTo(ActionStatus.INVALID_SERVICE_STATE);
    }

    @Test
    public void testArchiveWithWrongId() {
        Either<List<String>, ActionStatus> actionStatus = this.archiveOperation.archiveComponent("fakeComponentId");
        assertThat(actionStatus.isLeft()).isFalse();
        assertThat(actionStatus.right().value()).isEqualTo(ActionStatus.RESOURCE_NOT_FOUND);
    }

    @Test
    public void testAlreadyArchived() {
        Either<List<String>, ActionStatus> actionStatus = this.archiveOperation.archiveComponent(archivedVertex1.getUniqueId());
        assertThat(actionStatus.isLeft()).isTrue();
        assertThat(actionStatus.left().value()).containsExactly(archivedVertex1.getUniqueId());
    }

    @Test
    public void testScenario2_archive_1_0(){
        Either<List<String>, ActionStatus> actionStatus = this.archiveOperation.archiveComponent(serviceVertex1_0.getUniqueId());
        assertThat(actionStatus.isLeft()).isTrue();
        assertThat(actionStatus.left().value()).containsExactlyInAnyOrder(serviceVertex1_0.getUniqueId(), serviceVertex1_1.getUniqueId());
        assertArchived(serviceVertex1_0.getUniqueId());
        assertArchived(serviceVertex1_1.getUniqueId());
    }

    @Test
    public void testScenario2_archive_1_1(){
        Either<List<String>, ActionStatus> actionStatus = this.archiveOperation.archiveComponent(serviceVertex1_1.getUniqueId());
        assertThat(actionStatus.left().value()).containsExactlyInAnyOrder(serviceVertex1_0.getUniqueId(), serviceVertex1_1.getUniqueId());
        assertArchived(serviceVertex1_0.getUniqueId());
        assertArchived(serviceVertex1_1.getUniqueId());
    }

    @Test
    public void testScenario4_oneLowOneHighestVersion(){
        Either<List<String>, ActionStatus> actionStatus = this.archiveOperation.archiveComponent(serviceVertex0_2.getUniqueId());
        assertThat(actionStatus.left().value()).containsExactlyInAnyOrder(serviceVertex0_2.getUniqueId(), serviceVertex0_1.getUniqueId(), serviceVertex0_3.getUniqueId(), serviceVertex0_4.getUniqueId(), serviceVertex0_5.getUniqueId());
        assertArchived(serviceVertex0_1.getUniqueId());
        assertArchived(serviceVertex0_2.getUniqueId());
        assertArchived(serviceVertex0_3.getUniqueId());
        assertArchived(serviceVertex0_4.getUniqueId());
        assertArchived(serviceVertex0_5.getUniqueId());

        actionStatus = this.archiveOperation.restoreComponent(serviceVertex0_2.getUniqueId());
        assertThat(actionStatus.isLeft()).isTrue();
        assertThat(actionStatus.left().value()).containsExactlyInAnyOrder(serviceVertex0_2.getUniqueId(), serviceVertex0_1.getUniqueId(), serviceVertex0_3.getUniqueId(), serviceVertex0_4.getUniqueId(), serviceVertex0_5.getUniqueId());
    }


    /////////////// Continue Here //////////////////
    @Test
    public void testScenario4_archiveFromNonHighest(){
        Either<List<String>, ActionStatus> actionStatus = this.archiveOperation.archiveComponent(serviceVertex0_2.getUniqueId());
        assertArchived(serviceVertex0_1.getUniqueId());
        assertArchived(serviceVertex0_2.getUniqueId());
        assertArchived(serviceVertex0_3.getUniqueId());
        assertArchived(serviceVertex0_4.getUniqueId());
        assertArchived(serviceVertex0_5.getUniqueId());

        actionStatus = this.archiveOperation.restoreComponent(serviceVertex0_3.getUniqueId());
        assertRestored(serviceVertex0_1.getUniqueId());
        assertRestored(serviceVertex0_2.getUniqueId());
        assertRestored(serviceVertex0_3.getUniqueId());
        assertRestored(serviceVertex0_4.getUniqueId());
        assertRestored(serviceVertex0_5.getUniqueId());
    }

    @Test
    public void testArchiveFailsWhenHighestVersionIsInCheckoutState(){
        checkoutComponent(serviceVertex0_5);
        Either<List<String>, ActionStatus> actionStatus = this.archiveOperation.archiveComponent(serviceVertex0_2.getUniqueId());
        assertThat(actionStatus.right().value()).isEqualTo(ActionStatus.INVALID_SERVICE_STATE);
    }

    @Test
    public void testScenario3_archive_3_0(){
        Either<List<String>, ActionStatus> actionStatus = this.archiveOperation.archiveComponent(serviceVertex3_0.getUniqueId());
        assertArchived(serviceVertex3_0.getUniqueId());
        assertArchived(serviceVertex3_1.getUniqueId());
        assertArchivedProps(serviceVertex2_0.getUniqueId());
    }

    @Test
    public void testArchivedOriginsCalculation(){

        //Archive the CP resource
        this.archiveOperation.archiveComponent(this.compositionResource1.getUniqueId());
        this.archiveOperation.archiveComponent(this.compositionServiceProxy.getUniqueId());

        List<String> ciWithArchivedOrigins = this.archiveOperation.setArchivedOriginsFlagInComponentInstances(this.compositionService);

        //Validate that method returns the CI of CP
        assertThat(ciWithArchivedOrigins).containsExactlyInAnyOrder(CI_UID_RES1_CP, CI_UID_SVC_PROXY);

        Map<String, CompositionDataDefinition> compositionsJson = (Map<String, CompositionDataDefinition>) this.compositionService.getJson();

        assertThat(compositionsJson).isNotNull();
        assertThat(compositionsJson.get(JsonConstantKeysEnum.COMPOSITION.getValue())).isNotNull();

        CompositionDataDefinition composition = compositionsJson.get(JsonConstantKeysEnum.COMPOSITION.getValue());

        //Get all component instances from composition
        Map<String, ComponentInstanceDataDefinition> componentInstances = composition.getComponentInstances();
        for (ComponentInstanceDataDefinition ci : componentInstances.values()) {
            //Verify that exactly 2 CIs are marked as archived
            if (ci.getUniqueId().equals(CI_UID_RES1_CP) || ci.getUniqueId().equals(CI_UID_SVC_PROXY)) {
                assertThat(ci.isOriginArchived()).isTrue();
            }
        }

    }

    @Test
    public void testNoArchivedOriginsCalculation(){
        List<String> ciWithArchivedOrigins = this.archiveOperation.setArchivedOriginsFlagInComponentInstances(this.compositionService);

        //Validate that method returns the CI of CP
        assertThat(ciWithArchivedOrigins).isEmpty();
    }

    @Test
    public void testOnVspArchivedAndRestored(){
        this.archiveOperation.onVspArchived(csarUuid);
        //assertOnCommit();

        assertOnVspArchived(true);

        this.archiveOperation.onVspRestored(csarUuid);
        //assertOnCommit();
        assertOnVspArchived(false);

        //Not Found CSAR UUID
        ActionStatus result = this.archiveOperation.onVspRestored("fakeUuid");
        //assertOnCommit();
        assertThat(result).isEqualTo(ActionStatus.RESOURCE_NOT_FOUND);
    }

    private void assertOnVspArchived(boolean expectedValue) {
        GraphVertex v = titanDao.getVertexById(vfResource0_1.getUniqueId()).left().value();
        assertThat(v.getMetadataProperty(GraphPropertyEnum.IS_VSP_ARCHIVED)).isEqualTo(expectedValue);

        v = titanDao.getVertexById(vfResource0_2.getUniqueId()).left().value();
        assertThat(v.getMetadataProperty(GraphPropertyEnum.IS_VSP_ARCHIVED)).isEqualTo(expectedValue);

        v = titanDao.getVertexById(vfResource1_0.getUniqueId()).left().value();
        assertThat(v.getMetadataProperty(GraphPropertyEnum.IS_VSP_ARCHIVED)).isEqualTo(expectedValue);
    }

    /**************************
     * Utility Methods
     *************************/

    private void checkoutComponent(GraphVertex serviceVertex0_5) {
        Either<GraphVertex, TitanOperationStatus> vE = titanDao.getVertexById(serviceVertex0_5.getUniqueId());
        GraphVertex v = vE.left().value();
        v.addMetadataProperty(GraphPropertyEnum.STATE, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        v.setJsonMetadataField(JsonPresentationFields.LIFECYCLE_STATE, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        titanDao.updateVertex(v);
        assertOnCommit();
    }

    private void assertOnCommit(){
        final TitanOperationStatus commit = this.titanDao.commit();
        assertThat(commit).isEqualTo(TitanOperationStatus.OK);
    }

    private void assertArchived(String componentUniqueId) {
        assertArchivedOrRestored(ArchiveOperation.Action.ARCHIVE, componentUniqueId);
    }

    private void assertRestored(String componentUniqueId) {
        assertArchivedOrRestored(ArchiveOperation.Action.RESTORE, componentUniqueId);
    }

    private void assertArchivedOrRestored(ArchiveOperation.Action action,  String componentUniqueId) {
        GraphVertex v = titanDao.getVertexById(componentUniqueId).left().value();

        EdgeLabelEnum requiredEdge = action == ArchiveOperation.Action.ARCHIVE ? EdgeLabelEnum.ARCHIVE_ELEMENT : EdgeLabelEnum.CATALOG_ELEMENT;
        EdgeLabelEnum otherEdge = action == ArchiveOperation.Action.ARCHIVE ? EdgeLabelEnum.CATALOG_ELEMENT : EdgeLabelEnum.ARCHIVE_ELEMENT;

        GraphVertex parent = null;
        Either<GraphVertex, TitanOperationStatus> otherLookup = null;
        Boolean isHighest = (Boolean) v.getMetadataProperty(GraphPropertyEnum.IS_HIGHEST_VERSION);
        if (isHighest != null && isHighest) {
            //Highest version are linked to Archive/Catalog Root
            parent = titanDao.getParentVertex(v, requiredEdge, JsonParseFlagEnum.NoParse).left().value();
            otherLookup = titanDao.getParentVertex(v, otherEdge, JsonParseFlagEnum.NoParse);
            assertThat(otherLookup.isRight()).isTrue();           //Verify that component is not linked to Catalog/Archive Root
            assertThat(parent.getUniqueId()).isEqualTo(action == ArchiveOperation.Action.ARCHIVE ? this.archiveVertex.getUniqueId() : this.catalogVertex.getUniqueId()); //Verify that parent is indeed Archive Root
        }

        assertArchivedOrRestoredProps(action, v);
    }

    private void assertArchivedProps(String uniqueId) {
        GraphVertex v =
                titanDao.getVertexById(uniqueId).left().value();
        assertArchivedOrRestoredProps(ArchiveOperation.Action.ARCHIVE, v);
    }

    private void assertRestoredProps(String uniqueId) {
        GraphVertex v =
                titanDao.getVertexById(uniqueId).left().value();
        assertArchivedOrRestoredProps(ArchiveOperation.Action.RESTORE, v);
    }

    private void assertArchivedOrRestoredProps(ArchiveOperation.Action action, GraphVertex v) {
        Object isArchived = v.getMetadataProperty(GraphPropertyEnum.IS_ARCHIVED);
        Object archiveTime = v.getMetadataProperty(GraphPropertyEnum.ARCHIVE_TIME);
        assertThat(isArchived).isNotNull().isEqualTo(action == ArchiveOperation.Action.ARCHIVE ? true : false);
        assertThat(archiveTime).isNotNull();
    }

    /*******************************
     * Preperation Methods
     *******************************/
    private void initGraphForTest() {
        //Create Catalog Root
        this.catalogVertex = GraphTestUtils.createRootCatalogVertex(titanDao);
        //Create Archive Root
        this.archiveVertex = GraphTestUtils.createRootArchiveVertex(titanDao);

        createScenario1_SingleVersionNode();
        createScenario2_TwoHighestVersions();
        createScenario3_TwoHighestVersionsOneLowest();
        createMiscServices();
        createServiceCompositionForCalculatingArchivedOrigins();
        createScenario4_1Highest4LowestVersions();
        createResourcesForArchivedVsp();

        assertOnCommit();
    }

    private void createScenario1_SingleVersionNode() {
        //Create Service for Scenario 1 Tests (1 Service)
        this.serviceVertex1 = GraphTestUtils.createServiceVertex(titanDao, propsForHighestVersion());

        //Connect Service to Catalog Root
        titanDao.createEdge(catalogVertex, serviceVertex1, EdgeLabelEnum.CATALOG_ELEMENT, null);
    }

    private void createScenario2_TwoHighestVersions() {
        //Create Service for Scenario 2 Tests (1 Service)
        this.serviceVertex1_0 = GraphTestUtils.createServiceVertex(titanDao, propsForHighestVersion());
        this.serviceVertex1_1 = GraphTestUtils.createServiceVertex(titanDao, propsForHighestVersion());

        titanDao.createEdge(serviceVertex1_0, serviceVertex1_1, EdgeLabelEnum.VERSION, null);

        //Connect 1.0 and 1.1 to Catalog Root
        titanDao.createEdge(catalogVertex, serviceVertex1_0, EdgeLabelEnum.CATALOG_ELEMENT, null);
        titanDao.createEdge(catalogVertex, serviceVertex1_1, EdgeLabelEnum.CATALOG_ELEMENT, null);
    }

    private void createScenario3_TwoHighestVersionsOneLowest() {
        //Create Service for Scenario 1 Tests (1 Service)
        this.serviceVertex2_0 = GraphTestUtils.createServiceVertex(titanDao, propsForNonHighestVersion()); //NonHighestVersion
        this.serviceVertex3_0 = GraphTestUtils.createServiceVertex(titanDao, propsForHighestVersion());
        this.serviceVertex3_1 = GraphTestUtils.createServiceVertex(titanDao, propsForHighestVersion());

        //Connect version edges
        titanDao.createEdge(serviceVertex2_0, serviceVertex3_0, EdgeLabelEnum.VERSION, null);
        titanDao.createEdge(serviceVertex3_0, serviceVertex3_1, EdgeLabelEnum.VERSION, null);

        //Connect 3.0 and 3.1 to Catalog Root
        titanDao.createEdge(catalogVertex, serviceVertex3_0, EdgeLabelEnum.CATALOG_ELEMENT, null);
        titanDao.createEdge(catalogVertex, serviceVertex3_1, EdgeLabelEnum.CATALOG_ELEMENT, null);
    }

    private void createScenario4_1Highest4LowestVersions() {
        //2 Lowest version only
        this.serviceVertex0_1 = GraphTestUtils.createServiceVertex(titanDao, propsForNonHighestVersion());
        this.serviceVertex0_2 = GraphTestUtils.createServiceVertex(titanDao, propsForNonHighestVersion());
        this.serviceVertex0_3 = GraphTestUtils.createServiceVertex(titanDao, propsForNonHighestVersion());
        this.serviceVertex0_4 = GraphTestUtils.createServiceVertex(titanDao, propsForNonHighestVersion());
        this.serviceVertex0_5 = GraphTestUtils.createServiceVertex(titanDao, propsForHighestVersion());

        titanDao.createEdge(serviceVertex0_1, serviceVertex0_2, EdgeLabelEnum.VERSION, null);
        titanDao.createEdge(serviceVertex0_2, serviceVertex0_3, EdgeLabelEnum.VERSION, null);
        titanDao.createEdge(serviceVertex0_3, serviceVertex0_4, EdgeLabelEnum.VERSION, null);
        titanDao.createEdge(serviceVertex0_4, serviceVertex0_5, EdgeLabelEnum.VERSION, null);

        titanDao.createEdge(catalogVertex, serviceVertex0_5, EdgeLabelEnum.CATALOG_ELEMENT, null);
    }

    private void createResourcesForArchivedVsp(){
        Map<GraphPropertyEnum, Object> vfPropsNonHighest = propsForNonHighestVersion();
        Map<GraphPropertyEnum, Object> vfPropsHighest = propsForNonHighestVersion();

        vfPropsNonHighest.put(GraphPropertyEnum.CSAR_UUID, csarUuid);
        vfPropsNonHighest.put(GraphPropertyEnum.IS_VSP_ARCHIVED, false);
        vfPropsHighest.put(GraphPropertyEnum.CSAR_UUID, csarUuid);
        vfPropsHighest.put(GraphPropertyEnum.IS_VSP_ARCHIVED, false);

        this.vfResource0_1 = GraphTestUtils.createResourceVertex(titanDao, vfPropsNonHighest, ResourceTypeEnum.VF);
        this.vfResource0_2 = GraphTestUtils.createResourceVertex(titanDao, vfPropsNonHighest, ResourceTypeEnum.VF);
        this.vfResource1_0 = GraphTestUtils.createResourceVertex(titanDao, vfPropsHighest, ResourceTypeEnum.VF);

        titanDao.createEdge(vfResource0_1, vfResource0_2, EdgeLabelEnum.VERSION, null);
        titanDao.createEdge(vfResource0_2, vfResource1_0, EdgeLabelEnum.VERSION, null);
    }

    private void createMiscServices() {
        //Create Service for Scenario 1 Tests (1 Service)
        this.archivedVertex1 = GraphTestUtils.createServiceVertex(titanDao, new HashMap<>());

        //Connect Service to Catalog Root
        titanDao.createEdge(archiveVertex, archivedVertex1, EdgeLabelEnum.ARCHIVE_ELEMENT, null);
    }

    private void createServiceCompositionForCalculatingArchivedOrigins(){
        //Service that point to another service in composition
        this.compositionService = GraphTestUtils.createServiceVertex(titanDao, propsForHighestVersion());
        this.compositionAnotherService = GraphTestUtils.createServiceVertex(titanDao, propsForHighestVersion());

        this.compositionResource1 = GraphTestUtils.createResourceVertex(titanDao, propsForHighestVersion(), ResourceTypeEnum.CP);
        this.compositionResource2 = GraphTestUtils.createResourceVertex(titanDao, propsForHighestVersion(), ResourceTypeEnum.VL);
        this.compositionServiceProxy = GraphTestUtils.createResourceVertex(titanDao, propsForHighestVersion(), ResourceTypeEnum.ServiceProxy);

        titanDao.createEdge(compositionService, compositionResource1, EdgeLabelEnum.INSTANCE_OF, null);
        titanDao.createEdge(compositionService, compositionResource2, EdgeLabelEnum.INSTANCE_OF, null);
        titanDao.createEdge(compositionService, compositionServiceProxy, EdgeLabelEnum.INSTANCE_OF, null);
        titanDao.createEdge(compositionService, compositionAnotherService, EdgeLabelEnum.PROXY_OF, null);

        createAndAttachCompositionJson(compositionService);
    }

    private void createAndAttachCompositionJson(GraphVertex compositionService) {
        //Full composition json
        Map<String, CompositionDataDefinition> compositions = new HashMap<>();
        //Single composition data
        CompositionDataDefinition composition = new CompositionDataDefinition();
        //Instances Map
        Map<String, ComponentInstanceDataDefinition> instances = new HashMap<>();

        //Prepare Instances Map
        ComponentInstanceDataDefinition instance = new ComponentInstanceDataDefinition();
        instance.setUniqueId(CI_UID_RES1_CP);
        instance.setComponentUid(compositionResource1.getUniqueId());
        instances.put(CI_UID_RES1_CP, instance);

        instance = new ComponentInstanceDataDefinition();
        instance.setUniqueId(CI_UID_RES2_VL);
        instance.setComponentUid(compositionResource2.getUniqueId());
        instances.put(CI_UID_RES2_VL, instance);

        instance = new ComponentInstanceDataDefinition();
        instance.setUniqueId(CI_UID_SVC_PROXY);
        instance.setComponentUid(compositionServiceProxy.getUniqueId());
        instances.put(CI_UID_SVC_PROXY, instance);
        
        //Add Instances to Composition
        composition.setComponentInstances(instances);
        //Add to full composition
        compositions.put(JsonConstantKeysEnum.COMPOSITION.getValue(), composition);
        //Add Full Json to vertex
        compositionService.setJson(compositions);
        //System.out.println(JsonParserUtils.toJson(compositions));
        titanDao.updateVertex(compositionService);
    }

    private Map<GraphPropertyEnum, Object> propsForHighestVersion(){
        Map<GraphPropertyEnum, Object> props = new HashMap();
        props.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        props.put(GraphPropertyEnum.STATE, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        return props;
    }

    private Map<GraphPropertyEnum, Object> propsForNonHighestVersion(){
        Map<GraphPropertyEnum, Object> props = new HashMap();
        props.put(GraphPropertyEnum.IS_HIGHEST_VERSION, false);
        props.put(GraphPropertyEnum.STATE, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        return props;
    }

}