/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.logging.servlet;

import org.onap.logging.ref.slf4j.ONAPLogConstants.ResponseStatus;

/**
 * Interpretation of request processing results.
 *
 * @author evitaliy
 * @since 02 Aug 2018
 */
public interface RequestProcessingResult {

    /**
     * Numeric status code.
     *
     * @return usually HTTP response status.
     */
    int getStatus();

    /**
     * Whether the response is considered success or failure.
     *
     * @return on of pre-defined status codes
     */
    ResponseStatus getStatusCode();

    /**
     * Human-friendly description of the numeric status.
     *
     * @return usually HTTP reason phrase
     */
    String getStatusPhrase();
}
