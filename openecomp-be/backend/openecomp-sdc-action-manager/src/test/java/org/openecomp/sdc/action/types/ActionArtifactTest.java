/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.action.types;

import org.junit.Test;
import org.openecomp.sdc.action.dao.types.ActionArtifactEntity;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.MatcherAssert.assertThat;
public class ActionArtifactTest {

    ActionArtifact actionArtifact = new ActionArtifact();

    @Test
    public void shouldHaveValidGettersAndSetters() {
        assertThat(ActionArtifact.class, hasValidGettersAndSetters());
    }
  
    @Test
    public void testToEntity() {
        ActionArtifactEntity destination = actionArtifact.toEntity();
        assertNotNull(destination);
        assertEqualsMultipleAssert(actionArtifact ,destination);
    }
  
    @Test
    public void testEqual() {
        assertEquals(true, actionArtifact.equals(actionArtifact));
    }

    private void assertEqualsMultipleAssert(ActionArtifact source, ActionArtifactEntity destination) {
        assertEquals(source.getArtifactUuId().toUpperCase(), destination.getArtifactUuId());
        assertEquals(source.getEffectiveVersion(), new Integer(destination.getEffectiveVersion()));
        assertEquals(ByteBuffer.wrap(source.getArtifact()), destination.getArtifact());
    }
}