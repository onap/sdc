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
package org.openecomp.types;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.Relation;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AsdcElementTest {
    @Test
    public void shouldHaveValidGettersAndSetters() {
        assertThat(AsdcElement.class,
            hasValidGettersAndSettersExcluding("data", "info", "searchableData", "visualization"));
    }

    @Test
    public void getInfoTest() {
        AsdcElement element =  new AsdcElement();
        element.setName("name");
        element.setType("type");
        element.setProperties(new HashMap<>());
        element.setDescription("desc");

        Info actualInfo = element.getInfo();
        assertEquals("name", actualInfo.getName());
        assertEquals(1, actualInfo.getProperties().size());
        assertEquals("type", actualInfo.getProperty("elementType"));
        assertEquals("desc", actualInfo.getDescription());
    }

    @Test
    public void getSearchableDataTest() {
        AsdcElement element =  new AsdcElement();
        assertNull(element.getSearchableData());
    }

    @Test
    public void getVisualizationTest() {
        AsdcElement element =  new AsdcElement();
        assertNull(element.getVisualization());
    }

    @Test
    public void dataTest() throws IOException {
        AsdcElement element =  new AsdcElement();
        InputStream inputStream = new ByteArrayInputStream( "testString".getBytes(StandardCharsets.UTF_8) );
        element.setData(inputStream);

        assertEquals("testString", IOUtils.toString(element.getData(), StandardCharsets.UTF_8));
    }

    @Test
    public void addSubElementTest() throws IOException {
        AsdcElement element =  new AsdcElement();
        assertEquals(0, element.getSubElements().size());

        element.addSubElement(new AsdcElement());
        assertEquals(1, element.getSubElements().size());
    }
}