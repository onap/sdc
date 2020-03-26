/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.info;

import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class GenericArtifactQueryInfoTest {

    private static final String ARTIFACT_UNIQUE_ID = "artifactId";
    private static final String PARENT_ID = "parentId";
    private static final Set<String> FIELDS = Collections.emptySet();

    @Test
    public void shouldTestConstructorCorrectlySetFields(){
        GenericArtifactQueryInfo genericArtifactQueryInfo = new GenericArtifactQueryInfo(FIELDS, PARENT_ID,
            ARTIFACT_UNIQUE_ID);
        assertThat(genericArtifactQueryInfo.getArtifactUniqueId(), is(ARTIFACT_UNIQUE_ID));
        assertThat(genericArtifactQueryInfo.getFields(), is(FIELDS));
        assertThat(genericArtifactQueryInfo.getParentId(), is(PARENT_ID));
    }
}