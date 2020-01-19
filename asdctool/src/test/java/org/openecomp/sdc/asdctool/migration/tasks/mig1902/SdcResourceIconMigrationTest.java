/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.asdctool.migration.tasks.mig1902;

import fj.data.Either;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Maps;
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
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CompositionDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.enums.JsonConstantKeysEnum;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SdcResourceIconMigrationTest {
    @Mock
    private JanusGraphDao janusGraphDao;

    @Mock
    private GraphVertex graphVertex;

    @Mock
    private GraphVertex topologyTemplateVertex;

    @Mock
    private CompositionDataDefinition compositionDataDefinition;

    @Mock
    private ComponentInstanceDataDefinition  componentInstanceDataDefinition;

    @InjectMocks
    private SdcResourceIconMigration iconMigration;

    @Before
    public void setUp() {
        iconMigration = new SdcResourceIconMigration(janusGraphDao);
        when(janusGraphDao.getVertexById(any())).thenReturn(Either.left(topologyTemplateVertex));
        when(janusGraphDao.commit()).thenReturn(JanusGraphOperationStatus.OK);
    }


    @Test
    public void migrationFailedWhenNoNodeTypeDefined() {
        when(janusGraphDao.getByCriteria(eq(VertexTypeEnum.NODE_TYPE), anyMap(), anyMap(), eq(JsonParseFlagEnum.ParseAll)))
                .thenReturn(Either.left(Lists.emptyList()));
        assertEquals(MigrationResult.MigrationStatus.FAILED, iconMigration.migrate().getMigrationStatus());
    }

    @Test
    public void resourceIsNotUpdatedIfNotVL() {
        //iconMigration.handleOneContainer(graphVertex);
        mockInstancesNotFoundFlow();

        iconMigration.updateNodeTypeIconAndStoreInMap(ResourceTypeEnum.VL);
        assertFalse(iconMigration.updateIconInsideInstance(componentInstanceDataDefinition));
    }



    @Test
    public void resourceIsUpdatedIfCP() {
        //iconMigration.handleOneContainer(graphVertex);
        mockInstancesFoundFlow();
        when(janusGraphDao.updateVertex(any(GraphVertex.class))).thenReturn(Either.left(graphVertex));
        iconMigration.updateNodeTypeIconAndStoreInMap(ResourceTypeEnum.CP);
        assertTrue(iconMigration.updateIconInsideInstance(componentInstanceDataDefinition));
    }

    @Test
    public void migrateWhenIconsAreUpdated() {
        mockInstancesFoundFlow();
        when(janusGraphDao.getByCriteria(eq(VertexTypeEnum.TOPOLOGY_TEMPLATE), eq(null), anyMap(), eq(JsonParseFlagEnum.ParseAll)))
                .thenReturn(Either.left(Lists.newArrayList(topologyTemplateVertex)));
        when(compositionDataDefinition.getComponentInstances()).thenReturn(Maps.newHashMap("a", componentInstanceDataDefinition));
        doReturn(Maps.newHashMap(JsonConstantKeysEnum.COMPOSITION.getValue(), compositionDataDefinition)).when(topologyTemplateVertex).getJson();
        when(janusGraphDao.updateVertex(any(GraphVertex.class))).thenReturn(Either.left(graphVertex));

        assertEquals(MigrationResult.MigrationStatus.COMPLETED, iconMigration.migrate().getMigrationStatus());
        verify(janusGraphDao, times(3)).commit();
    }

    @Test
    public void migrateWhenIconsNotUpdated() {
        mockInstancesNotFoundFlow();
        when(janusGraphDao.getByCriteria(eq(VertexTypeEnum.TOPOLOGY_TEMPLATE), eq(null), anyMap(), eq(JsonParseFlagEnum.ParseAll)))
                .thenReturn(Either.left(Lists.newArrayList(topologyTemplateVertex)));
        when(compositionDataDefinition.getComponentInstances()).thenReturn(Maps.newHashMap("a", componentInstanceDataDefinition));
        doReturn(Maps.newHashMap(JsonConstantKeysEnum.COMPOSITION.getValue(), compositionDataDefinition)).when(topologyTemplateVertex).getJson();

        assertEquals(MigrationResult.MigrationStatus.COMPLETED, iconMigration.migrate().getMigrationStatus());
        verify(janusGraphDao, times(2)).commit();
    }

    @Test
    public void migrateWhenNoInstancesFound() {
        mockInstancesNotFoundFlow();
        when(janusGraphDao.getByCriteria(eq(VertexTypeEnum.TOPOLOGY_TEMPLATE), eq(null), anyMap(), eq(JsonParseFlagEnum.ParseAll)))
                .thenReturn(Either.left(Lists.newArrayList(topologyTemplateVertex)));

        assertEquals(MigrationResult.MigrationStatus.COMPLETED, iconMigration.migrate().getMigrationStatus());
        verify(janusGraphDao, times(2)).commit();
    }

    // A temp remark for this test - following Commit hash:    08595ad21b0c409c69e3902232f5575963199e3e [ASDC-641] – Migration workaround for deployment artifact timeout. Reviewer: Lior.
//    @Test
//    public void migrationFailedWhenInstanceVertexUpdateFailed() {
//        mockInstancesFoundFlow();
//        when(janusGraphDao.getByCriteria(eq(VertexTypeEnum.TOPOLOGY_TEMPLATE), eq(null), anyMap(), eq(JsonParseFlagEnum.ParseAll)))
//                .thenReturn(Either.left(Lists.newArrayList(topologyTemplateVertex)));
//        when(compositionDataDefinition.getComponentInstances()).thenReturn(Maps.newHashMap("a", componentInstanceDataDefinition));
//        doReturn(Maps.newHashMap(JsonConstantKeysEnum.COMPOSITION.getValue(), compositionDataDefinition)).when(topologyTemplateVertex).getJson();
//        when(janusGraphDao.updateVertex(any(GraphVertex.class))).thenReturn(Either.left(graphVertex))
//                .thenReturn(Either.left(graphVertex))
//                .thenReturn(Either.right(JanusGraphOperationStatus.GENERAL_ERROR));
//
//        assertEquals(MigrationResult.MigrationStatus.FAILED, iconMigration.migrate().getMigrationStatus());
//        verify(janusGraphDao, times(2)).commit();
//    }

    @Test
    public void migrationCompletedWhenVertexJsonIsEmpty() {
        mockInstancesFoundFlow();
        when(janusGraphDao.getByCriteria(eq(VertexTypeEnum.TOPOLOGY_TEMPLATE), eq(null), anyMap(), eq(JsonParseFlagEnum.ParseAll)))
                .thenReturn(Either.left(Lists.newArrayList(topologyTemplateVertex)));
        doReturn(new HashMap<>()).when(topologyTemplateVertex).getJson();
        when(janusGraphDao.updateVertex(any(GraphVertex.class))).thenReturn(Either.left(graphVertex));

        assertEquals(MigrationResult.MigrationStatus.COMPLETED, iconMigration.migrate().getMigrationStatus());
        verify(janusGraphDao, times(2)).commit();
    }

    @Test
    public void migrationCompletedWhenVertexJsonIsNull() {
        mockInstancesFoundFlow();
        when(janusGraphDao.getByCriteria(eq(VertexTypeEnum.TOPOLOGY_TEMPLATE), eq(null), anyMap(), eq(JsonParseFlagEnum.ParseAll)))
                .thenReturn(Either.left(Lists.newArrayList(topologyTemplateVertex)));
        doReturn(null).when(topologyTemplateVertex).getJson();
        when(janusGraphDao.updateVertex(any(GraphVertex.class))).thenReturn(Either.left(graphVertex));

        assertEquals(MigrationResult.MigrationStatus.COMPLETED, iconMigration.migrate().getMigrationStatus());
        verify(janusGraphDao, times(2)).commit();
    }

    @Test
    public void migrationFailedWhenTypeUpdateFailed() {
        mockInstancesFoundFlow();
        when(janusGraphDao.updateVertex(any(GraphVertex.class))).thenReturn(Either.right(JanusGraphOperationStatus.GENERAL_ERROR));
        assertEquals(MigrationResult.MigrationStatus.FAILED, iconMigration.migrate().getMigrationStatus());
        verify(janusGraphDao, times(0)).commit();
    }

    private void mockInstancesNotFoundFlow() {
        List<GraphVertex> nodeTypeVertexList = Lists.newArrayList(graphVertex);
        when(graphVertex.getMetadataProperty(GraphPropertyEnum.NAME)).thenReturn("vl1");
        when(componentInstanceDataDefinition.getComponentName()).thenReturn("other");
        when(janusGraphDao.getByCriteria(eq(VertexTypeEnum.NODE_TYPE), anyMap(), anyMap(), eq(JsonParseFlagEnum.ParseAll)))
                .thenReturn(Either.left(nodeTypeVertexList));
        when(janusGraphDao.updateVertex(any(GraphVertex.class))).thenReturn(Either.left(graphVertex));
    }

    private void mockInstancesFoundFlow() {
        when(graphVertex.getMetadataProperty(GraphPropertyEnum.NAME)).thenReturn(String.valueOf("cp1"));
        when(componentInstanceDataDefinition.getComponentName()).thenReturn("cp1");
        when(janusGraphDao.getByCriteria(eq(VertexTypeEnum.NODE_TYPE), anyMap(), anyMap(), eq(JsonParseFlagEnum.ParseAll)))
                .thenReturn(Either.left(Lists.newArrayList(graphVertex)));
    }


}
