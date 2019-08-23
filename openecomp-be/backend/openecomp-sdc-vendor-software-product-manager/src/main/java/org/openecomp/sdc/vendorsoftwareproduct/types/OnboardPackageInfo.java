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

import static org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.OrchestrationTemplateCandidateDaoZusammenImpl.InfoPropertyName.ORIGINAL_FILE_CONTENT;
import static org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.OrchestrationTemplateCandidateDaoZusammenImpl.InfoPropertyName.ORIGINAL_FILE_NAME;
import static org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.OrchestrationTemplateCandidateDaoZusammenImpl.InfoPropertyName.ORIGINAL_FILE_SUFFIX;

import java.util.HashMap;
import java.util.Map;

public class OnboardPackageInfo {

    private OnboardPackageInfo(){
    }

    public static Map<String, Object> mapOnboardPackageInfo(final String filename,
            final String fileExtension, final byte[] fileBytes) {
        final Map<String, Object> originalFileToUploadDetails = new HashMap<>();
        originalFileToUploadDetails.put(ORIGINAL_FILE_CONTENT.getVal(), fileBytes);
        originalFileToUploadDetails.put(ORIGINAL_FILE_NAME.getVal(),filename);
        originalFileToUploadDetails.put(ORIGINAL_FILE_SUFFIX.getVal(), fileExtension);
        return originalFileToUploadDetails;
    }

}
