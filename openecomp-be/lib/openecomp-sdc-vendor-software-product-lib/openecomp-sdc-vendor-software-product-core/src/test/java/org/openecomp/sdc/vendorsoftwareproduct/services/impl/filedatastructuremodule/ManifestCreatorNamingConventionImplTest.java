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

package org.openecomp.sdc.vendorsoftwareproduct.services.impl.filedatastructuremodule;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ManifestCreatorNamingConventionImplTest extends ManifestCreatorNamingConventionImpl {

    private static final String ARTIFACT_1 = "cloudtech_k8s_charts.zip";
    private static final String ARTIFACT_2 = "cloudtech_azure_day0.zip";
    private static final String ARTIFACT_3 = "cloudtech_aws_configtemplate.zip";
    private static final String ARTIFACT_4 = "k8s_charts.zip";
    private static final String ARTIFACT_5 = "cloudtech_openstack_configtemplate.zip";

    @Test
    public void testIsCloudSpecificArtifact() {
        assertTrue(isCloudSpecificArtifact(ARTIFACT_1));
        assertTrue(isCloudSpecificArtifact(ARTIFACT_2));
        assertTrue(isCloudSpecificArtifact(ARTIFACT_3));
        assertFalse(isCloudSpecificArtifact(ARTIFACT_4));
        assertFalse(isCloudSpecificArtifact(ARTIFACT_5));
    }
}
