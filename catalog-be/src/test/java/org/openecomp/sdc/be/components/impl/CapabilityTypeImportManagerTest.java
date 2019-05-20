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

package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import static org.junit.Assert.assertEquals;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CapabilityTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.common.util.CapabilityTypeNameEnum;
import org.openecomp.sdc.exception.ResponseFormat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CapabilityTypeImportManagerTest {
    private static final CapabilityTypeOperation capabilityTypeOperation = mock(CapabilityTypeOperation.class);
    private static final ComponentsUtils componentsUtils = mock(ComponentsUtils.class);
    private static final JanusGraphGenericDao JANUS_GRAPH_GENERIC_DAO = mock(JanusGraphGenericDao.class);
    private static final PropertyOperation propertyOperation = mock(PropertyOperation.class);
    private CommonImportManager commonImportManager = new CommonImportManager(componentsUtils, propertyOperation);
    private CapabilityTypeImportManager manager = new CapabilityTypeImportManager(capabilityTypeOperation, commonImportManager);

    @BeforeClass
    public static void beforeClass() {
        when(capabilityTypeOperation.addCapabilityType(Mockito.any(CapabilityTypeDefinition.class))).thenAnswer(new Answer<Either<CapabilityTypeDefinition, StorageOperationStatus>>() {
            public Either<CapabilityTypeDefinition, StorageOperationStatus> answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                return Either.left((CapabilityTypeDefinition) args[0]);
            }

        });

        when(propertyOperation.getJanusGraphGenericDao()).thenReturn(JANUS_GRAPH_GENERIC_DAO);
        when(capabilityTypeOperation.getCapabilityType(Mockito.anyString())).thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
    }

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateCapabilityTypes() throws IOException {
        String ymlContent = getCapabilityTypesYml();
        Either<List<ImmutablePair<CapabilityTypeDefinition, Boolean>>, ResponseFormat> createCapabilityTypes = manager.createCapabilityTypes(ymlContent);
        assertTrue(createCapabilityTypes.isLeft());

        List<ImmutablePair<CapabilityTypeDefinition, Boolean>> capabilityTypesList = createCapabilityTypes.left().value();
        assertEquals(14, capabilityTypesList.size());
        Map<String, CapabilityTypeDefinition> capibilityTypeMap = new HashMap<>();
        for (ImmutablePair<CapabilityTypeDefinition, Boolean> capTypePair : capabilityTypesList) {
            capibilityTypeMap.put(capTypePair.left.getType(), capTypePair.left);
        }
        assertEquals(14, capabilityTypesList.size());

        for (CapabilityTypeNameEnum curr : CapabilityTypeNameEnum.values()) {
            assertTrue(capibilityTypeMap.containsKey(curr.getCapabilityName()));
        }

    }

    private String getCapabilityTypesYml() throws IOException {
        Path filePath = Paths.get("src/test/resources/types/capabilityTypes.yml");
        byte[] fileContent = Files.readAllBytes(filePath);
        return new String(fileContent);
    }

}
