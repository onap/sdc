/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
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

package org.openecomp.sdcrests.vsp.rest.services;

import java.util.Optional;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspUploadStatusDto;

public interface PackageUploadManager {

    /**
     * Creates a lock to start uploading a package.
     *
     * @param vspId        the Vendor Software Product id
     * @param vspVersionId the Vendor Software Product version id
     * @param user         the current user
     * @return a new upload status containing the lock
     */
    VspUploadStatusDto startUpload(String vspId, String vspVersionId, String user);

    /**
     * Finds the latest upload status for a given Vendor Software Product version.
     *
     * @param vspId        the Vendor Software Product id
     * @param vspVersionId the Vendor Software Product version id
     * @param user         the current user
     * @return the latest upload status for the requested Vendor Software Product version
     */
    Optional<VspUploadStatusDto> findLatest(String vspId, String vspVersionId, String user);
}
