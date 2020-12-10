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
package org.onap.sdc.backend.ci.tests.validation.pmdictionary;

import org.apache.commons.io.IOUtils;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.vendorsoftwareproduct.exception.OnboardPackageException;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardPackage;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardPackageInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class CsarLoader {

    private CsarLoader() {
    }

    public static FileContentHandler load(String csarFileName, String csarFilePath) throws IOException, OnboardPackageException {
        InputStream inputStream = CsarLoader.class.getResourceAsStream(csarFilePath);
        OnboardPackage onboardPackage = getOnboardPackage(csarFileName, inputStream);
        return onboardPackage.getFileContentHandler();
    }

    private static OnboardPackage getOnboardPackage(String csarFileName, InputStream inputStream) throws OnboardPackageException, IOException {
        OnboardPackageInfo onboardPackageInfo = new OnboardPackageInfo(csarFileName, OnboardingTypesEnum.CSAR.toString(),
                convertFileInputStream(inputStream), OnboardingTypesEnum.CSAR);
        return onboardPackageInfo.getOnboardPackage();
    }

    private static ByteBuffer convertFileInputStream(final InputStream fileInputStream) throws IOException {
        byte[] fileContent = IOUtils.toByteArray(fileInputStream);
        return ByteBuffer.wrap(fileContent);
    }
}
