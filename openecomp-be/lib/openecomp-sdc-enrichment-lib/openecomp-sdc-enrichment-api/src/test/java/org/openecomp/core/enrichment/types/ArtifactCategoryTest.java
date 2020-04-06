/*
 * Copyright © 2020 Samsung
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

package org.openecomp.core.enrichment.types;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class ArtifactCategoryTest {
    public static final String DISPLAY_NAME_INFO = "Informational";
    public static final String DISPLAY_NAME_DEPL = "Deployment";
    public static final String DISPLAY_NAME_NON_EXIST = "NonExistentName";

    @Test
    public void testCtor() {
        ArtifactCategory artifactCategoryInf = ArtifactCategory.INFORMATIONAL;
        ArtifactCategory artifactCategoryDepl = ArtifactCategory.DEPLOYMENT;

        assertThat(artifactCategoryInf.getDisplayName(), is(DISPLAY_NAME_INFO));
        assertThat(artifactCategoryDepl.getDisplayName(), is(DISPLAY_NAME_DEPL));
    }

    @Test
    public void testGetArtifactTypeByDisplayName() {
        ArtifactCategory result = ArtifactCategory.getArtifactTypeByDisplayName(DISPLAY_NAME_INFO);
        assertThat(result, is(ArtifactCategory.INFORMATIONAL));
    }

    @Test
    public void testGetArtifactTypeByDisplayNameNonExistentName() {
        ArtifactCategory result = ArtifactCategory.getArtifactTypeByDisplayName(DISPLAY_NAME_NON_EXIST);
        assertThat(result, nullValue());
    }

    @Test
    public void testGetArtifactTypeByDisplayNameNullName() {
        ArtifactCategory result = ArtifactCategory.getArtifactTypeByDisplayName(null);
        assertThat(result, nullValue());
    }
}