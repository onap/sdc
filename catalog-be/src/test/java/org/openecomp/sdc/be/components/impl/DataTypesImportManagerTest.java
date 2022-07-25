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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import fj.data.Either;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ModelOperation;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;

public class DataTypesImportManagerTest {

    private static final ComponentsUtils componentsUtils = mock(ComponentsUtils.class);
    private static final JanusGraphGenericDao janusGraphGenericDao = mock(JanusGraphGenericDao.class);
    private static final PropertyOperation propertyOperation = mock(PropertyOperation.class);
    private static final ModelOperation modelOperation = mock(ModelOperation.class);
    @Spy
    private CommonImportManager commonImportManager = new CommonImportManager(componentsUtils, propertyOperation, modelOperation);

    @InjectMocks
    private DataTypeImportManager dataTypeImportManager = new DataTypeImportManager();

    private AutoCloseable closeable;

    @BeforeEach
    public void openMocks() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @Test
    public void testCreateDataTypes() {

        DataTypeDefinition rootDataTypeDef = new DataTypeDefinition();
        rootDataTypeDef.setName("tosca.datatypes.Root");
        rootDataTypeDef.setProperties(Collections.emptyList());
        DataTypeDefinition testADataTypeDef = new DataTypeDefinition();
        testADataTypeDef.setName("tosca.datatypes.test_a");
        testADataTypeDef.setProperties(Collections.emptyList());
        DataTypeDefinition testCDataTypeDef = new DataTypeDefinition();
        testCDataTypeDef.setName("tosca.datatypes.test_c");
        testCDataTypeDef.setProperties(Collections.emptyList());
        when(propertyOperation.getDataTypeByName("tosca.datatypes.Root", null)).thenReturn(Either.left(rootDataTypeDef));
        when(propertyOperation.getDataTypeByName("tosca.datatypes.test_c", null)).thenReturn(Either.left(testCDataTypeDef));

        when(propertyOperation.getJanusGraphGenericDao()).thenReturn(janusGraphGenericDao);
        when(propertyOperation.getDataTypeByUidWithoutDerived("tosca.datatypes.Root.datatype", true)).thenReturn(Either.left(rootDataTypeDef));
        when(propertyOperation.getDataTypeByUidWithoutDerived("tosca.datatypes.test_a.datatype", true))
                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        when(propertyOperation.getDataTypeByUidWithoutDerived("tosca.datatypes.test_b.datatype", true))
                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        when(propertyOperation.getDataTypeByUidWithoutDerived("tosca.datatypes.test_c.datatype", true))
                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        when(propertyOperation.addDataType(any())).thenReturn(Either.left(testADataTypeDef));

        String dataTypeYml = 
                "tosca.datatypes.test_a:\n" + 
                "  derived_from: tosca.datatypes.Root\n" + 
                "  properties:\n" + 
                "    prop2:\n" +
                "      type: tosca.datatypes.test_b\n" +
                "tosca.datatypes.test_b:\n" +
                "  derived_from: tosca.datatypes.test_c\n" +
                "  properties:\n" +
                "    prop2:\n" +
                "      type: string\n" +
                "tosca.datatypes.test_c:\n" +
                "  derived_from: tosca.datatypes.Root\n" +
                "  properties:\n" +
                "    prop1:\n" +
                "      type: string";
        dataTypeImportManager.createDataTypes(dataTypeYml, "", false);

        ArgumentCaptor<DataTypeDefinition> dataTypeDefinitionCaptor = ArgumentCaptor.forClass(DataTypeDefinition.class);
        Mockito.verify(propertyOperation, times(3)).addDataType(dataTypeDefinitionCaptor.capture());

        assertEquals("tosca.datatypes.test_c", dataTypeDefinitionCaptor.getAllValues().get(0).getName());
        assertEquals("tosca.datatypes.test_b", dataTypeDefinitionCaptor.getAllValues().get(1).getName());
        assertEquals("tosca.datatypes.test_a", dataTypeDefinitionCaptor.getAllValues().get(2).getName());
    }


}
