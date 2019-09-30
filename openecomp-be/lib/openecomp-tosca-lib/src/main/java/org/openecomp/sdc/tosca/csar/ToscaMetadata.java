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

import java.util.Optional;
import org.openecomp.sdc.datatypes.error.ErrorMessage;

import java.util.List;
import java.util.Map;

public interface ToscaMetadata {

    /**
     * Checks if metadata file is valid.
     *
     * @return {@code true} if the metadata is valid, {@code false} otherwise
     */
    boolean isValid();

    /**
     * Gets the list of errors occurred during manifest parsing.
     *
     * @return the list of errors
     */
    List<ErrorMessage> getErrors();

    /**
     * Metadata entries of block_0.
     *
     * @return a map representing the entries
     */
    Map<String, String> getMetaEntries();

    /**
     * Checks if the entry exists.
     *
     * @param entry the entry name.
     * @return {@code true} if the entry exists, {@code false} otherwise.
     */
    boolean hasEntry(String entry);

    /**
     * Get the entry value if it exists.
     *
     * @param entry the entry to retrieve the value.
     * @return an optional with the entry value if it exists.
     */
    Optional<String> getEntry(ToscaMetaEntry entry);
}
