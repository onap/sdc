/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.asdctool.migration.tasks.mig1806;

import fj.data.Either;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult.MigrationStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CompositionDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.enums.JsonConstantKeysEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeTemplateOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SDCInstancesMigrationTest{
    @Mock
    private JanusGraphDao janusGraphDao;
    @Mock
    private NodeTemplateOperation nodeTemplateOperation;
    @Mock
    GraphVertex topologyTemplateVertex;

    
    @Test
    public void testFailedMigration(){
        SDCInstancesMigration instancesMigration = new SDCInstancesMigration(janusGraphDao, nodeTemplateOperation);
        when(janusGraphDao.getByCriteria(any(), any(), any(), any() )).thenReturn(Either.right(
            JanusGraphOperationStatus.GENERAL_ERROR));
        
        MigrationResult migrate = instancesMigration.migrate();
        MigrationStatus migrationStatus = migrate.getMigrationStatus();
        assertEquals(MigrationStatus.FAILED, migrationStatus);
    }
    @Test
    public void testSuccessMigration(){
        SDCInstancesMigration instancesMigration = new SDCInstancesMigration(janusGraphDao, nodeTemplateOperation);
        List<GraphVertex> list = new ArrayList<>();
        GraphVertex vertexOrig = new GraphVertex();
        Map<String, CompositionDataDefinition> jsonComposition = new HashMap<>();
        CompositionDataDefinition composition = new CompositionDataDefinition();
        Map<String, ComponentInstanceDataDefinition> componentInstances = new HashMap<>();
        ComponentInstanceDataDefinition instance = new ComponentInstanceDataDefinition();
        componentInstances.put("instanceId", instance);
        composition.setComponentInstances(componentInstances);
        jsonComposition.put(JsonConstantKeysEnum.COMPOSITION.getValue(), composition);
        vertexOrig.setJson(jsonComposition);
        vertexOrig.setType(ComponentTypeEnum.SERVICE);
        list.add(vertexOrig);


        when(janusGraphDao.getVertexById(any())).thenReturn(Either.left(vertexOrig));
        when(janusGraphDao.getByCriteria(any(), any(), any(), any() )).thenReturn(Either.left(list));
        when(nodeTemplateOperation.createInstanceEdge(vertexOrig, instance)).thenReturn(StorageOperationStatus.OK);
        when(janusGraphDao.commit()).thenReturn(JanusGraphOperationStatus.OK);

        MigrationResult migrate = instancesMigration.migrate();
        MigrationStatus migrationStatus = migrate.getMigrationStatus();
        assertEquals(MigrationStatus.COMPLETED, migrationStatus);
    }
}
