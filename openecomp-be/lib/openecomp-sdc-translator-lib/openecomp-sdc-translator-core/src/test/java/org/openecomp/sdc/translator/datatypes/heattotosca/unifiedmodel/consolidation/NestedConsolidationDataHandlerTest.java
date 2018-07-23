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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class NestedConsolidationDataHandlerTest {

    private NestedConsolidationData nestedConsolidationData = new NestedConsolidationData();

    @Mock
    NestedConsolidationData nestedConsolidationDataMock;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void isNestedConsolidationDataExist() {
        String nestedFileName = "nestedFileName";
        NestedConsolidationDataHandler nestedConsolidationDataHandler =
                new NestedConsolidationDataHandler(nestedConsolidationDataMock);

        nestedConsolidationDataHandler.isNestedConsolidationDataExist(nestedFileName);

        Mockito.verify(nestedConsolidationDataMock).isNestedConsolidationDataExist(nestedFileName);
    }
}
