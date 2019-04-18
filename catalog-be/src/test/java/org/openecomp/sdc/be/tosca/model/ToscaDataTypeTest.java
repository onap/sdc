/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Fujitsu Limited. All rights reserved.
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

package org.openecomp.sdc.be.tosca.model;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ToscaDataTypeTest {
    private static final String DERIVED_FROM = "tosca.datatypes.Root";
    private static final String VERSION = "1.1";
    private static final String DESCRIPTION = "data type description";
    private static final String PROPERTY_NAME = "prop1";
    private static final String METADATA_KEY = "foo";
    private static final String METADATA_VALUE = "bar";

    private ToscaDataType testSubject;

    @Test
    public void testToscaDataType() throws Exception {
        testSubject = new ToscaDataType();
        testSubject.setDerived_from(DERIVED_FROM);
        testSubject.setVersion(VERSION);
        testSubject.setDescription(DESCRIPTION);
        Map<String, String> metadata = new HashMap<>();
        metadata.put(METADATA_KEY, METADATA_VALUE);
        testSubject.setMetadata(metadata);
        Map<String, ToscaProperty> properties = new HashMap<>();
        properties.put(PROPERTY_NAME, new ToscaProperty());
        testSubject.setProperties(properties);

        assertThat(testSubject.getDerived_from(), is(DERIVED_FROM));
        assertThat(testSubject.getVersion(), is(VERSION));
        assertThat(testSubject.getDescription(), is(DESCRIPTION));
        assertThat(testSubject.getMetadata(), is(metadata));
        assertThat(testSubject.getProperties(), is(properties));
    }
}
