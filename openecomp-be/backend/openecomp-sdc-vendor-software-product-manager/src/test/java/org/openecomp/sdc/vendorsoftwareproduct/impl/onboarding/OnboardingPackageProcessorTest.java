/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.openecomp.sdc.common.errors.Messages.PACKAGE_EMPTY_ERROR;
import static org.openecomp.sdc.common.errors.Messages.PACKAGE_INVALID_EXTENSION;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardPackageInfo;

@RunWith(Parameterized.class)
public class OnboardingPackageProcessorTest {
    private static final String BASE_DIR = "/vspmanager.csar/";
    private final String packageName;
    private final byte[] packageBytes;
    private final Set<ErrorMessage> expectedErrorSet;
    private final OnboardingTypesEnum expectedPackageType;

    public OnboardingPackageProcessorTest(final String packageName, final byte[] packageBytes,
                                          final Set<ErrorMessage> expectedErrorSet,
                                          final OnboardingTypesEnum expectedPackageType) {
        this.packageName = packageName;
        this.packageBytes = packageBytes;
        this.expectedErrorSet = expectedErrorSet;
        this.expectedPackageType = expectedPackageType;
    }

    @Parameters(name = "Run {index} for {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"emptyPackage.csar", new byte[0],
                ImmutableSet.of(
                    new ErrorMessage(ErrorLevel.ERROR, PACKAGE_EMPTY_ERROR.formatMessage("emptyPackage.csar"))
                ), null},

            {"notCsar.txt", getFileBytes("notCsar.txt"),
                ImmutableSet.of(
                    new ErrorMessage(ErrorLevel.ERROR,
                        PACKAGE_INVALID_EXTENSION.formatMessage("notCsar.txt", "csar, zip"))
                ), null},

            {"signed-package.zip", getFileBytes("signing/signed-package.zip"), Collections.emptySet(),
                OnboardingTypesEnum.SIGNED_CSAR},

            {"csar-and-cms-in-root.zip", getFileBytes("signing/csar-and-cms-in-root.zip"), Collections.emptySet(),
                OnboardingTypesEnum.SIGNED_CSAR},

            {"successfulUpload.csar", getFileBytes("successfulUpload.csar"), Collections.emptySet(),
                OnboardingTypesEnum.CSAR},

            {"fakeNonSignedZipPackage.zip", getFileBytes("signing/fakeNonSignedZipPackage.zip"), Collections.emptySet(),
                OnboardingTypesEnum.ZIP}
        });
    }

    @Test
    public void processPackage() {
        final OnboardingPackageProcessor onboardingPackageProcessor = new OnboardingPackageProcessor(packageName, packageBytes);
        assertThat("Should contains errors", onboardingPackageProcessor.hasErrors(), is(!expectedErrorSet.isEmpty()));
        assertThat("Should have the same number of errors", onboardingPackageProcessor.getErrorMessageSet().size(), equalTo(expectedErrorSet.size()));
        if (expectedErrorSet.size() > 0) {
            assertThat("Should have the expected errors", onboardingPackageProcessor.getErrorMessageSet(), containsInAnyOrder(expectedErrorSet.toArray()));
            return;
        }
        final OnboardPackageInfo onboardPackageInfo = onboardingPackageProcessor.getOnboardPackageInfo().orElse(null);
        assertThat("Should build onboardPackageInfo", onboardPackageInfo, is(notNullValue()));
        assertThat("Should have the expected package type", onboardPackageInfo.getPackageType(), is(equalTo(expectedPackageType)));
    }

    private static byte[] getFileBytes(final String filePath) {
        final Path path = Paths.get(BASE_DIR, filePath);
        try {
            return Files.readAllBytes(Paths.get(
                OnboardingPackageProcessorTest.class.getResource(path.toString()).toURI()));
        } catch (final IOException | URISyntaxException e) {
            fail(String.format("Could not load file %s", path.toString()));
        }
        return null;
    }

}