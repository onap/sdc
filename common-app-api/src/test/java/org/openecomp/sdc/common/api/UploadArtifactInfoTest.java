/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.common.api;

import org.junit.Assert;
import org.junit.Test;

public class UploadArtifactInfoTest {

    private UploadArtifactInfo createTestSubject() {
        return new UploadArtifactInfo();
    }

    @Test
    public void testGetArtifactName() throws Exception {
        UploadArtifactInfo testSubject;
        String result;
        // default test
        testSubject = createTestSubject();
        result = testSubject.getArtifactName();
    }

    @Test
    public void testSetArtifactName() throws Exception {
        UploadArtifactInfo testSubject;
        String artifactName = "";
        // default test
        testSubject = createTestSubject();
        testSubject.setArtifactName(artifactName);
    }

    @Test
    public void testGetArtifactPath() throws Exception {
        UploadArtifactInfo testSubject;
        String result;
        // default test
        testSubject = createTestSubject();
        result = testSubject.getArtifactPath();
    }

    @Test
    public void testSetArtifactPath() throws Exception {
        UploadArtifactInfo testSubject;
        String artifactPath = "";
        // default test
        testSubject = createTestSubject();
        testSubject.setArtifactPath(artifactPath);
    }

    @Test
    public void testGetArtifactType() throws Exception {
        UploadArtifactInfo testSubject;
        ArtifactTypeEnum result;
        // default test
        testSubject = createTestSubject();
        result = testSubject.getArtifactType();
    }

    @Test
    public void testSetArtifactType() throws Exception {
        UploadArtifactInfo testSubject;
        ArtifactTypeEnum artifactType = null;
        // default test
        testSubject = createTestSubject();
        testSubject.setArtifactType(artifactType);
    }

    @Test
    public void testGetArtifactDescription() throws Exception {
        UploadArtifactInfo testSubject;
        String result;
        // default test
        testSubject = createTestSubject();
        result = testSubject.getArtifactDescription();
    }

    @Test
    public void testSetArtifactDescription() throws Exception {
        UploadArtifactInfo testSubject;
        String artifactDescription = "";
        // default test
        testSubject = createTestSubject();
        testSubject.setArtifactDescription(artifactDescription);
    }

    @Test
    public void testGetArtifactData() throws Exception {
        UploadArtifactInfo testSubject;
        String result;
        // default test
        testSubject = createTestSubject();
        result = testSubject.getArtifactData();
    }

    @Test
    public void testSetArtifactData() throws Exception {
        UploadArtifactInfo testSubject;
        String artifactData = "";
        // default test
        testSubject = createTestSubject();
        testSubject.setArtifactData(artifactData);
    }

    @Test
    public void testHashCode() throws Exception {
        UploadArtifactInfo testSubject;
        int result;
        // default test
        testSubject = createTestSubject();
        result = testSubject.hashCode();
    }

    @Test
    public void testEquals() throws Exception {
        UploadArtifactInfo testSubject;
        Object obj = null;
        boolean result;
        // test 1
        testSubject = createTestSubject();
        obj = null;
        result = testSubject.equals(obj);
        Assert.assertEquals(false, result);
    }

    @Test
    public void testToString() throws Exception {
        UploadArtifactInfo testSubject;
        String result;
        // default test
        testSubject = createTestSubject();
        result = testSubject.toString();
    }
}
