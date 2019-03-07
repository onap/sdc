/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import java.util.List;
import java.util.Map;

/**
 * Validates the contents of the CSAR package uploaded in SDC.
 */

public interface Validator {

    /**
     *
     * @param contentHandler contains file and its data
     * @param folderList folder structure inside the package
     * @return errors Map of errors that occur
     */
    Map<String, List<ErrorMessage>> validateContent(FileContentHandler contentHandler, List<String> folderList);
}
