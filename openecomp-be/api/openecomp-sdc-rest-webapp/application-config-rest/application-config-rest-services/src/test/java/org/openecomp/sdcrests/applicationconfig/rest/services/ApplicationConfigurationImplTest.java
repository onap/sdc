/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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

package org.openecomp.sdcrests.applicationconfig.rest.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openecomp.core.utilities.applicationconfig.dao.type.ApplicationConfigEntity;
import org.openecomp.core.utilities.applicationconfig.type.ConfigurationData;
import org.openecomp.sdc.applicationconfig.ApplicationConfigManager;
import org.openecomp.sdcrests.applicationconfiguration.types.ApplicationConfigDto;
import org.openecomp.sdcrests.applicationconfiguration.types.ConfigurationDataDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.http.HttpStatus;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class ApplicationConfigurationImplTest {


    private ApplicationConfigurationImpl applicationConfiguration;

    @Mock
    ApplicationConfigManager applicationConfigManager;

    @Before
    public void setUp() {
        openMocks(this);
        applicationConfiguration = new ApplicationConfigurationImpl(applicationConfigManager);
    }

    @Test
    public void validateInsertInToTableCallsManagerFunctionWithValidParameters() {

        final String testNamespace = "namespace";
        final String testKey = "key";
        final String testValue = "testingValue";
        final InputStream testInput = new ByteArrayInputStream(testValue.getBytes());

        Response response = applicationConfiguration.insertToTable(testNamespace, testKey, testInput);

        assertEquals(response.getStatus(), HttpStatus.OK.value());
        verify(applicationConfigManager).insertIntoTable(eq(testNamespace),eq(testKey),eq(testValue));
    }

    @Test
    public void validateGetFromTableReturnsValidObject() {

        final String testNamespace = "namespace";
        final String testKey = "key";
        final ConfigurationData testValue = new ConfigurationData("testValue", 111);

        when(applicationConfigManager.getFromTable(eq(testNamespace),eq(testKey))).thenReturn(testValue);

        Response response = applicationConfiguration.getFromTable(testNamespace, testKey);

        assertEquals(response.getEntity().getClass(), ConfigurationDataDto.class);
        assertEquals(((ConfigurationDataDto)response.getEntity()).getValue(),testValue.getValue());
    }


    @Test
    public void validateGetListOfConfigurationByNamespaceFromTableReturnsValidList() {

        final String testNamespace = "namespace";
        final ArrayList<ApplicationConfigEntity> testApplicationConfigEntities = new ArrayList<>();
        final ApplicationConfigEntity testConfigEntity01 = new ApplicationConfigEntity();
        final String testValue01 = "testValue01";
        final ApplicationConfigEntity testConfigEntity02 = new ApplicationConfigEntity();
        final String testValue02 = "testValue02";
        testConfigEntity01.setValue(testValue01);
        testConfigEntity02.setValue(testValue02);
        testApplicationConfigEntities.add(testConfigEntity01);
        testApplicationConfigEntities.add(testConfigEntity02);

        when(applicationConfigManager.getListOfConfigurationByNamespace(eq(testNamespace)))
                .thenReturn(testApplicationConfigEntities);

        Response response = applicationConfiguration.getListOfConfigurationByNamespaceFromTable(testNamespace);

        assertEquals(response.getEntity().getClass(), GenericCollectionWrapper.class);
        assertEquals(
                ((GenericCollectionWrapper)response.getEntity()).getResults().size(),
                testApplicationConfigEntities.size()
        );
        assertEquals(
                ((ApplicationConfigDto)response.readEntity(GenericCollectionWrapper.class).getResults().get(0)).getValue(),
                testApplicationConfigEntities.get(0).getValue()
        );
        assertEquals(
                ((ApplicationConfigDto)response.readEntity(GenericCollectionWrapper.class).getResults().get(1)).getValue(),
                testApplicationConfigEntities.get(1).getValue()
        );
    }

}
