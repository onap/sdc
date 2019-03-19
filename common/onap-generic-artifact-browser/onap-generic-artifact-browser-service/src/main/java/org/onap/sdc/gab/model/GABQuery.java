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

package org.onap.sdc.gab.model;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * See GABQuery.{@link #fields}, GABQuery.{@link #document}
 */
@AllArgsConstructor
@Getter
public class GABQuery {

    /**
     * PATH - when provided path to the yaml file
     * CONTENT - when provided yaml file content
     */
    public enum GABQueryType{
        PATH, CONTENT
    }

    /**
     * JSON paths for querying specific data (this will be the definition of a "column").
     */
    private Set<String> fields;

    /**
     * An YAML document path/content
     */
    private String document;

    /**
     * Used for query type checking.
     *
     * @see GABQueryType
     *
     */
    private GABQueryType type;

}
