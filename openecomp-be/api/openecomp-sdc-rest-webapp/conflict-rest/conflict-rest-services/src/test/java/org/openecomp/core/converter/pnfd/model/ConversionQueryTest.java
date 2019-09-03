/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
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

package org.openecomp.core.converter.pnfd.model;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class ConversionQueryTest {

    @Test
    public void isValidAttributeQueryTest() {
        ConversionQuery conversionQuery = new ConversionQuery(new HashMap<>());
        assertThat("Map query should be valid", conversionQuery.isValidAttributeQuery(), is(true));
        conversionQuery = new ConversionQuery(null);
        assertThat("Non Map query should be invalid", conversionQuery.isValidAttributeQuery(), is(false));
        conversionQuery = new ConversionQuery("query");
        assertThat("Non Map query should be invalid", conversionQuery.isValidAttributeQuery(), is(false));
    }
}