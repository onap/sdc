/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.client.onboarding.api;

import fj.data.Either;
import java.util.Map;
import java.util.Optional;
import org.openecomp.sdc.be.model.VendorSoftwareProduct;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

public interface OnboardingClient {

    /**
     * Finds the CSAR package of the latest version of a Vendor Software Product (VSP) from the onboarding repository.
     *
     * @param vspId  the VSP id
     * @param userId the logged user id
     * @return a Map containing the CSAR files <path, bytes> (left), or a StorageOperationStatus if an error occurs (right)
     */
    Either<Map<String, byte[]>, StorageOperationStatus> findLatestPackage(String vspId, String userId);

    Either<Map<String, byte[]>, StorageOperationStatus> findPackage(String vspId, String versionId, String userId);

    /**
     * Finds the Vendor Software Product (VSP) from the onboarding repository.
     *
     * @param id        the VSP id
     * @param versionId the VSP version
     * @param userId    the logged user id
     * @return a VSP representation if found, empty otherwise.
     */
    Optional<VendorSoftwareProduct> findVendorSoftwareProduct(String id, String versionId, String userId);

    /**
     * Finds the latest version of the Vendor Software Product (VSP) from the onboarding repository.
     *
     * @param id        the VSP id
     * @param userId    the logged user id
     * @return a VSP representation if found, empty otherwise.
     */
    Optional<VendorSoftwareProduct> findLatestVendorSoftwareProduct(String id, String userId);

}
