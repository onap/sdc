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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ActionArtifactTest {

    ActionArtifact actionArtifact = new ActionArtifact();

    @Before
    public void setup() {
        actionArtifact.setArtifactCategory("new Artifact Category");
        actionArtifact.setArtifactDescription("Artifact for policy");
        actionArtifact.setArtifactLabel("policy");
        actionArtifact.setArtifactName("Policy Artifact");
        actionArtifact.setArtifactProtection("protected");
        actionArtifact.setArtifactUuId("1142-0097");
        actionArtifact.setEffectiveVersion(1);
    }

    @Test
    public void testArtifactCategory () {
        assertEquals(actionArtifact.getArtifactCategory(), "new Artifact Category");
    }

    @Test
    public void testArtifactDescription() {
        assertEquals(actionArtifact.getArtifactDescription(), "Artifact for policy");
    }

    @Test
    public void testArtifactLabel() {
        assertEquals(actionArtifact.getArtifactLabel(), "policy");
    }

    @Test
    public void testArtifactName() {
        assertEquals(actionArtifact.getArtifactName(), "Policy Artifact");
    }

    @Test
    public void testArtifactProtection() {
        assertEquals(actionArtifact.getArtifactProtection(), "protected");
    }

    @Test
    public void testArtifactUuid() {
        assertEquals(actionArtifact.getArtifactUuId(), "1142-0097");
    }

    @Test
    public void testEffectiveVersion() {
        assertEquals(actionArtifact.getEffectiveVersion(), new Integer(1));
    }
}