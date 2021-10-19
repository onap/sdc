/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
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
 */
package org.openecomp.sdc.be.components.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.model.Model;
import org.openecomp.sdc.be.model.operations.impl.ArtifactTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.ModelOperation;

@TestInstance(Lifecycle.PER_CLASS)
public class ArtifactTypeImportManagerTest {

    @InjectMocks
    private ArtifactTypeImportManager artifactTypeImportManager = new ArtifactTypeImportManager();
    @Mock
    private CommonImportManager commonImportManager;
    @Mock
    private ModelOperation modelOperation;
    @Mock
    private ArtifactTypeOperation artifactTypeOperation;

    @BeforeAll
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createArtifactTypeFromYmlTest() throws IOException {
        when(commonImportManager.createElementTypesFromYml(Mockito.anyString(), any())).thenCallRealMethod();
        when(commonImportManager.createElementTypesFromToscaJsonMap(any(), any())).thenCallRealMethod();
        when(artifactTypeOperation.createArtifactType(any())).thenCallRealMethod();
        when(modelOperation.findModelByName("test")).thenReturn(Optional.of(new Model("test")));
        final var result = artifactTypeImportManager
            .createArtifactTypes(getArtifactsYml(), "test", false);
        assertThat("The createElementTypesFromYml should be left", result.isLeft(), is(true));
        final var artifactTypes = result.left().value();
        assertThat("The artifact types list should have size", artifactTypes, hasSize(1));
    }

    private String getArtifactsYml() throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/test/resources/types/artifactTypes.yml")));
    }
}
