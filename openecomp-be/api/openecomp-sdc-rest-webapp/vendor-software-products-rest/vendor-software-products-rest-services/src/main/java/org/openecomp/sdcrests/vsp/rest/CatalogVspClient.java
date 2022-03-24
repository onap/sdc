/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation
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
 *
 *
 */

package org.openecomp.sdcrests.vsp.rest;

import java.util.Optional;
import org.openecomp.sdc.common.errors.CatalogRestClientException;

public interface CatalogVspClient {

    /**
     * Returns the name of a VF which is using the provided VSP
     *
     * @param vspId        the id of the vsp
     * @param user         the user to perform the action
     */
    Optional<String> findNameOfVfUsingVsp(String vspId, String user) throws CatalogRestClientException;
}
