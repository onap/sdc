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
        
        when(janusGraphDao.getByCriteria(any(), any(), any(), any() )).thenReturn(Either.left(list));
        when(nodeTemplateOperation.createInstanceEdge(vertexOrig, instance)).thenReturn(StorageOperationStatus.OK);
        
        MigrationResult migrate = instancesMigration.migrate();
        MigrationStatus migrationStatus = migrate.getMigrationStatus();
        assertEquals(MigrationStatus.COMPLETED, migrationStatus);
    }
}
