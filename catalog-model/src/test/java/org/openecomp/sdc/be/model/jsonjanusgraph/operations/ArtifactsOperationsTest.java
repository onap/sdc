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

package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import fj.data.Either;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.InterfaceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;

public class ArtifactsOperationsTest {

    private static final String SERVICE_ID = "serviceId";
    private static final String INSTANCE_ID = "instanceId";
    private ArtifactsOperations testInstance = mock(ArtifactsOperations.class, CALLS_REAL_METHODS);

    @Test
    public void addArtifactToComponent() {
        ArtifactDefinition artifactDef = new ArtifactDefinition();
        artifactDef.setArtifactGroupType(ArtifactGroupTypeEnum.INFORMATIONAL);
        final Resource resource = new Resource();
        resource.setUniqueId(INSTANCE_ID);
        Map<String, ToscaDataDefinition> instanceArtifacts = Collections.singletonMap(INSTANCE_ID, getArtifactsByInstance("name1"));
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.ARTIFACTS);
        doReturn(Either.left(false)).when(testInstance).isCloneNeeded(Mockito.any(),
            Mockito.any());
        Map<String, ArtifactDefinition> artMap = new HashMap<>();
        artMap.put(INSTANCE_ID, artifactDef);
        doReturn(Either.left(artifactDef)).when(testInstance).updateArtifactOnGraph(resource,
            artifactDef, NodeTypeEnum.Resource, null, INSTANCE_ID, false,
            false);

        Either<ArtifactDefinition, StorageOperationStatus> ret = testInstance.addArtifactToComponent(
            artifactDef, resource, NodeTypeEnum.Resource, false, "instanceId");

        assertTrue(ret.isLeft());
    }

    @Test
    public void addArtifactToComponentEsId() {
        ArtifactDefinition artifactDef = new ArtifactDefinition();
        artifactDef.setArtifactGroupType(ArtifactGroupTypeEnum.INFORMATIONAL);
        artifactDef.setUniqueId(null);
        artifactDef.setEsId(INSTANCE_ID);
        final Resource resource = new Resource();
        resource.setUniqueId(INSTANCE_ID);
        Map<String, ArtifactDataDefinition> instanceArtifacts = getMapArtifactsByName("artifactDefId");
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.ARTIFACTS);
        doReturn(Either.left(false)).when(testInstance).isCloneNeeded(Mockito.any(),
            Mockito.any());
        Map<String, ArtifactDefinition> artMap = new HashMap<>();
        artMap.put(INSTANCE_ID, artifactDef);
        doReturn(StorageOperationStatus.OK).when(testInstance).updateToscaDataOfToscaElement(Mockito.anyString(),
            Mockito.any(), Mockito.any(), (List<ToscaDataDefinition>) Mockito.any(), Mockito.any());
        doReturn(Either.left(artifactDef)).when(testInstance).updateArtifactOnGraph(resource,
            artifactDef, NodeTypeEnum.Resource, null, INSTANCE_ID, false,
            false);

        Either<ArtifactDefinition, StorageOperationStatus> ret = testInstance.addArtifactToComponent(
            artifactDef, resource, NodeTypeEnum.Resource, false, "instanceId");

        assertTrue(ret.isLeft());
    }

    @Test
    public void addArtifactToComponentFail() {
        ArtifactDefinition artifactDef = new ArtifactDefinition();
        artifactDef.setArtifactGroupType(ArtifactGroupTypeEnum.INFORMATIONAL);
        final Resource resource = new Resource();
        resource.setUniqueId(INSTANCE_ID);
        Map<String, ToscaDataDefinition> instanceArtifacts = Collections.singletonMap(INSTANCE_ID, getArtifactsByInstance("name1"));
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.ARTIFACTS);
        doReturn(Either.left(false)).when(testInstance).isCloneNeeded(Mockito.any(),
            Mockito.any());
        Map<String, ArtifactDefinition> artMap = new HashMap<>();
        artMap.put(INSTANCE_ID, artifactDef);
        doReturn(Either.right(StorageOperationStatus.ARTIFACT_NOT_FOUND)).when(testInstance).updateArtifactOnGraph(resource,
            artifactDef, NodeTypeEnum.Resource, null, INSTANCE_ID, false,
            false);

        Either<ArtifactDefinition, StorageOperationStatus> ret = testInstance.addArtifactToComponent(
            artifactDef, resource, NodeTypeEnum.Resource, false, "instanceId");

        assertTrue(ret.isRight());
    }

    @Test
    public void updateArtifactOnResource() {
        ArtifactDefinition artifactDef = new ArtifactDefinition();
        artifactDef.setArtifactGroupType(ArtifactGroupTypeEnum.INFORMATIONAL);
        final Resource resource = new Resource();
        resource.setUniqueId(INSTANCE_ID);
        Map<String, ArtifactDataDefinition> instanceArtifacts = getMapArtifactsByName("artifactDefId");
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.ARTIFACTS);
        doReturn(Either.left(false)).when(testInstance).isCloneNeeded(Mockito.any(),
            Mockito.any());
        Map<String, ArtifactDefinition> artMap = new HashMap<>();
        artMap.put(INSTANCE_ID, artifactDef);
        doReturn(StorageOperationStatus.OK).when(testInstance).updateToscaDataOfToscaElement(Mockito.anyString(),
            Mockito.any(), Mockito.any(), (List<ToscaDataDefinition>) Mockito.any(), Mockito.any());
        doReturn(Either.left(artifactDef)).when(testInstance).updateArtifactOnGraph(resource,
            artifactDef, NodeTypeEnum.Resource, null, INSTANCE_ID, false,
            false);

        Either<ArtifactDefinition, StorageOperationStatus> ret = testInstance.updateArtifactOnResource(
            artifactDef, resource, "instanceId", NodeTypeEnum.Resource, "instanceId", false);

        assertTrue(ret.isLeft());
    }

    @Test
    public void updateArtifactOnResourceFail() {
        ArtifactDefinition artifactDef = new ArtifactDefinition();
        artifactDef.setArtifactGroupType(ArtifactGroupTypeEnum.INFORMATIONAL);
        final Resource resource = new Resource();
        resource.setUniqueId(INSTANCE_ID);
        Map<String, ToscaDataDefinition> instanceArtifacts = Collections.singletonMap(INSTANCE_ID, getArtifactsByInstance("name1"));
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.ARTIFACTS);
        doReturn(Either.left(false)).when(testInstance).isCloneNeeded(Mockito.any(),
            Mockito.any());
        Map<String, ArtifactDefinition> artMap = new HashMap<>();
        artMap.put(INSTANCE_ID, artifactDef);

        doReturn(Either.right(StorageOperationStatus.ARTIFACT_NOT_FOUND)).when(testInstance).updateArtifactOnGraph(resource,
            artifactDef, NodeTypeEnum.Resource, INSTANCE_ID, INSTANCE_ID, false,
            false);

        Either<ArtifactDefinition, StorageOperationStatus> ret = testInstance.updateArtifactOnResource(
            artifactDef, resource, "instanceId", NodeTypeEnum.Resource, "instanceId", false);

        assertTrue(ret.isRight());
    }

    @Test
    public void isCloneNeeded() {
        ArtifactDefinition artifactDef = new ArtifactDefinition();
        artifactDef.setArtifactGroupType(ArtifactGroupTypeEnum.INFORMATIONAL);
        doReturn(Either.left(false)).when(testInstance).isCloneNeeded(Mockito.any(),
            Mockito.any());

        Either<Boolean, StorageOperationStatus> ret = testInstance.isCloneNeeded(
            INSTANCE_ID, artifactDef, NodeTypeEnum.Resource);

        assertTrue(ret.isLeft());
    }

    @Test
    public void getArtifactById() {
        Map<String, ArtifactDataDefinition> instanceArtifacts = getMapArtifactsByName("artifactDefId");
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.DEPLOYMENT_ARTIFACTS);
        Either<ArtifactDefinition, StorageOperationStatus> ret = testInstance.getArtifactById(
            INSTANCE_ID, "artifactDefId");

        assertTrue(ret.isLeft());
    }

    @Test
    public void getArtifactById2() {
        Map<String, ToscaDataDefinition> instanceArtifacts =
            Collections.singletonMap(INSTANCE_ID, getArtifactsByInstanceSettingUniqueId("artifactDefId"));
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph("containerId",
            EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS);
        Either<ArtifactDefinition, StorageOperationStatus> ret = testInstance.getArtifactById(
            INSTANCE_ID, "artifactDefId", ComponentTypeEnum.RESOURCE_INSTANCE, "containerId");

        assertTrue(ret.isLeft());
    }

    @Test
    public void getArtifactById3() {
        Map<String, ToscaDataDefinition> instanceArtifacts =
            Collections.singletonMap(INSTANCE_ID, getArtifactsByInstanceSettingUniqueId("artifactDefId"));
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph("containerId",
            EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS);
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph("containerId", EdgeLabelEnum.INSTANCE_ARTIFACTS);
        Either<ArtifactDefinition, StorageOperationStatus> ret = testInstance.getArtifactById(
            INSTANCE_ID, "artifactDefId", ComponentTypeEnum.RESOURCE_INSTANCE, "containerId");

        assertTrue(ret.isLeft());
    }

    @Test
    public void getArtifactById4() {
        Map<String, ArtifactDataDefinition> instanceArtifacts = getMapArtifactsByName("artifactDefId");
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph("containerId",
            EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph("containerId",
            EdgeLabelEnum.INSTANCE_ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph(INSTANCE_ID,
            EdgeLabelEnum.DEPLOYMENT_ARTIFACTS);
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.TOSCA_ARTIFACTS);
        Either<ArtifactDefinition, StorageOperationStatus> ret = testInstance.getArtifactById(
            INSTANCE_ID, "artifactDefId", ComponentTypeEnum.RESOURCE_INSTANCE, "containerId");

        assertTrue(ret.isLeft());
    }

    @Test
    public void getArtifactById5() {
        Map<String, ArtifactDataDefinition> instanceArtifacts = getMapArtifactsByName("artifactDefId");
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph("containerId",
            EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph("containerId",
            EdgeLabelEnum.INSTANCE_ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph(INSTANCE_ID,
            EdgeLabelEnum.DEPLOYMENT_ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.TOSCA_ARTIFACTS);
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.ARTIFACTS);
        Either<ArtifactDefinition, StorageOperationStatus> ret = testInstance.getArtifactById(
            INSTANCE_ID, "artifactDefId", ComponentTypeEnum.RESOURCE_INSTANCE, "containerId");

        assertTrue(ret.isLeft());
    }

    @Test
    public void getArtifactById6() {
        Map<String, ArtifactDataDefinition> instanceArtifacts = getMapArtifactsByName("artifactDefId");
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph("containerId",
            EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph("containerId",
            EdgeLabelEnum.INSTANCE_ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph(INSTANCE_ID,
            EdgeLabelEnum.DEPLOYMENT_ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.TOSCA_ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.ARTIFACTS);
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.SERVICE_API_ARTIFACTS);
        Either<ArtifactDefinition, StorageOperationStatus> ret = testInstance.getArtifactById(
            INSTANCE_ID, "artifactDefId", ComponentTypeEnum.RESOURCE_INSTANCE, "containerId");

        assertTrue(ret.isLeft());
    }

    @Test
    public void getArtifactById7_NotFound1() {
        Map<String, InterfaceDataDefinition> interfaceDefinitions = getInterfaceDataDefinitionsByName("artifactDefId");
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph("containerId",
            EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph("containerId",
            EdgeLabelEnum.INSTANCE_ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph(INSTANCE_ID,
            EdgeLabelEnum.DEPLOYMENT_ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.TOSCA_ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph(INSTANCE_ID,
            EdgeLabelEnum.SERVICE_API_ARTIFACTS);
        doReturn(Either.left(interfaceDefinitions)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.INTERFACE);
        Either<ArtifactDefinition, StorageOperationStatus> ret = testInstance.getArtifactById(
            INSTANCE_ID, "artifactDefId", ComponentTypeEnum.RESOURCE_INSTANCE, "containerId");

        assertTrue(ret.isRight());
    }

    @Test
    public void getArtifactById7_NotFound2() {
        Map<String, InterfaceDataDefinition> interfaceDefinitions = getInterfaceDataDefinitionsByName("artifactDefId");
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph("containerId",
            EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph("containerId",
            EdgeLabelEnum.INSTANCE_ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph(INSTANCE_ID,
            EdgeLabelEnum.DEPLOYMENT_ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.TOSCA_ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph(INSTANCE_ID,
            EdgeLabelEnum.SERVICE_API_ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.INTERFACE);
        Either<ArtifactDefinition, StorageOperationStatus> ret = testInstance.getArtifactById(
            INSTANCE_ID, "artifactDefId", ComponentTypeEnum.RESOURCE_INSTANCE, "containerId");

        assertTrue(ret.isRight());
    }

    @Test
    public void getArtifactById7_NotFound3() {
        Map<String, InterfaceDataDefinition> interfaceDefinitions =
            getInterfaceDataDefinitionsWithOperationsByNameNotFound("artifactDefId");
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph("containerId",
            EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph("containerId",
            EdgeLabelEnum.INSTANCE_ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph(INSTANCE_ID,
            EdgeLabelEnum.DEPLOYMENT_ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.TOSCA_ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph(INSTANCE_ID,
            EdgeLabelEnum.SERVICE_API_ARTIFACTS);
        doReturn(Either.left(interfaceDefinitions)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.INTERFACE);
        Either<ArtifactDefinition, StorageOperationStatus> ret = testInstance.getArtifactById(
            INSTANCE_ID, "artifactDefId", ComponentTypeEnum.RESOURCE_INSTANCE, "containerId");

        assertTrue(ret.isRight());
    }

    @Test
    public void getArtifactById7() {
        Map<String, InterfaceDataDefinition> interfaceDefinitions = getInterfaceDataDefinitionsWithOperationsByName("artifactDefId");
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph("containerId",
            EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph("containerId",
            EdgeLabelEnum.INSTANCE_ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph(INSTANCE_ID,
            EdgeLabelEnum.DEPLOYMENT_ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.TOSCA_ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph(INSTANCE_ID,
            EdgeLabelEnum.SERVICE_API_ARTIFACTS);
        doReturn(Either.left(interfaceDefinitions)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.INTERFACE);
        Either<ArtifactDefinition, StorageOperationStatus> ret = testInstance.getArtifactById(
            INSTANCE_ID, "artifactDefId", ComponentTypeEnum.RESOURCE_INSTANCE, "containerId");

        assertTrue(ret.isLeft());
    }

    @Test
    public void removeArtifactFromResource() {
        Map<String, ArtifactDataDefinition> instanceArtifacts = getMapArtifactsByName2("artifactDefId");
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.DEPLOYMENT_ARTIFACTS);
        doReturn(StorageOperationStatus.OK).when(testInstance).deleteToscaDataElement(INSTANCE_ID, EdgeLabelEnum.ARTIFACTS, VertexTypeEnum.ARTIFACTS,
            null, JsonPresentationFields.ARTIFACT_LABEL);
        Either<ArtifactDefinition, StorageOperationStatus> ret = testInstance.removeArifactFromResource(
            INSTANCE_ID, "instanceId", NodeTypeEnum.Resource, false);

        assertTrue(ret.isLeft());
    }

    @Test
    public void removeArtifactFromResource_Mandatory() {
        Map<String, ArtifactDataDefinition> instanceArtifacts = getMapArtifactsByName2("artifactDefId");
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.DEPLOYMENT_ARTIFACTS);
        doReturn(StorageOperationStatus.OK).when(testInstance).deleteToscaDataElement(INSTANCE_ID, EdgeLabelEnum.ARTIFACTS, VertexTypeEnum.ARTIFACTS,
            null, JsonPresentationFields.ARTIFACT_LABEL);
        Either<ArtifactDefinition, StorageOperationStatus> ret = testInstance.removeArifactFromResource(
            INSTANCE_ID, "instanceId", NodeTypeEnum.Resource, true);

        assertTrue(ret.isLeft());
    }

    @Test
    public void removeArtifactFromResource_NotFound() {
        doReturn(Either.right(StorageOperationStatus.NOT_FOUND)).when(testInstance).getArtifactById(INSTANCE_ID, INSTANCE_ID);
        Either<ArtifactDefinition, StorageOperationStatus> ret = testInstance.removeArifactFromResource(
            INSTANCE_ID, "instanceId", NodeTypeEnum.Resource, false);

        assertTrue(ret.isRight());
    }

    @Test
    public void removeArtifactFromResource_NotFound2() {
        doReturn(Either.right(StorageOperationStatus.NOT_FOUND)).when(testInstance).removeArtifactOnGraph(INSTANCE_ID, INSTANCE_ID,
            NodeTypeEnum.Resource, false);
        Either<ArtifactDefinition, StorageOperationStatus> ret = testInstance.removeArifactFromResource(
            INSTANCE_ID, "instanceId", NodeTypeEnum.Resource, false);

        assertTrue(ret.isRight());
    }

    @Test
    public void getArtifacts() {
        Map<String, ArtifactDataDefinition> instanceArtifacts = getMapArtifactsByName2("artifactDefId");
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.ARTIFACTS);

        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> ret = testInstance.getArtifacts(INSTANCE_ID, NodeTypeEnum.Resource,
            ArtifactGroupTypeEnum.INFORMATIONAL, INSTANCE_ID);

        assertTrue(ret.isLeft());
    }

    @Test
    public void getArtifacts_NotFound() {
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.ARTIFACTS);

        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> ret = testInstance.getArtifacts(INSTANCE_ID, NodeTypeEnum.Resource,
            ArtifactGroupTypeEnum.INFORMATIONAL, INSTANCE_ID);

        assertTrue(ret.isRight());
    }

    @Test
    public void getArtifacts2() {
        Map<String, ArtifactDataDefinition> instanceArtifacts = getMapArtifactsByName2("artifactDefId");
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.ARTIFACTS);
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.DEPLOYMENT_ARTIFACTS);
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.TOSCA_ARTIFACTS);

        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> ret = testInstance.getArtifacts(INSTANCE_ID);

        assertTrue(ret.isLeft());
    }

    @Test
    public void addHeatEnvArtifact() {
        ArtifactDefinition artifactDef = new ArtifactDefinition();
        artifactDef.setArtifactGroupType(ArtifactGroupTypeEnum.INFORMATIONAL);
        ArtifactDefinition artifactDefHeat = new ArtifactDefinition();
        artifactDefHeat.setArtifactGroupType(ArtifactGroupTypeEnum.INFORMATIONAL);
        final Resource resource = new Resource();
        resource.setUniqueId(INSTANCE_ID);
        doReturn(Either.left(false)).when(testInstance).isCloneNeeded(Mockito.any(),
            Mockito.any());
        Map<String, ArtifactDataDefinition> instanceArtifacts = getMapArtifactsByName("artifactDefId");
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.ARTIFACTS);
        doReturn(StorageOperationStatus.OK).when(testInstance).updateToscaDataOfToscaElement(Mockito.anyString(),
            Mockito.any(), Mockito.any(), (List<ToscaDataDefinition>) Mockito.any(), Mockito.any());
        Either<ArtifactDefinition, StorageOperationStatus> ret = testInstance.addHeatEnvArtifact(artifactDef, artifactDefHeat,
            resource, NodeTypeEnum.Resource, false, INSTANCE_ID);

        assertTrue(ret.isLeft());
    }

    @Test
    public void getHeatArtifactByHeatEnvId() {
        Map<String, InterfaceDataDefinition> interfaceDefinitions = getInterfaceDataDefinitionsByName("artifactDefId");
        Map<String, ArtifactDataDefinition> instanceArtifacts = getMapArtifactsByName("artifactDefId");
        ArtifactDefinition artifactDefHeat = new ArtifactDefinition();
        artifactDefHeat.setArtifactGroupType(ArtifactGroupTypeEnum.INFORMATIONAL);
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.DEPLOYMENT_ARTIFACTS);
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.TOSCA_ARTIFACTS);
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.ARTIFACTS);
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.SERVICE_API_ARTIFACTS);
        doReturn(Either.left(interfaceDefinitions)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.INTERFACE);
        Either<ArtifactDefinition, StorageOperationStatus> ret = testInstance.getHeatArtifactByHeatEnvId(
            INSTANCE_ID, artifactDefHeat, "containerId", ComponentTypeEnum.RESOURCE);

        assertTrue(ret.isRight());
    }

    @Test
    public void updateHeatEnvArtifact() {
        ArtifactDefinition artifactDef = new ArtifactDefinition();
        artifactDef.setUniqueId("artifactId");
        artifactDef.setArtifactGroupType(ArtifactGroupTypeEnum.INFORMATIONAL);
        ArtifactDefinition artifactDefHeat = new ArtifactDefinition();
        artifactDefHeat.setUniqueId("newArtifactId");
        artifactDefHeat.setArtifactGroupType(ArtifactGroupTypeEnum.INFORMATIONAL);
        final Resource resource = new Resource();
        resource.setUniqueId(INSTANCE_ID);
        Map<String, ArtifactDataDefinition> instanceArtifacts = getMapArtifactsByName("artifactDefId");
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.DEPLOYMENT_ARTIFACTS);
        doReturn(Either.left(false)).when(testInstance).isCloneNeeded(Mockito.any(),
            Mockito.any());
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.ARTIFACTS);
        doReturn(StorageOperationStatus.OK).when(testInstance).updateToscaDataOfToscaElement(Mockito.anyString(),
            Mockito.any(), Mockito.any(), (List<ToscaDataDefinition>) Mockito.any(), Mockito.any());

        Either<ArtifactDefinition, StorageOperationStatus> ret = testInstance.updateHeatEnvArtifact(
            resource, artifactDef, "artifactId", "newArtifactId", NodeTypeEnum.Resource, INSTANCE_ID);

        assertTrue(ret.isLeft());
    }

    @Test
    public void updateHeatEnvArtifact_NotFound() {
        ArtifactDefinition artifactDef = new ArtifactDefinition();
        artifactDef.setUniqueId("artifactId");
        artifactDef.setArtifactGroupType(ArtifactGroupTypeEnum.INFORMATIONAL);
        ArtifactDefinition artifactDefHeat = new ArtifactDefinition();
        artifactDefHeat.setUniqueId("newArtifactId");
        artifactDefHeat.setArtifactGroupType(ArtifactGroupTypeEnum.INFORMATIONAL);
        final Resource resource = new Resource();
        resource.setUniqueId(INSTANCE_ID);
        Map<String, ArtifactDataDefinition> instanceArtifacts = getMapArtifactsByName("artifactDefId");
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.DEPLOYMENT_ARTIFACTS);
        Either<ArtifactDefinition, StorageOperationStatus> ret = testInstance.updateHeatEnvArtifact(
            resource, artifactDef, "artifactId", "newArtifactId", NodeTypeEnum.Resource, INSTANCE_ID);

        assertTrue(ret.isRight());
    }

    @Test
    public void updateHeatEnvArtifactOnInstance() {
        ArtifactDefinition artifactDef = new ArtifactDefinition();
        artifactDef.setUniqueId("artifactId");
        artifactDef.setArtifactGroupType(ArtifactGroupTypeEnum.INFORMATIONAL);
        ArtifactDefinition artifactDefHeat = new ArtifactDefinition();
        artifactDefHeat.setUniqueId("newArtifactId");
        artifactDefHeat.setArtifactGroupType(ArtifactGroupTypeEnum.INFORMATIONAL);
        final Resource resource = new Resource();
        resource.setUniqueId(INSTANCE_ID);
        Map<String, ArtifactDataDefinition> instanceArtifacts = getMapArtifactsByName("artifactDefId");
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS);
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.DEPLOYMENT_ARTIFACTS);
        doReturn(Either.left(false)).when(testInstance).isCloneNeeded(Mockito.any(),
            Mockito.any());
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.ARTIFACTS);
        doReturn(StorageOperationStatus.OK).when(testInstance).updateToscaDataOfToscaElement(Mockito.anyString(),
            Mockito.any(), Mockito.any(), (List<ToscaDataDefinition>) Mockito.any(), Mockito.any());

        Either<ArtifactDefinition, StorageOperationStatus> ret = testInstance.updateHeatEnvArtifactOnInstance(
            resource, artifactDef, "artifactId", "newArtifactId", NodeTypeEnum.Resource, INSTANCE_ID);

        assertTrue(ret.isLeft());
    }

    @Test
    public void updateHeatEnvPlaceholder() {
        ArtifactDefinition artifactDef = new ArtifactDefinition();
        artifactDef.setArtifactGroupType(ArtifactGroupTypeEnum.INFORMATIONAL);
        final Resource resource = new Resource();
        resource.setUniqueId(INSTANCE_ID);
        Map<String, ArtifactDataDefinition> instanceArtifacts = getMapArtifactsByName("artifactDefId");
        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(INSTANCE_ID, EdgeLabelEnum.ARTIFACTS);
        doReturn(Either.left(false)).when(testInstance).isCloneNeeded(Mockito.any(),
            Mockito.any());
        Map<String, ArtifactDefinition> artMap = new HashMap<>();
        artMap.put(INSTANCE_ID, artifactDef);
        doReturn(StorageOperationStatus.OK).when(testInstance).updateToscaDataOfToscaElement(Mockito.anyString(),
            Mockito.any(), Mockito.any(), (List<ToscaDataDefinition>) Mockito.any(), Mockito.any());
        doReturn(Either.left(artifactDef)).when(testInstance).updateArtifactOnGraph(resource,
            artifactDef, NodeTypeEnum.Resource, null, INSTANCE_ID, false,
            false);

        Either<ArtifactDefinition, StorageOperationStatus> ret = testInstance.updateHeatEnvPlaceholder(
            artifactDef, resource, NodeTypeEnum.Resource);

        assertTrue(ret.isLeft());
    }

    @Test
    public void getInstanceArtifacts_collectAllInstanceArtifacts() throws Exception {
        Map<String, ToscaDataDefinition> instanceArtifacts = Collections.singletonMap(INSTANCE_ID, getArtifactsByInstance("name1"));

        Map<String, ToscaDataDefinition> instanceDeploymentArtifacts = new HashMap<>();
        instanceDeploymentArtifacts.put(INSTANCE_ID, getArtifactsByInstance("name2", "name3"));
        instanceDeploymentArtifacts.put("instanceId2", getArtifactsByInstance("name4"));

        doReturn(Either.left(instanceArtifacts)).when(testInstance).getDataFromGraph(SERVICE_ID, EdgeLabelEnum.INSTANCE_ARTIFACTS);
        doReturn(Either.left(instanceDeploymentArtifacts)).when(testInstance).getDataFromGraph(SERVICE_ID, EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS);
        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> allInstArtifacts =
            testInstance.getAllInstanceArtifacts(SERVICE_ID, INSTANCE_ID);

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
        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> allInstArtifacts =
            testInstance.getAllInstanceArtifacts(SERVICE_ID, "someOtherInstance");

        assertTrue(allInstArtifacts.isLeft());
        assertTrue(allInstArtifacts.left().value().isEmpty());
    }

    @Test
    public void getInstanceArtifacts_errorGettingInstanceArtifacts() throws Exception {
        doReturn(Either.right(JanusGraphOperationStatus.GENERAL_ERROR)).when(testInstance)
            .getDataFromGraph(SERVICE_ID, EdgeLabelEnum.INSTANCE_ARTIFACTS);
        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> allInstArtifacts =
            testInstance.getAllInstanceArtifacts(SERVICE_ID, INSTANCE_ID);
        verify(testInstance, times(0)).getDataFromGraph(SERVICE_ID, EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS);
        assertTrue(allInstArtifacts.isRight());
    }

    @Test
    public void getAllInstanceArtifacts_errorGettingDeploymentArtifacts() throws Exception {
        doReturn(Either.left(new HashMap<>())).when(testInstance).getDataFromGraph(SERVICE_ID, EdgeLabelEnum.INSTANCE_ARTIFACTS);
        doReturn(Either.right(JanusGraphOperationStatus.GENERAL_ERROR)).when(testInstance).getDataFromGraph(SERVICE_ID,
            EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS);
        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> allInstArtifacts =
            testInstance.getAllInstanceArtifacts(SERVICE_ID, INSTANCE_ID);
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

    private ToscaDataDefinition getArtifactsByInstanceSettingUniqueId(String ... artifactsNames) {
        MapArtifactDataDefinition artifactsByInstance = new MapArtifactDataDefinition();
        Map<String, ArtifactDataDefinition> artifactsByName = new HashMap<>();
        for (String artifactName : artifactsNames) {
            ArtifactDataDefinition artifactDataDefinition = new ArtifactDataDefinition();
            artifactDataDefinition.setUniqueId(artifactName);
            artifactsByName.put(artifactName, artifactDataDefinition);
        }
        artifactsByInstance.setMapToscaDataDefinition(artifactsByName);
        return artifactsByInstance;
    }

    private Map<String, ArtifactDataDefinition> getMapArtifactsByName(String ... artifactsNames) {
        Map<String, ArtifactDataDefinition> artifactsByName = new HashMap<>();
        for (String artifactName : artifactsNames) {
            ArtifactDataDefinition artifactDataDefinition = new ArtifactDataDefinition();
            artifactDataDefinition.setUniqueId(artifactName);
            artifactDataDefinition.setGeneratedFromId("artifactId");
            artifactDataDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.INFORMATIONAL);
            artifactsByName.put(artifactName, artifactDataDefinition);
        }
        return artifactsByName;
    }

    private Map<String, ArtifactDataDefinition> getMapArtifactsByName2(String ... artifactsNames) {
        Map<String, ArtifactDataDefinition> artifactsByName = new HashMap<>();
        for (String artifactName : artifactsNames) {
            ArtifactDataDefinition artifactDataDefinition = new ArtifactDataDefinition();
            artifactDataDefinition.setUniqueId(INSTANCE_ID);
            artifactDataDefinition.setMandatory(true);
            artifactDataDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.INFORMATIONAL);
            artifactsByName.put(artifactName, artifactDataDefinition);
        }
        return artifactsByName;
    }

    private Map<String, InterfaceDataDefinition> getInterfaceDataDefinitionsByName(String ... artifactsNames) {
        Map<String, InterfaceDataDefinition> interfaceDataDefinitionsByName = new HashMap<>();
        for (String artifactName : artifactsNames) {
            InterfaceDataDefinition interfaceDataDefinition = new InterfaceDataDefinition();
            interfaceDataDefinition.setUniqueId(artifactName);
            interfaceDataDefinitionsByName.put(artifactName, interfaceDataDefinition);
        }
        return interfaceDataDefinitionsByName;
    }

    private Map<String, InterfaceDataDefinition> getInterfaceDataDefinitionsWithOperationsByName(String ... artifactsNames) {
        Map<String, InterfaceDataDefinition> interfaceDataDefinitionsByName = new HashMap<>();
        for (String artifactName : artifactsNames) {
            Map<String, OperationDataDefinition> operations = new HashMap<>();
            OperationDataDefinition operation1 = new OperationDataDefinition();
            operation1.setName("operation1");
            ArtifactDataDefinition impl1 = new ArtifactDataDefinition();
            impl1.setUniqueId(artifactName);
            operation1.setImplementation(impl1);
            operations.put(artifactName, operation1);
            InterfaceDataDefinition interfaceDataDefinition = new InterfaceDataDefinition();
            interfaceDataDefinition.setUniqueId(artifactName);
            interfaceDataDefinition.setOperations(operations);
            interfaceDataDefinitionsByName.put(artifactName, interfaceDataDefinition);
        }
        return interfaceDataDefinitionsByName;
    }

    private Map<String, InterfaceDataDefinition> getInterfaceDataDefinitionsWithOperationsByNameNotFound(String ... artifactsNames) {
        Map<String, InterfaceDataDefinition> interfaceDataDefinitionsByName = new HashMap<>();
        for (String artifactName : artifactsNames) {
            Map<String, OperationDataDefinition> operations = new HashMap<>();
            OperationDataDefinition operation1 = new OperationDataDefinition();
            operation1.setName("operation1");
            ArtifactDataDefinition impl1 = new ArtifactDataDefinition();
            impl1.setUniqueId("implementation1");
            operation1.setImplementation(impl1);
            operations.put(artifactName, operation1);
            InterfaceDataDefinition interfaceDataDefinition = new InterfaceDataDefinition();
            interfaceDataDefinition.setUniqueId(artifactName);
            interfaceDataDefinition.setOperations(operations);
            interfaceDataDefinitionsByName.put(artifactName, interfaceDataDefinition);
        }
        return interfaceDataDefinitionsByName;
    }
}
