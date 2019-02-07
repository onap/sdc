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

package org.openecomp.sdc.be.nodeFilter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openecomp.sdc.be.impl.ServiceFilterUtils;

public class ServiceFilterUtilsPropertyRemovedTest extends BaseServiceFilterUtilsTest {


    @Test
    public void checkPropertyIsFound() {
        assertTrue(ServiceFilterUtils.isNodeFilterAffectedByPropertyRemoval(service, CI_NAME, SIZE_PROP));
    }

    @Test
    public void checkPropertyIsNotFound() {
        assertFalse(ServiceFilterUtils.isNodeFilterAffectedByPropertyRemoval(service, CI_NAME, A_PROP_NAME + "XXXX"));
    }

}
