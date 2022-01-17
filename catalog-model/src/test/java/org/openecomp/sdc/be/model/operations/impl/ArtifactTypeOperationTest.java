/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 *
 *
 */

package org.openecomp.sdc.be.model.operations.impl;

import fj.data.Either;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphVertex;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.dao.janusgraph.HealingJanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ArtifactTypeDataDefinition;
import org.openecomp.sdc.be.model.ArtifactTypeDefinition;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Iterator;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SpringJUnitConfig(locations = "classpath:application-context-test.xml")
public class ArtifactTypeOperationTest extends ModelTestBase {

    private static final String NULL_STRING = null;

    @Autowired
    private ArtifactTypeOperation artifactTypeOperation;

    @Autowired
    private HealingJanusGraphGenericDao janusGraphGenericDao;

    @BeforeAll
    public static void setupBeforeClass() {
        ModelTestBase.init();
    }

    @BeforeEach
    public void cleanUp() {
        JanusGraphGenericDao janusGraphGenericDao = artifactTypeOperation.janusGraphGenericDao;
        Either<JanusGraph, JanusGraphOperationStatus> graphResult = janusGraphGenericDao.getGraph();
        JanusGraph graph = graphResult.left().value();

        Iterable<JanusGraphVertex> vertices = graph.query().vertices();
        if (vertices != null) {
            Iterator<JanusGraphVertex> iterator = vertices.iterator();
            while (iterator.hasNext()) {
                JanusGraphVertex vertex = iterator.next();
                vertex.remove();
            }
        }
        janusGraphGenericDao.commit();
    }

    @Test
    public void createArtifactType() {

        ArtifactTypeDefinition createdType = createArtifactTypeDef("type1", "description1", "derivedFromTest1");
        artifactTypeOperation.createArtifactType(createdType, false);

        assertNotNull(createdType);
        assertEquals("type1name", createdType.getName());
        assertEquals("description1", createdType.getDescription());
        assertEquals("derivedFromTest1", createdType.getDerivedFrom());
    }

    @Test
    public void createAndModifyArtifactType() {

        ArtifactTypeDefinition createdType = createArtifactTypeDef("type2", "description1", "derivedFromTest2");
        artifactTypeOperation.createArtifactType(createdType, false);

        createdType = createArtifactTypeDef("type2", "description2", NULL_STRING);
        createdType.setName("newName2");

        createdType = artifactTypeOperation.createArtifactType(createdType, false);
        assertNotNull(createdType);
        assertEquals("newName2", createdType.getName());
        assertEquals("description2", createdType.getDescription());
        assertEquals(null, createdType.getDerivedFrom());
    }

    @Test
    public void createAndModifyArtifactType_WithProps() {

        ArtifactTypeDefinition createdType = createArtifactTypeDef("type3", "description1", "derivedFromTest3");
        artifactTypeOperation.createArtifactType(createdType, false);

        PropertyDefinition prop1 = createSimpleProperty("val1", "prop1", "string");
        PropertyDefinition prop2 = createSimpleProperty("val2", "prop2", "string");
        createdType = createArtifactTypeDef("type3", "description2", NULL_STRING, prop1, prop2);
        createdType.setName("newName");

        createdType = artifactTypeOperation.createArtifactType(createdType, false);
        assertNotNull(createdType);
        assertEquals("newName", createdType.getName());
        assertEquals("description2", createdType.getDescription());
        assertEquals(null, createdType.getDerivedFrom());
        assertEquals(2, createdType.getProperties().size());
    }

    private ArtifactTypeDefinition createArtifactTypeDef(String type, String description, String derivedFrom) {
        return createArtifactTypeDef(type, description, derivedFrom, null);
    }

    private ArtifactTypeDefinition createArtifactTypeDef(String type, String description, String derivedFrom, PropertyDefinition... props) {
        ArtifactTypeDataDefinition artifactTypeDataDefinition = new ArtifactTypeDataDefinition();
        artifactTypeDataDefinition.setDescription(description);
        artifactTypeDataDefinition.setType(type);
        artifactTypeDataDefinition.setName(type + "name");
        artifactTypeDataDefinition.setDerivedFrom(derivedFrom);
        ArtifactTypeDefinition artifactTypeDefinition = new ArtifactTypeDefinition(artifactTypeDataDefinition);

        if (props != null) {
            artifactTypeDefinition.setProperties(asList(props));
        }
        return artifactTypeDefinition;
    }

}