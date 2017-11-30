package org.openecomp.sdc.be.model.jsontitan.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import fj.data.Either;

public class ArtifactsOperationsTest {

    private static final String SERVICE_ID = "serviceId";
    private static final String INSTANCE_ID = "instanceId";
    private ArtifactsOperations testInstance = mock(ArtifactsOperations.class, CALLS_REAL_METHODS);

    @Test
    public void getInstanceArtifacts_collectAllInstanceArtifacts() throws Exception {
        Map<String, ToscaDataDefinition> instanceArtifacts = Collections.singletonMap(INSTANCE_ID, getArtifactsByInstance("name1"));

        Map<String, ToscaDataDefinition> instanceDeploymentArtifacts = new HashMap<>();
        instanceDeploymentArtifacts.put(INSTANCE_ID, getArtifactsByInstance("name2", "name3"));
        instanceDeploymentArtifacts.put("instanceId2", getArtifactsByInstance("name4"));

        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(SERVICE_ID, EdgeLabelEnum.INSTANCE_ARTIFACTS);
        doReturn(Either.left(instanceDeploymentArtifacts)).when(testInstance).getDataFromGraph(SERVICE_ID, EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS);
        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> allInstArtifacts = testInstance.getAllInstanceArtifacts(SERVICE_ID, INSTANCE_ID);

        assertTrue(allInstArtifacts.isLeft());
        assertEquals(allInstArtifacts.left().value().size(), 3);
        assertTrue(allInstArtifacts.left().value().containsKey("name1"));
        assertTrue(allInstArtifacts.left().value().containsKey("name2"));
        assertTrue(allInstArtifacts.left().value().containsKey("name3"));
        assertFalse(allInstArtifacts.left().value().containsKey("name4"));//this key is of different instance
    }

    @Test
    public void getInstanceArtifacts_noArtifactsForInstance() throws Exception {
        Map<String, ToscaDataDefinition> instanceArtifacts = Collections.singletonMap(INSTANCE_ID, getArtifactsByInstance("name1"));

        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(SERVICE_ID, EdgeLabelEnum.INSTANCE_ARTIFACTS);
        doReturn(Either.left(new HashMap<>())).when(testInstance).getDataFromGraph(SERVICE_ID, EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS);
        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> allInstArtifacts = testInstance.getAllInstanceArtifacts(SERVICE_ID, "someOtherInstance");

        assertTrue(allInstArtifacts.isLeft());
        assertTrue(allInstArtifacts.left().value().isEmpty());
    }

    @Test
    public void getInstanceArtifacts_errorGettingInstanceArtifacts() throws Exception {
        doReturn(Either.right(TitanOperationStatus.GENERAL_ERROR)).when(testInstance).getDataFromGraph(SERVICE_ID, EdgeLabelEnum.INSTANCE_ARTIFACTS);
        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> allInstArtifacts = testInstance.getAllInstanceArtifacts(SERVICE_ID, INSTANCE_ID);
        verify(testInstance, times(0)).getDataFromGraph(SERVICE_ID, EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS);
        assertTrue(allInstArtifacts.isRight());
    }

    @Test
    public void getAllInstanceArtifacts_errorGettingDeploymentArtifacts() throws Exception {
        doReturn(Either.left(new HashMap<>())).when(testInstance).getDataFromGraph(SERVICE_ID, EdgeLabelEnum.INSTANCE_ARTIFACTS);
        doReturn(Either.right(TitanOperationStatus.GENERAL_ERROR)).when(testInstance).getDataFromGraph(SERVICE_ID, EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS);
        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> allInstArtifacts = testInstance.getAllInstanceArtifacts(SERVICE_ID, INSTANCE_ID);
        assertTrue(allInstArtifacts.isRight());
    }

    private ToscaDataDefinition getArtifactsByInstance(String ... artifactsNames) {
        MapArtifactDataDefinition artifactsByInstance = new MapArtifactDataDefinition();
        Map<String, ArtifactDataDefinition> artifactsByName = new HashMap<>();
        for (String artifactName : artifactsNames) {
            artifactsByName.put(artifactName, new ArtifactDataDefinition());
        }
        artifactsByInstance.setMapToscaDataDefinition(artifactsByName);
        return artifactsByInstance;
    }
}
