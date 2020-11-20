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
package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.tosca.csar.ManifestTokenType;
import org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding.OnboardingPackageContentHandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.openecomp.sdc.be.config.NonManoArtifactType.ONAP_PM_DICTIONARY;
import static org.openecomp.sdc.be.test.util.TestResourcesHandler.getResourceBytesOrFail;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.*;

public class PMDictionaryValidatorTest {

    private PMDictionaryValidator pmDictionaryValidator;
    private OnboardingPackageContentHandler contentHandler;
    private ManifestBuilder manifestBuilder;
    private String nonManoSource = "Files/Measurements/PM_Dictionary.yaml";
    private String failReasonMessage = "The actual error message and expected error message lists should be the same";

    @Before
    public void setUp() throws IOException {
        manifestBuilder = getPnfManifestSampleBuilder();
        contentHandler = new OnboardingPackageContentHandler();
    }

    @Test
    public void givenPmDictionaryFile_whenIsEmpty_thenErrorMessageListShouldContainsEmptyYamlMessage() {

        //given
        contentHandler.addFile(nonManoSource, getResourceBytesOrFail("validation.files/empty.yaml"));
        manifestBuilder.withNonManoArtifact(ONAP_PM_DICTIONARY.getType(), nonManoSource);
        List<String> expectedErrorMessageList = Arrays.asList("PM_Dictionary YAML file is empty");
        List<String> paths = Arrays.asList(nonManoSource);
        pmDictionaryValidator = new PMDictionaryValidator(nonManoSource, contentHandler);

        //when
        List<String> errorMessageList = pmDictionaryValidator.validatePmDict(paths);

        //then
        assertThat(failReasonMessage
                , errorMessageList, containsInAnyOrder(expectedErrorMessageList.toArray())
        );
    }

    @Test
    public void givenPmDictionaryFile_whenIsCorrect_thenErrorMessageListShouldBeEmpty() {

        //given
        contentHandler.addFile(nonManoSource, getResourceBytesOrFail("validation.files/measurements/pmEvents-valid.yaml"));
        manifestBuilder.withNonManoArtifact(ONAP_PM_DICTIONARY.getType(), nonManoSource);
        List<String> paths = Arrays.asList(nonManoSource);
        pmDictionaryValidator = new PMDictionaryValidator(nonManoSource, contentHandler);

        //when
        List<String> errorMessageList = pmDictionaryValidator.validatePmDict(paths);

        //then
        assertThat("The actual error message should be empty"
                ,  errorMessageList, is(empty())
        );
    }

    @Test
    public void givenPmDictionaryFile_whenPmDictionaryHeaderIsMissing_thenErrorMessageListShouldContainsKeyNotFoundMessage() {

        //given
        contentHandler.addFile(nonManoSource, getResourceBytesOrFail("validation.files/measurements/pmEvents-invalid.yaml"));
        manifestBuilder.withNonManoArtifact(ONAP_PM_DICTIONARY.getType(), nonManoSource);
        List<String> expectedErrorMessageList = Arrays.asList("Key not found: pmDictionaryHeader");
        List<String> paths = Arrays.asList(nonManoSource);
        pmDictionaryValidator = new PMDictionaryValidator(nonManoSource, contentHandler);

        //when
        List<String> errorMessageList = pmDictionaryValidator.validatePmDict(paths);

        //then
        assertThat(failReasonMessage
                , errorMessageList, containsInAnyOrder(expectedErrorMessageList.toArray())
        );
    }

    private ManifestBuilder getPnfManifestSampleBuilder() {
        return new ManifestBuilder()
                .withMetaData(PNFD_NAME.getToken(), "myPnf")
                .withMetaData(ManifestTokenType.PNFD_PROVIDER.getToken(), "ACME")
                .withMetaData(PNFD_ARCHIVE_VERSION.getToken(), "1.0")
                .withMetaData(PNFD_RELEASE_DATE_TIME.getToken(), "2019-03-11T11:25:00+00:00");
    }
}
