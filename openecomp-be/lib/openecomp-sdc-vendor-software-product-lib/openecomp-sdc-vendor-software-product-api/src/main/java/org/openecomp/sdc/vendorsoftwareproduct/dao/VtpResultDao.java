/**
 * Copyright (c) 2019 Vodafone Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.vendorsoftwareproduct.dao;

import java.util.List;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VtpResultsEntity;

public interface VtpResultDao {

    List<VtpResultsEntity> getVtpResult(String versionId, String vspId);

    void updateVtpResult(String id, String vspId, String vspVersionId, String requestId, String endPointName);

    void deleteVtpResult(String vspId, String vspVersionId);
}