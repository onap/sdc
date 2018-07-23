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

public class NestedConsolidationDataTest {

    @Test
    public void isNestedConsolidationDataExistNotNull() {
        String nestedFileName = "nestedFileName";

        NestedConsolidationData nestedConsolidationData = new NestedConsolidationData();
        nestedConsolidationData.setFileNestedConsolidationData(nestedFileName, new FileNestedConsolidationData());

        Assert.assertTrue(nestedConsolidationData.isNestedConsolidationDataExist(nestedFileName));
    }

    @Test
    public void isNestedConsolidationDataExistNull() {
        NestedConsolidationData nestedConsolidationData = new NestedConsolidationData();

        Assert.assertFalse(nestedConsolidationData.isNestedConsolidationDataExist(null));
    }
}
