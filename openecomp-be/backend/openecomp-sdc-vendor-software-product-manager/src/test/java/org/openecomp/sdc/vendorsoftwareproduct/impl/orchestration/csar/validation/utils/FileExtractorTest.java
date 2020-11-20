/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 Nokia. All rights reserved.
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
package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.openecomp.sdc.be.config.NonManoArtifactType.ONAP_PM_DICTIONARY;
import static org.openecomp.sdc.be.test.util.TestResourcesHandler.getResourceBytesOrFail;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding.OnboardingPackageContentHandler;


public class FileExtractorTest {

    private static final String PATH_TO_MANIFEST = "/PATH/TO/MANIFEST/";
    private OnboardingPackageContentHandler contentHandler;

    @BeforeEach
    public void setUp() throws IOException {
        contentHandler = new OnboardingPackageContentHandler();
    }

    @Test
    void shouldExtractPMDictionaryFiles() {
        // given
        final byte[] pmDictionaryContent = "PM_DICTIONARY_CONTENT".getBytes();
        contentHandler.addFile(PATH_TO_MANIFEST,
            getResourceBytesOrFail("validation.files/manifest/manifestCompliantWithSOL004.mf"));
        contentHandler.addFile("Files/Measurements/PM_Dictionary.yaml", pmDictionaryContent);

        // when
        final List<byte[]> filesContents = new FileExtractor(PATH_TO_MANIFEST, contentHandler)
            .findFiles(ONAP_PM_DICTIONARY)
            .collect(Collectors.toList());

        // then
        assertThat(filesContents.size(), is(1));
        assertThat(filesContents.get(0), is(pmDictionaryContent));
    }

    @Test
    void shouldReturnEmptyStream_whenPmDictionaryIsMissing() {
        // given
        contentHandler.addFile(PATH_TO_MANIFEST,
            getResourceBytesOrFail("validation.files/manifest/sampleManifest2.mf"));

        // when
        final List<byte[]> filesContents = new FileExtractor(PATH_TO_MANIFEST, contentHandler)
            .findFiles(ONAP_PM_DICTIONARY)
            .collect(Collectors.toList());

        // then
        assertThat(filesContents.size(), is(0));
    }
}
