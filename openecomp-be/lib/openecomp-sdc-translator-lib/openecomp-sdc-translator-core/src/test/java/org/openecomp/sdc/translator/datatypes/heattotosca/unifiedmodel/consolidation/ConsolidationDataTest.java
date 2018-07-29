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

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.translator.services.heattotosca.ConsolidationEntityType;

import java.util.Optional;

public class ConsolidationDataTest {

    private final ConsolidationData consolidationData = new ConsolidationData();

    @Test
    public void testGetConsolidationDataHandler() {

        checkHandlerIsValid(ConsolidationEntityType.COMPUTE, ComputeConsolidationDataHandler.class);
        checkHandlerIsValid(ConsolidationEntityType.PORT, PortConsolidationDataHandler.class);
        checkHandlerIsValid(ConsolidationEntityType.SUB_INTERFACE, SubInterfaceConsolidationDataHandler.class);
        checkHandlerIsValid(ConsolidationEntityType.NESTED, NestedConsolidationDataHandler.class);
        checkHandlerIsValid(ConsolidationEntityType.VFC_NESTED, NestedConsolidationDataHandler.class);
    }

    @Test
    public void testGetConsolidationDataHandler_Negative() {
        checkHandlerNotExist(ConsolidationEntityType.OTHER);
        checkHandlerNotExist(ConsolidationEntityType.VOLUME);
    }

    private void checkHandlerIsValid(ConsolidationEntityType consolidationEntityType, Class cls) {
        Optional<ConsolidationDataHandler> consolidationDataHandler =
                consolidationData.getConsolidationDataHandler(consolidationEntityType);
        Assert.assertTrue(consolidationDataHandler.isPresent());
        Assert.assertTrue(cls.isInstance(consolidationDataHandler.get()));
    }

    private void checkHandlerNotExist(ConsolidationEntityType consolidationEntityType) {
        Optional<ConsolidationDataHandler> consolidationDataHandler =
                consolidationData.getConsolidationDataHandler(consolidationEntityType);
        Assert.assertFalse(consolidationDataHandler.isPresent());
    }

}
