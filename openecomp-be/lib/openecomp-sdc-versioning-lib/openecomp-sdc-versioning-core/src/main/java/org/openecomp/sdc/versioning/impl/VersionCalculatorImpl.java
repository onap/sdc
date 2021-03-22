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
package org.openecomp.sdc.versioning.impl;

import java.util.HashSet;
import java.util.Set;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.versioning.VersionCalculator;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.VersionCreationMethod;

public class VersionCalculatorImpl implements VersionCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(VersionCalculatorImpl.class);
    private static final String INITIAL_VERSION = "1.0";
    private static final String VERSION_STRING_VIOLATION_MSG = "Version string must be in the format of: {integer}.{integer}";
    private static final String INVALID_CREATION_METHOD_MSG = "Invalid creation method";

    @Override
    public String calculate(String baseVersion, VersionCreationMethod creationMethod) {
        if (baseVersion == null) {
            return INITIAL_VERSION;
        }
        String[] versionLevels = baseVersion.split("\\.");
        if (versionLevels.length != 2) {
            throw new IllegalArgumentException(VERSION_STRING_VIOLATION_MSG);
        }
        int index;
        switch (creationMethod) {
            case major:
                index = Integer.parseInt(versionLevels[0]);
                index++;
                versionLevels[0] = Integer.toString(index);
                versionLevels[1] = "0";
                break;
            case minor:
                index = Integer.parseInt(versionLevels[1]);
                index++;
                versionLevels[1] = Integer.toString(index);
                break;
        }
        return CommonMethods.arrayToSeparatedString(versionLevels, '.');
    }

    @Override
    public void injectAdditionalInfo(Version version, Set<String> existingVersions) {
        String optionalVersion;
        Set<VersionCreationMethod> optionalCreationMethods = new HashSet<>();
        if (version.getStatus().equals(VersionStatus.Certified)) {
            for (VersionCreationMethod versionCreationMethod : VersionCreationMethod.values()) {
                try {
                    optionalVersion = calculate(version.getName(), versionCreationMethod);
                    if (!existingVersions.contains(optionalVersion)) {
                        optionalCreationMethods.add(versionCreationMethod);
                    }
                } catch (IllegalArgumentException iae) {
                    LOGGER.error("{}:{}", INVALID_CREATION_METHOD_MSG, versionCreationMethod.name(), iae);
                }
            }
        }
        version.getAdditionalInfo().put("OptionalCreationMethods", optionalCreationMethods);
    }
}
