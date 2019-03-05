/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019, Nordix Foundation. All rights reserved.
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

package org.openecomp.sdc.tosca.csar;

import org.openecomp.sdc.datatypes.error.ErrorMessage;

import java.util.List;
import java.util.Map;

public interface ToscaMetadata {
    /**
     * checks if metadata file is valid
     * @return
     */
    boolean isValid();

    /**
     * List of errors occured during manifest parsing
     * @return
     */
    List<ErrorMessage> getErrors();

    /**
     * Metadata entries of block_0
     * @return
     */
    Map<String, String> getMetaEntries();
}
