/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.info;

import org.junit.Test;
import org.openecomp.sdc.be.resources.data.ESArtifactData;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ArtifactAccessInfoTest {

    @Test
    public void shouldHaveValidGettersAndSetters() {
        assertThat(ArtifactAccessInfo.class, hasValidGettersAndSetters());
    }

    @Test
    public void testArtifactAccessInfoConstructorUsingESArtifactData() {
        ArtifactAccessInfo artifactAccessInfo = new ArtifactAccessInfo(new ESArtifactData("anyId"));
        assertThat(artifactAccessInfo.getId(), is("anyId"));
    }

    @Test
    public void testArtifactAccessInfoConstructorUsingServletContext() {
        ArtifactAccessInfo artifactAccessInfo = new ArtifactAccessInfo("http://localhost/test");
        assertThat(artifactAccessInfo.getUrl(), is("http://localhost/test/resources/artifacts/"));
    }

}