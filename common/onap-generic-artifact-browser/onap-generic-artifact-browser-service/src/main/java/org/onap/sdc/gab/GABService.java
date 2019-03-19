/*
 * ============LICENSE_START=======================================================
 * GAB
 * ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property. All rights reserved.
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

package org.onap.sdc.gab;

import java.io.IOException;
import org.onap.sdc.gab.model.GABQuery;
import org.onap.sdc.gab.model.GABResults;

/**
 * <pre>
 *     SDC/DCAE-DS FM/PM artifact browser service.
 * </pre>
 *
 * Currently the artifact browser is able to parse VES_EVENT_REGISTRATION action (registering of all VES events -
 * including alarms/faults) to serve required data from the given document.
 *
 * @version %I%, %G%
 * @since 1.0.0-SNAPSHOT
 */
public interface GABService {

    /**
     * Extracting event data based on given YAML paths. As an output, a list of results is returned.
     *
     * @param gabQuery the parameter should contain three entries:
     * <br>* JSON paths for querying specific data
     * <br>* path/content of YAML document containing faults/measurements data
     * <br>* type of the query - can be defined as a PATH or CONTENT depends of document-parameter type
     *
     * @exception IOException thrown in case of file/content problem.
     * @return Result of search the query inside the given document.
     *
     * @see GABResults
     * @see GABQuery
     */
    GABResults searchFor(GABQuery gabQuery) throws IOException;

}

