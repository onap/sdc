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

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface Manifest {

    /**
     * This Method will parse manifest, extracting fields mandatory/non-mandatory,
     * if error occurred it's recorded and will be used for deciding if manifest is valid
     * @param is manifest file input stream
     */
    void parse(InputStream is);

    /**
     * Returns if manifest is valid
     * @return true/false
     */
    boolean isValid();

    /**
     * Metadata section of manifest
     * @return
     */
    Map<String, String> getMetadata();

    /**
     * Source section of manifest
     * @return
     */
    List<String> getSources();

    /**
     * Validation errors of manifest
     * @return
     */
    List<String> getErrors();

    /**
     * non mano section of manifest sol004 #4.3.7
     * @return
     */
    Map<String, List<String>> getNonManoSources();
}
