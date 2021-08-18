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
 */
package org.openecomp.sdc.be.model.operations.impl;

import fj.data.Either;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.sdc.be.client.onboarding.api.OnboardingClient;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.VendorSoftwareProduct;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("csar-operation")
public class CsarOperation {

    private static final Logger LOGGER = Logger.getLogger(CsarOperation.class.getName());

    private final OnboardingClient onboardingClient;

    @Autowired
    public CsarOperation(final OnboardingClient onboardingClient) {
        this.onboardingClient = onboardingClient;
    }

    /**
     * Finds the CSAR package of the latest version of a Vendor Software Product (VSP) from the onboarding repository.
     *
     * @param csarUuid the VSP id
     * @param user     the logged user
     * @return a Map containing the CSAR files <path, bytes> (left), or a StorageOperationStatus if an error occurs (right)
     */
    public Either<Map<String, byte[]>, StorageOperationStatus> findVspLatestPackage(String csarUuid, User user) {
        final Either<Map<String, byte[]>, StorageOperationStatus> result = onboardingClient.findLatestPackage(csarUuid, user.getUserId());
        if (result.isRight()) {
            LOGGER.debug("Could not find VSP Package '{}'. Status '{}'", csarUuid, result.right().value());
            return result;
        }
        if (MapUtils.isNotEmpty(result.left().value())) {
            final Map<String, byte[]> values = result.left().value();
            LOGGER.debug("The returned files are {}", values.keySet());
        }
        return result;
    }

    /**
     * Finds the Vendor Software Product (VSP) from the onboarding repository.
     *
     * @param id        the VSP id
     * @param versionId the VSP version
     * @param user      the logged user
     * @return a VSP representation if found, empty otherwise.
     */
    public Optional<VendorSoftwareProduct> findVsp(final String id, final String versionId, final User user) {
        return onboardingClient.findVendorSoftwareProduct(id, versionId, user.getUserId());
    }

}
