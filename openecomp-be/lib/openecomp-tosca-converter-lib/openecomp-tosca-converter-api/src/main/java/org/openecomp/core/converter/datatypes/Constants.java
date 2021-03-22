/*
 * Copyright © 2016-2017 European Support Limited
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
package org.openecomp.core.converter.datatypes;

public final class Constants {

    public static final String MAIN_ST_NAME = "MainServiceTemplate.yaml";
    public static final String GLOBAL_ST_NAME = "GlobalSubstitutionTypesServiceTemplate.yaml";
    public static final String DEFINITIONS_DIR = "Definitions/";
    public static final String TOPOLOGY_TEMPLATE = "topology_template";
    public static final String NODE_TYPE = "node_type";
    public static final String NODE_FILTER = "node_filter";
    public static final String NODE_TYPES = "node_types";
    public static final String METADATA = "metadata";
    public static final String NODE_TEMPLATES = "node_templates";
    public static final String INPUTS = "inputs";
    public static final String OUTPUTS = "outputs";
    public static final String SUBSTITUTION_MAPPINGS = "substitution_mappings";
    public static final String CAPABILITIES = "capabilities";
    public static final String REQUIREMENTS = "requirements";
    public static final String POLICIES = "policies";
    public static final String OPENECOMP_HEAT_INDEX = "openecomp_heat_index";
    public static final String ONAP_INDEX = "onap_index";
    public static final String GLOBAL_SUBSTITUTION = "GlobalSubstitutionTypes";

    // prevent utility class instantiation
    private Constants() {
    }
}
