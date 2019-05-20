/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.model;

import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphVertex;
import fj.data.Either;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ModelTestBase {

    protected static ConfigurationManager configurationManager;
    protected static final String CONTAINER_ID = "containerId";
    protected static final String CONTAINER_NAME = "containerName";

    public static void init() {
        String appConfigDir = "src/test/resources/config";
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(),
                appConfigDir);
        configurationManager = new ConfigurationManager(configurationSource);

        Configuration configuration = new Configuration();

        configuration.setTitanInMemoryGraph(true);

        Map<String, Object> deploymentRIArtifacts = new HashMap<>();
        ArtifactDataDefinition artifactInfo = new ArtifactDataDefinition();
        Object artifactDataObj = new HashMap<String, Object>();
        ((HashMap) artifactDataObj).put("1", artifactInfo);
        deploymentRIArtifacts.put("VfHeatEnv", artifactDataObj);

        configurationManager.setConfiguration(configuration);
        configurationManager.getConfiguration().setDeploymentResourceInstanceArtifacts(deploymentRIArtifacts);
	}

    protected void removeGraphVertices(Either<JanusGraph, JanusGraphOperationStatus> graphResult) {
        JanusGraph graph = graphResult.left().value();
        Iterable<JanusGraphVertex> vertices = graph.query().vertices();
        if (vertices != null) {
            Iterator<JanusGraphVertex> iterator = vertices.iterator();
            while (iterator.hasNext()) {
                JanusGraphVertex vertex = iterator.next();
                vertex.remove();
            }

        }
    }

    protected PropertyDefinition createSimpleProperty(String defaultValue, String name, String type) {
        PropertyDefinition prop1 = new PropertyDefinition();
        prop1.setDefaultValue(defaultValue);
        prop1.setName(name);
        prop1.setType(type);
        return prop1;
    }

    protected GraphVertex createBasicContainerGraphVertex() {
        GraphVertex resource = new GraphVertex(VertexTypeEnum.TOPOLOGY_TEMPLATE);
        resource.addMetadataProperty(GraphPropertyEnum.UNIQUE_ID, CONTAINER_ID);
        resource.addMetadataProperty(GraphPropertyEnum.NAME, CONTAINER_NAME);
        resource.setJsonMetadataField(JsonPresentationFields.NAME, CONTAINER_NAME);
        resource.setJsonMetadataField(JsonPresentationFields.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
        return resource;
    }
}
