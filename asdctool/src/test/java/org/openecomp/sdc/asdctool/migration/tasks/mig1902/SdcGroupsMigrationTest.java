package org.openecomp.sdc.asdctool.migration.tasks.mig1902;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GroupTypeOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SdcGroupsMigrationTest {

    @Mock
    private JanusGraphDao janusGraphDao;
    @Mock
    private GroupTypeOperation groupTypeOperation;
    @Mock
    private GraphVertex topologyTemplateVertex;
    @Mock
    private GraphVertex groupsVertex;

    @InjectMocks
    private SdcGroupsMigration groupsMigration;

    @Before
    public void setUp() {
        groupsMigration = new SdcGroupsMigration(janusGraphDao, groupTypeOperation);
        when(janusGraphDao.getVertexById(any())).thenReturn(Either.left(topologyTemplateVertex));
    }

    @Test
    public void handleOneContainerWhenErrorHappened() {
        when(janusGraphDao.getChildVertex(any(GraphVertex.class), eq(EdgeLabelEnum.GROUPS), eq(JsonParseFlagEnum.ParseAll)))
                .thenReturn(Either.right(JanusGraphOperationStatus.MATCH_NOT_FOUND));
        StorageOperationStatus status = groupsMigration.handleOneContainer(topologyTemplateVertex);
        assertEquals(StorageOperationStatus.MATCH_NOT_FOUND, status);
    }

    @Test
    public void handleOneContainerWhenNoGroups() {
        when(janusGraphDao.getChildVertex(any(GraphVertex.class), eq(EdgeLabelEnum.GROUPS), eq(JsonParseFlagEnum.ParseAll)))
                .thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));
        StorageOperationStatus status = groupsMigration.handleOneContainer(topologyTemplateVertex);
        assertEquals(StorageOperationStatus.OK, status);
    }

    @Test
    public void handleOneContainerWhenGroupsShouldNotBeUpdated() {
        when(janusGraphDao.getChildVertex(any(GraphVertex.class), eq(EdgeLabelEnum.GROUPS), eq(JsonParseFlagEnum.ParseAll)))
                .thenReturn(Either.left(groupsVertex));

        doReturn(buildGroupInstancesMap(new String[] {"org.openecomp.groups.heat.HeatStack", "org.openecomp.groups.VfModule"}, new String[]{}))
                .when(groupsVertex).getJson();
        StorageOperationStatus status = groupsMigration.handleOneContainer(topologyTemplateVertex);
        assertEquals(StorageOperationStatus.OK, status);
        verify(janusGraphDao, times(0)).commit();

    }

    @Test
    public void handleOneContainerWhenGroupsShouldBeUpdated() {
        mockLatestGroupMapCreating();
        mockUpgradeHappyFlow();
        StorageOperationStatus status = groupsMigration.handleOneContainer(topologyTemplateVertex);
        assertEquals(StorageOperationStatus.OK, status);
        verify(janusGraphDao, times(1)).commit();
        verify(janusGraphDao, times(0)).rollback();
    }

    private void mockUpgradeHappyFlow() {
        when(janusGraphDao.getChildVertex(any(GraphVertex.class), eq(EdgeLabelEnum.GROUPS), eq(JsonParseFlagEnum.ParseAll)))
                .thenReturn(Either.left(groupsVertex));
        when(janusGraphDao.updateVertex(any(GraphVertex.class))).thenReturn(Either.left(groupsVertex));
        when(janusGraphDao.commit()).thenReturn(JanusGraphOperationStatus.OK);
        doReturn(buildGroupInstancesMap(new String[] {"org.openecomp.groups.NetworkCollection", "org.openecomp.groups.VfcInstanceGroup"},
                new String[] {"old1", "old2"}))
                .when(groupsVertex).getJson();
    }

    @Test
    public void handleOneContainerWhenGroupsAlreadyUpdated() {
        mockLatestGroupMapCreating();
        when(janusGraphDao.getChildVertex(any(GraphVertex.class), eq(EdgeLabelEnum.GROUPS), eq(JsonParseFlagEnum.ParseAll)))
                .thenReturn(Either.left(groupsVertex));
        doReturn(buildGroupInstancesMap(new String[] {"org.openecomp.groups.NetworkCollection"},
                new String[] {"a", "b", "c", "d"}))
                .when(groupsVertex).getJson();
        StorageOperationStatus status = groupsMigration.handleOneContainer(topologyTemplateVertex);
        assertEquals(StorageOperationStatus.OK, status);
        verify(janusGraphDao, times(0)).commit();
        verify(janusGraphDao, times(1)).rollback();
    }

    @Test
    public void handleOneContainerWhenExceptionIsThrown() {
        when(janusGraphDao.getChildVertex(any(GraphVertex.class), eq(EdgeLabelEnum.GROUPS), eq(JsonParseFlagEnum.ParseAll)))
                .thenThrow(new RuntimeException());
        StorageOperationStatus status = groupsMigration.handleOneContainer(topologyTemplateVertex);
        assertEquals(StorageOperationStatus.GENERAL_ERROR, status);
        verify(janusGraphDao, times(0)).commit();
        verify(janusGraphDao, times(1)).rollback();

    }

    // A temp remark for this test - following Commit hash:    08595ad21b0c409c69e3902232f5575963199e3e [ASDC-641] – Migration workaround for deployment artifact timeout. Reviewer: Lior.
//    @Test
//    public void migrateWhenExceptionIsThrown() {
//        List<GraphVertex> vertexList = new ArrayList<>();
//        vertexList.add(topologyTemplateVertex);
//        mockLatestGroupMapCreating();
//        when(janusGraphDao.getByCriteria(eq(VertexTypeEnum.TOPOLOGY_TEMPLATE), eq(null), anyMap(), eq(JsonParseFlagEnum.ParseAll)))
//                .thenReturn(Either.left(vertexList));
//        when(janusGraphDao.getChildVertex(any(GraphVertex.class), eq(EdgeLabelEnum.GROUPS), eq(JsonParseFlagEnum.ParseAll)))
//                .thenThrow(new RuntimeException());
//        assertEquals(MigrationResult.MigrationStatus.FAILED, groupsMigration.migrate().getMigrationStatus());
//        verify(janusGraphDao, times(0)).commit();
//        verify(janusGraphDao, times(1)).rollback();
//
//    }

    @Test
    public void migrateWhenGroupsShouldBeUpdated() {
        List<GraphVertex> vertexList = new ArrayList<>();
        vertexList.add(topologyTemplateVertex);
        mockLatestGroupMapCreating();
        when(janusGraphDao.getByCriteria(eq(VertexTypeEnum.TOPOLOGY_TEMPLATE), eq(null), anyMap(), eq(JsonParseFlagEnum.ParseAll)))
                .thenReturn(Either.left(vertexList));
        mockUpgradeHappyFlow();

        assertEquals(MigrationResult.MigrationStatus.COMPLETED, groupsMigration.migrate().getMigrationStatus());
        verify(janusGraphDao, times(1)).commit();
        verify(janusGraphDao, times(0)).rollback();
    }


    private Map<String, GroupDataDefinition> buildGroupInstancesMap(String[] groupTypes, String [] propertyNames) {
        Map<String, GroupDataDefinition> groupsMap = new HashMap<>();
        for (String type: groupTypes) {
            GroupDataDefinition gr = new GroupDataDefinition();
            gr.setType(type);

            gr.setProperties(createInstanceProperties(propertyNames));
            groupsMap.put(gr.getType(), gr);
        }
        return groupsMap;
    }

    private void mockLatestGroupMapCreating() {
        doReturn(Either.left(createTypeDefinition(new String[] {"a", "b", "c", "d"})))
                .when(groupTypeOperation).getLatestGroupTypeByType(eq(SdcGroupsMigration.GroupsForUpgrade.NW_COLLECTION_GROUP_NAME.getToscaType()), eq(false));
        doReturn(Either.left(createTypeDefinition(new String[] {"l", "m", "n", "o", "p"})))
                .when(groupTypeOperation).getLatestGroupTypeByType(eq(SdcGroupsMigration.GroupsForUpgrade.VFC_INSTANCE_GROUP_NAME.getToscaType()), eq(false));
        groupsMigration.loadLatestGroupTypeDefinitions();

    }

    private GroupTypeDefinition createTypeDefinition(String[] propertyNames) {
        GroupTypeDefinition typeDefinition = new GroupTypeDefinition();
        typeDefinition.setProperties(createTypeProperties(propertyNames));
        return typeDefinition;
    }

    private List<PropertyDefinition> createTypeProperties(String[] propertyNames) {
        List<PropertyDefinition> propertyDefinitionList = new ArrayList<>();
        for (String name: propertyNames) {
            PropertyDefinition propertyDefinition = new PropertyDefinition();
            propertyDefinition.setName(name);
            propertyDefinitionList.add(propertyDefinition);
        }
        return propertyDefinitionList;
    }

    private List<PropertyDataDefinition> createInstanceProperties(String[] propertyNames) {
        List<PropertyDataDefinition> propertyDefinitionList = new ArrayList<>();
        for (String name: propertyNames) {
            PropertyDefinition propertyDefinition = new PropertyDefinition();
            propertyDefinition.setName(name);
            propertyDefinitionList.add(propertyDefinition);
        }
        return propertyDefinitionList;
    }

}
