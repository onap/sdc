/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.translator.services.heattotosca.ConsolidationEntityType;

public class ConsolidationDataTest {

    private final ConsolidationData consolidationData = new ConsolidationData();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetConsolidationDataHandler() {

        verifyHandlerIsValid(ConsolidationEntityType.COMPUTE, ComputeConsolidationDataHandler.class);
        verifyHandlerIsValid(ConsolidationEntityType.PORT, PortConsolidationDataHandler.class);
        verifyHandlerIsValid(ConsolidationEntityType.SUB_INTERFACE, SubInterfaceConsolidationDataHandler.class);
        verifyHandlerIsValid(ConsolidationEntityType.NESTED, NestedConsolidationDataHandler.class);
        verifyHandlerIsValid(ConsolidationEntityType.VFC_NESTED, NestedConsolidationDataHandler.class);
    }

    @Test
    public void testGetConsolidationDataHandler_Negative() {
        verifyHandlerNotExist(ConsolidationEntityType.OTHER);
        verifyHandlerNotExist(ConsolidationEntityType.VOLUME);
    }

    private void verifyHandlerIsValid(ConsolidationEntityType consolidationEntityType, Class cls) {
        Optional<ConsolidationDataHandler> consolidationDataHandler =
                consolidationData.getConsolidationDataHandler(consolidationEntityType);
        Assert.assertTrue(consolidationDataHandler.isPresent());
        cls.isInstance(consolidationDataHandler.get());
    }

    private void verifyHandlerNotExist(ConsolidationEntityType consolidationEntityType) {
        Optional<ConsolidationDataHandler> consolidationDataHandler =
                consolidationData.getConsolidationDataHandler(consolidationEntityType);
        Assert.assertFalse(consolidationDataHandler.isPresent());
    }

}
