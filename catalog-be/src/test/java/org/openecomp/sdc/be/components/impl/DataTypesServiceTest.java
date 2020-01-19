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

package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

public class DataTypesServiceTest {
    ApplicationDataTypeCache applicationDataTypeCache = Mockito.mock(ApplicationDataTypeCache.class);
    ComponentsUtils componentsUtils = Mockito.mock(ComponentsUtils.class);

    DataTypesService dataTypesService = new DataTypesService(componentsUtils);
    Map<String, DataTypeDefinition> mapreturn = new HashMap<>();
    JanusGraphOperationStatus janusGraphOperationStatus = JanusGraphOperationStatus.NOT_FOUND;
    Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes;

    @Before
    public void setup() {
        mapreturn.put("Demo",new DataTypeDefinition());
        allDataTypes = Either.left(mapreturn);
        when(applicationDataTypeCache.getAll()).thenReturn(allDataTypes);

    }

    @Test
    public void getAllDataTypes_success() {
        Assert.assertEquals(true,dataTypesService.getAllDataTypes(applicationDataTypeCache).isLeft());
    }

    @Test
    public void getAllDataTypes_failure() {
        allDataTypes = Either.right(janusGraphOperationStatus);
        when(applicationDataTypeCache.getAll()).thenReturn(allDataTypes);
        Assert.assertEquals(true,dataTypesService.getAllDataTypes(applicationDataTypeCache).isRight());
    }

}
