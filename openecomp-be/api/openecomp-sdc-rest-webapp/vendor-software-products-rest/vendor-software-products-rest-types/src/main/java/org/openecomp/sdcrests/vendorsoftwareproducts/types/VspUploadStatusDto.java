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

package org.openecomp.sdcrests.vendorsoftwareproducts.types;

import java.util.Date;
import java.util.UUID;
import lombok.Data;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspUploadStatusRecord;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspUploadStatus;

/**
 * DTO for class {@link VspUploadStatusRecord}
 */
@Data
public class VspUploadStatusDto {

    private String vspId;
    private String vspVersionId;
    private UUID lockId;
    private boolean isComplete;
    private VspUploadStatus status;
    private Date created;
    private Date updated;

}
