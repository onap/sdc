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

package org.openecomp.sdc.vendorsoftwareproduct.types;

import java.nio.ByteBuffer;
import lombok.Getter;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.vendorsoftwareproduct.exception.OnboardPackageException;

@Getter
public class OnboardPackageInfo {

    private final OnboardingTypesEnum packageType;
    private final OnboardPackage originalOnboardPackage;
    private final OnboardPackage onboardPackage;

    public OnboardPackageInfo(final OnboardPackage onboardPackage, final OnboardingTypesEnum packageType) {
        this(onboardPackage, onboardPackage, packageType);
    }

    public OnboardPackageInfo(final OnboardPackage originalOnboardPackage,
                              final OnboardPackage onboardPackage, final OnboardingTypesEnum packageType) {
        this.packageType = packageType;
        this.originalOnboardPackage = originalOnboardPackage;
        this.onboardPackage = onboardPackage;
    }

    public OnboardPackageInfo(final String filename,
                              final String fileExtension,
                              final ByteBuffer fileContent,
                              final OnboardingTypesEnum packageType) throws OnboardPackageException {
        this.packageType = packageType;
        originalOnboardPackage = new OnboardPackage(filename, fileExtension, fileContent);
        this.onboardPackage = originalOnboardPackage;
    }

}